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
        ObjectGraph.create(new QueryExecutorImplTestModule ()).inject(this);
    }

    @Test
    public void testCreateThenInsertThenSelect() {
        CreateTableQuery createTableQuery = new CreateTableQuery(TestUtils.createRandomName(),
                asList(new RowDefinition("test", Types.INT)));
        queryExecutor.execute(createTableQuery);

        InsertQuery insertQuery = new InsertQuery("Test", 2);
        queryExecutor.execute(insertQuery);

        SelectQuery selectQuery = new SelectQuery("Test", asList("test"));
        QueryResult queryResult = queryExecutor.execute(selectQuery);

        Assert.assertTrue(queryResult.isSuccessful());
        Assert.assertTrue(queryResult.hasResult());

        Assert.assertEquals(queryResult.getResult().getRow(0).getAt(0), 2);
    }

    @Module(injects = QueryExecutorImplTest.class, includes = CjDbModule.class)
    public static final class QueryExecutorImplTestModule {
    }
}