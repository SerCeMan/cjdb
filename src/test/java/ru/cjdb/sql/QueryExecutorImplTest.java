package ru.cjdb.sql;

import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.CjDbModule;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static ru.cjdb.sql.queries.ddl.CreateTableQuery.RowDefinition;

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
                asList(new RowDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery(tableName, 2);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Assert.assertEquals(2, queryResult.getResult().getRow(0).getAt(0));
    }

    @Test
    public void testCreateThenInsertThenSelectMoreThanPage() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new RowDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        int count = 4096;
        for (int i = 0; i < count; i++) {
            InsertQuery insertQuery = new InsertQuery(tableName, i);
            queryExecutor.execute(insertQuery);
        }


        SelectQuery selectQuery = new SelectQuery(tableName, asList("test"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());
        Assert.assertEquals(count, queryResult.getResult().getRowCount());
        for (int i = 0; i < count; i++) {
            Assert.assertEquals(i, queryResult.getResult().getRow(i).getAt(0));
        }
    }


    @Test
    public void testCreateThenInsertThenSelectMultipleColumnsAndRows() {
        String tableName = TestUtils.createRandomName();
        CreateTableQuery createTableQuery = new CreateTableQuery(tableName,
                asList(new RowDefinition("test1", Types.INT),
                        new RowDefinition("test2", Types.INT)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery1 = new InsertQuery(tableName, 1, 2);
        queryExecutor.execute(insertQuery1);

        InsertQuery insertQuery2 = new InsertQuery(tableName, 3, 4);
        queryExecutor.execute(insertQuery2);

        SelectQuery selectQuery = new SelectQuery(tableName, asList("test1", "test2"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Assert.assertEquals(queryResult.getResult().getRowCount(), 2);
        Assert.assertEquals(queryResult.getResult().getRow(0).getColumnCount(), 2);

        Assert.assertEquals(queryResult.getResult().getRow(0).getAt(0), 1);
        Assert.assertEquals(queryResult.getResult().getRow(0).getAt(1), 2);
        Assert.assertEquals(queryResult.getResult().getRow(1).getAt(0), 3);
        Assert.assertEquals(queryResult.getResult().getRow(1).getAt(1), 4);
    }

    @Module(injects = QueryExecutorImplTest.class, includes = CjDbModule.class)
    public static final class QueryExecutorImplTestModule {
    }
}