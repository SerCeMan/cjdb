package ru.cjdb.sql;

import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.CjDbModule;
import ru.cjdb.printer.ResultPrinterImpl;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static ru.cjdb.sql.queries.ddl.CreateTableQuery.ColumnDefinition;

public class QueryExecutorImplTest {

    @Inject
    QueryExecutor queryExecutor;

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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Assert.assertEquals(2, queryResult.getCursor().nextRow().getAt(0));
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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Assert.assertEquals(2, queryResult.getCursor().nextRow().getAt(0));
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
                        Comparison.BinOperator.GREATER
                ));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        Assert.assertEquals(3, row1.getAt(0));

        Row row2 = queryResult.getCursor().nextRow();
        Assert.assertEquals(4, row2.getAt(0));
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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(i, queryResult.getCursor().nextRow().getAt(0));
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

        ResultPrinterImpl printer = new ResultPrinterImpl();
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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row1.getColumnCount(), 2);
        Assert.assertEquals(row1.getAt(0), "a");
        Assert.assertEquals(row1.getAt(1), "abcdefghij");

        Row row2 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row2.getAt(0),"k");
        Assert.assertEquals(row2.getAt(1), "klmnopqrst");
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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Row row1 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row1.getColumnCount(), 2);
        Assert.assertEquals(row1.getAt(0), 1.0);
        Assert.assertEquals(row1.getAt(1), 3.14);

        Row row2 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row2.getAt(0), 2.0);
        Assert.assertEquals(row2.getAt(1), 2.718281828459045);
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

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

//        Assert.assertEquals(queryResult.getCursor().getRowCount(), 2);
        Row row1 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row1.getColumnCount(), 2);

        Assert.assertEquals(row1.getAt(0), 1);
        Assert.assertEquals(row1.getAt(1), 2);

        Row row2 = queryResult.getCursor().nextRow();
        Assert.assertEquals(row2.getAt(0), 3);
        Assert.assertEquals(row2.getAt(1), 4);
    }

    @Module(injects = QueryExecutorImplTest.class, includes = CjDbModule.class)
    public static final class QueryExecutorImplTestModule {
    }
}