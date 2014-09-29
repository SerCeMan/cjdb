package sql.parser;

import config.ConfigModule;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import sql.queries.Query;
import sql.queries.dml.SelectQuery;

import javax.inject.Inject;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryParserImplTest {

    @Inject
    QueryParser queryParser;

    @Before
    public void setup() {
        ObjectGraph.create(new MetaStorageTestModule()).inject(this);
    }

    @Test
    public void testParseSimpleSelectQuery() {
        Query query = queryParser.parseQuery("select test from test");

        assertTrue(query instanceof SelectQuery);
        SelectQuery select = (SelectQuery)query;
        assertEquals("test", select.getFrom());
        assertEquals(Arrays.asList("test"), select.getProjections());
    }


    @Module(injects = QueryParserImplTest.class, includes = ConfigModule.class)
    public static final class MetaStorageTestModule {
        @Provides
        public QueryParser provideStorage() {
            return new QueryParserImpl();
        }
    }
}