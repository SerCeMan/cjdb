package ru.cjdb.sql;

import com.google.common.collect.ImmutableMap;
import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
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
import ru.cjdb.sql.queries.dml.UpdateQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static ru.cjdb.scheme.dto.Index.IndexType;
import static ru.cjdb.sql.expressions.conditions.Comparison.BinOperator;
import static ru.cjdb.sql.expressions.conditions.Comparison.BinOperator.GREATER;
import static ru.cjdb.sql.queries.ddl.CreateTableQuery.ColumnDefinition;

public class QueryExecutorImplTest {

    @Inject
    QueryExecutor queryExecutor;
    @Inject
    QueryParser queryParser;

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

        UpdateQuery update = new UpdateQuery(tableName,
                ImmutableMap.of("test1", 2, "test2", 2),
                new Comparison(
                        new ColumnValueExpr("test1", Types.INT),
                        new ValueExpression("1"),
                        BinOperator.EQUAL
                ));

        queryExecutor.execute(update);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test1", "test2"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);


        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        assertEquals(2, row1.getAt(0));
        assertEquals(2, row1.getAt(1));

        Row row2 = queryResult.getCursor().nextRow();
        assertEquals(2, row2.getAt(0));
        assertEquals(2, row2.getAt(1));

        Row row3 = queryResult.getCursor().nextRow();
        assertEquals(3, row3.getAt(0));
        assertEquals(3, row3.getAt(1));
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
        assertEquals(1, cursor.nextRow().getAt(0));
        assertEquals(1, cursor.nextRow().getAt(0));
        assertEquals(1, cursor.nextRow().getAt(0));
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
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        assertEquals(3, row1.getAt(0));

        Row row2 = queryResult.getCursor().nextRow();
        assertEquals(4, row2.getAt(0));
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

        Row row2 = queryResult.getCursor().nextRow();
        assertEquals(row2.getAt(0), "k");
        assertEquals(row2.getAt(1), "klmnopqrst");
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

    @Module(injects = QueryExecutorImplTest.class, includes = {CjDbModule.class, QueryParserModule.class})
    public static final class QueryExecutorImplTestModule {
    }
}