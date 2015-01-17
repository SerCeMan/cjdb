package ru.cjdb.sql;

import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ru.cjdb.CjDbModule;
import ru.cjdb.printer.ConsoleResultPrinter;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Order;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.parser.QueryParser;
import ru.cjdb.sql.parser.QueryParserModule;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.ddl.CreateIndexQuery;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ru.cjdb.scheme.dto.Index.IndexType;
import static ru.cjdb.sql.expressions.conditions.Comparison.BinOperator.GREATER;
import static ru.cjdb.sql.queries.ddl.CreateTableQuery.ColumnDefinition;
import static ru.cjdb.testutils.TestUtils.assertRow;

public class QueryExecutorImplTest {

    @Inject
    QueryExecutor queryExecutor;
    @Inject
    QueryParser queryParser;

    private static void assertEnd(Cursor cursor) {
        assertNull(cursor.nextRow());
    }

    @Before
    public void setup() {
        ObjectGraph.create(new QueryExecutorImplTestModule()).inject(this);
    }

    @Test
    public void testCreateThenInsertThenSelect() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, 2);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

        assertEquals(2, queryResult.getCursor().nextRow().getAt(0));
    }

    @Test
    public void testInsertThenUpdate() {
        String tableName = TestUtils.createRandomName();

        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(
                        new ColumnDefinition("test1", Types.INT),
                        new ColumnDefinition("test2", Types.INT)
                ));
        queryExecutor.execute(createTableQuery);

        queryExecutor.execute(new InsertQuery(tableName, 1, 1));
        queryExecutor.execute(new InsertQuery(tableName, 2, 2));
        queryExecutor.execute(new InsertQuery(tableName, 3, 3));

        exec("update %s set test1=2, test2=2 where test1=1", tableName);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test1", "test2"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);


        Assert.assertTrue(queryResult.hasResult());

        Cursor cursor = queryResult.getCursor();
        assertRow(cursor, 2, 2);
        assertRow(cursor, 2, 2);
        assertRow(cursor, 3, 3);
    }

    @Test
    public void testDifferentColumns() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(
                        new ColumnDefinition("test1", Types.INT),
                        new ColumnDefinition("test2", Types.INT)
                ));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, 1, 2);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test2"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

        assertEquals(2, queryResult.getCursor().nextRow().getAt(0));
    }

    @Test
    public void testHashIndexWhereCreateLater() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT)", tableName);

        for (int i = 0; i < 10; i++) {
            exec("insert into %s values(%s)", tableName, i % 3);
        }

        queryExecutor.execute(new CreateIndexQuery(TestUtils.createRandomName(),
                tableName, false, IndexType.HASH, Arrays.asList(new Index.IndexColumnDef("test1", Order.ASC))
        ));

        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();
        assertRow(cursor, 1);
        assertRow(cursor, 1);
        assertRow(cursor, 1);
    }

    @Test
    public void testHashIndexWhere() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT)", tableName);

        queryExecutor.execute(new CreateIndexQuery(TestUtils.createRandomName(),
                tableName, false, IndexType.HASH, Arrays.asList(new Index.IndexColumnDef("test1", Order.ASC))
        ));

        for (int i = 0; i < 10; i++) {
            exec("insert into %s values(%s)", tableName, i % 3);
        }

        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();
        assertRow(cursor, 1);
        assertRow(cursor, 1);
        assertRow(cursor, 1);
    }

    @Test
    public void testHashIndexRemove() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING HASH", indexName, tableName);

        exec("insert into %s values(%s, %s)", tableName, 1, 1);
        exec("insert into %s values(%s, %s)", tableName, 1, 2);
        exec("insert into %s values(%s, %s)", tableName, 3, 3);

        exec("delete from %s where test2=%s", tableName, 1);

        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();
        assertRow(cursor, 1, 2);
        assertEnd(cursor);
    }

    @Test
    public void testBTreeIndexRemove() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING BTREE", indexName, tableName);

        exec("insert into %s values(%s, %s)", tableName, 1, 1);
        exec("insert into %s values(%s, %s)", tableName, 1, 2);
        exec("insert into %s values(%s, %s)", tableName, 3, 3);

        exec("delete from %s where test2=%s", tableName, 1);

        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();
        assertRow(cursor, 1, 2);
        assertEnd(cursor);
    }

    @Test
    public void testBTreeIndexSmall() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING BTREE", indexName, tableName);

        exec("insert into %s values(%s, %s)", tableName, 1, 1);
        exec("insert into %s values(%s, %s)", tableName, 1, 2);
        exec("insert into %s values(%s, %s)", tableName, 3, 3);

        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();
        assertRow(cursor, 1, 1);
        assertRow(cursor, 1, 2);
        assertEnd(cursor);
    }

    @Test
    public void testBTreeIndexMiddle() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING BTREE", indexName, tableName);

        int count = 1500;
        HashSet<Integer> contatiner = new HashSet<>();
        for (int i = 0; i < count; i++) {
            exec("insert into %s values(%s, %s)", tableName, 1, i);
            contatiner.add(i);
        }
        Cursor cursor = exec("select * from %s where test1=1", tableName).getCursor();

        for (int i = 0; i < count; i++) {
            Row row = cursor.nextRow();
            assertEquals(1, row.getAt(0));
            Integer element = (Integer) row.getAt(1);
            assertTrue(contatiner.contains(element));
            contatiner.remove(element);
        }
        assertTrue(contatiner.isEmpty());
        assertEnd(cursor);
    }

    @Test
    public void testBTreeIndexOneLevel() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING BTREE", indexName, tableName);

        int count = 16000; // works for 1600000
        HashSet<Integer> contatiner = new HashSet<>();
        for (int i = 0; i < count; i++) {
            exec("insert into %s values(%s, %s)", tableName, i, i);
            contatiner.add(i);
        }
        Cursor cursor = exec("select * from %s where test1<" + (count + 1), tableName).getCursor();

        for (int i = 0; i < count; i++) {
            Row row = cursor.nextRow();
            assertEquals(row.getAt(1), row.getAt(0));
            Integer element = (Integer) row.getAt(1);
            assertTrue(contatiner.contains(element));
            contatiner.remove(element);
        }
        assertTrue(contatiner.isEmpty());
        assertEnd(cursor);
    }

    @Test
    public void testBTreeIndexManyLevel() {
        String tableName = TestUtils.createRandomName();
        exec("create table %s (test1 VARCHAR(200), test2 INT)", tableName);

        String indexName = TestUtils.createRandomName();
        exec("CREATE INDEX %s ON %s(test1) USING BTREE", indexName, tableName);

        int count = 10000;
        HashSet<String> contatiner = new HashSet<>();
        for (int i = 0; i < count; i++) {
            String val = String.valueOf(i);
            exec("insert into %s values('%s', %s)", tableName, val, i);
            if (!val.startsWith("9")) {
                contatiner.add(val);
            }
        }
        Cursor cursor = exec("select * from %s where test1<'9'", tableName).getCursor();

        while (!contatiner.isEmpty()) {
            Row row = cursor.nextRow();
            String element = (String) row.getAt(0);
            assertTrue(contatiner.contains(element));
            contatiner.remove(element);
        }
        assertTrue(contatiner.isEmpty());
        assertEnd(cursor);
    }

    @Test
    public void testSimpleWhereLess() {
        String tableName = TestUtils.createRandomName();

        exec("create table %s (test1 INT, test2 INT)", tableName);

        exec("insert into %s values(%s, %s)", tableName, 1, 1);
        exec("insert into %s values(%s, %s)", tableName, 2, 2);
        exec("insert into %s values(%s, %s)", tableName, 3, 3);
        exec("insert into %s values(%s, %s)", tableName, 4, 4);

        Cursor cursor = exec("select test1,test2 from %s where test2<%s", tableName, 3).getCursor();

        assertRow(cursor, 1, 1);
        assertRow(cursor, 2, 2);
        assertTrue(cursor.nextRow() == null);
    }

    @Test
    public void testSimpleJoin() {
        String tableName1 = TestUtils.createRandomName();
        exec("create table %s (test1 INT, test3 INT)", tableName1);
        String tableName2 = TestUtils.createRandomName();
        exec("create table %s (test2 INT, test4 INT)", tableName2);

        exec("insert into %s values(%s, %s)", tableName1, 1, 8);
        exec("insert into %s values(%s, %s)", tableName1, 2, 7);
        exec("insert into %s values(%s, %s)", tableName1, 3, 6);
        exec("insert into %s values(%s, %s)", tableName1, 4, 5);
        exec("insert into %s values(%s, %s)", tableName1, 5, 14);
        exec("insert into %s values(%s, %s)", tableName1, 5, 15);


        exec("insert into %s values(%s, %s)", tableName2, 3, 11);
        exec("insert into %s values(%s, %s)", tableName2, 4, 12);
        exec("insert into %s values(%s, %s)", tableName2, 5, 13);
        exec("insert into %s values(%s, %s)", tableName2, 5, 18);
        exec("insert into %s values(%s, %s)", tableName2, 6, 14);

        Cursor cursor = exec("select test1,test3,test2,test4 from %s join %s on test1=test2", tableName1, tableName2).getCursor();

        assertRow(cursor, 3, 6, 3, 11);
        assertRow(cursor, 4, 5, 4, 12);
        assertRow(cursor, 5, 14, 5, 13);
        assertRow(cursor, 5, 14, 5, 18);
        assertRow(cursor, 5, 15, 5, 13);
        assertRow(cursor, 5, 15, 5, 18);
        assertTrue(cursor.nextRow() == null);
    }

    @Test
    public void testSimpleDelete() {
        String tableName = TestUtils.createRandomName();

        exec("create table %s (test1 INT, test2 INT)", tableName);

        exec("insert into %s values(%s, %s)", tableName, 1, 1);
        exec("insert into %s values(%s, %s)", tableName, 2, 2);
        exec("insert into %s values(%s, %s)", tableName, 3, 3);
        exec("insert into %s values(%s, %s)", tableName, 4, 4);

        exec("delete from %s where test1>%s", tableName, 2);

        exec("insert into %s values(%s, %s)", tableName, 5, 5);
        exec("insert into %s values(%s, %s)", tableName, 6, 6);

        Cursor cursor = exec("select test1,test2 from %s", tableName).getCursor();

        assertRow(cursor, 1, 1);
        assertRow(cursor, 2, 2);
        assertRow(cursor, 5, 5);
        assertRow(cursor, 6, 6);
        assertTrue(cursor.nextRow() == null);
    }

    @Test
    public void testSimpleWhere() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        queryExecutor.execute(new InsertQuery(tableName, 1));
        queryExecutor.execute(new InsertQuery(tableName, 2));
        queryExecutor.execute(new InsertQuery(tableName, 3));
        queryExecutor.execute(new InsertQuery(tableName, 4));

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test"),
                new Comparison(
                        new ColumnValueExpr("test", Types.INT),
                        new ValueExpression("2"),
                        GREATER
                ));
        Cursor cursor = queryExecutor.execute(selectQuery).getCursor();

        assertRow(cursor, 3);
        assertRow(cursor, 4);
        assertTrue(cursor.nextRow() == null);
    }

    @Test
    public void testCreateThenInsertThenSelectMoreThanPage() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        int count = 4096 * 4;
        Object[] arr = {0};
        InsertQuery insertQuery = new InsertQuery(tableName, arr);
        for (int i = 0; i < count; i++) {
            arr[0] = i;
            queryExecutor.execute(insertQuery);
        }


        SelectQuery selectQuery = new SelectQuery(tableName, asList("test"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);


        Assert.assertTrue(queryResult.hasResult());
        for (int i = 0; i < count; i++) {
            assertEquals(i, queryResult.getCursor().nextRow().getAt(0));
        }
    }

    @Test
    public void testCreateThenInsertThenSelectThenPrint() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("column1", Types.INT),
                        new ColumnDefinition("column2", Types.varchar(4)),
                        new ColumnDefinition("column3", Types.DOUBLE)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, 1, "str1", 3.14);
        queryExecutor.execute(insertQuery);
        insertQuery = new InsertQuery(tableName, 2, "str2", 2.718281828459045);
        queryExecutor.execute(insertQuery);
        insertQuery = new InsertQuery(tableName, 3, "str3", 1.0);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("column1", "column2", "column3"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        ConsoleResultPrinter printer = new ConsoleResultPrinter();
        printer.print(queryResult);
    }

    @Test
    public void testCreateThenInsertThenSelectVarchar() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("str1", Types.varchar(1)),
                        new ColumnDefinition("str10", Types.varchar(10))));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, "a", "abcdefghij");
        queryExecutor.execute(insertQuery);
        insertQuery = new InsertQuery(tableName, "k", "klmnopqrst");
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("str1", "str10"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        assertEquals(row1.getColumnCount(), 2);
        assertEquals(row1.getAt(0), "a");
        assertEquals(row1.getAt(1), "abcdefghij");

        assertRow(queryResult.getCursor(), "k", "klmnopqrst");
    }

    @Test
    public void testCreateThenInsertThenSelectDoubleType() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("double1", Types.DOUBLE),
                        new ColumnDefinition("constant", Types.DOUBLE)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, 1.0, 3.14);
        queryExecutor.execute(insertQuery);
        insertQuery = new InsertQuery(tableName, 2.0, 2.718281828459045);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("double1", "constant"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        assertEquals(row1.getColumnCount(), 2);
        assertEquals(row1.getAt(0), 1.0);
        assertEquals(row1.getAt(1), 3.14);

        Row row2 = queryResult.getCursor().nextRow();
        assertEquals(row2.getAt(0), 2.0);
        assertEquals(row2.getAt(1), 2.718281828459045);
    }

    @Test
    public void testCreateThenInsertThenSelectMultipleColumnsAndRows() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new ColumnDefinition("test1", Types.INT),
                        new ColumnDefinition("test2", Types.INT)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery1 = new InsertQuery(tableName, 1, 2);
        queryExecutor.execute(insertQuery1);

        InsertQuery insertQuery2 = new InsertQuery(tableName, 3, 4);
        queryExecutor.execute(insertQuery2);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test1", "test2"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

//        Assert.assertEquals(queryResult.getCursor().getRowCount(), 2);
        Row row1 = queryResult.getCursor().nextRow();
        assertEquals(row1.getColumnCount(), 2);

        assertEquals(row1.getAt(0), 1);
        assertEquals(row1.getAt(1), 2);

        Row row2 = queryResult.getCursor().nextRow();
        assertEquals(row2.getAt(0), 3);
        assertEquals(row2.getAt(1), 4);
    }

    private QueryResult exec(String sql, Object... params) {
        String req = String.format(sql, params);
        Query query = queryParser.parseQuery(req);
        return queryExecutor.execute(query);
    }

    //    @Test
    public void indexSimplePerfTest() {
        // use JMH, i know, i know, but 4 second is not nanoseconds
        String tableName = TestUtils.createRandomName();

        exec("create table %s (test1 INT, test2 VARCHAR(500))", tableName);

        queryExecutor.execute(new CreateIndexQuery(TestUtils.createRandomName(),
                tableName, false, IndexType.HASH, Arrays.asList(new Index.IndexColumnDef("test1", Order.ASC))
        ));

        long start = System.currentTimeMillis();

        int count = 10_000;
        ThreadLocalRandom tlr = ThreadLocalRandom.current();
        int searched = 3;
        int expect = 0;
        for (int i = 0; i < count; i++) {
            int val = tlr.nextInt(count / 10);
            if (val == searched) {
                expect++;
            }
            exec("insert into %s values(%s, '%s')", tableName, val, "ok");
        }
        Cursor cursor = exec("select test1,test2 from %s where test1=%s", tableName, searched).getCursor();

        int actual = 0;
        while (cursor.nextRow() != null) {
            actual++;
        }

        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - start) + "ms");

        assertEquals(expect, actual);
    }

    @Module(injects = QueryExecutorImplTest.class, includes = {CjDbModule.class, QueryParserModule.class})
    public static final class QueryExecutorImplTestModule {
    }
}