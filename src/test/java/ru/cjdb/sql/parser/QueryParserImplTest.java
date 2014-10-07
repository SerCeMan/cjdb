package ru.cjdb.sql.parser;

import ru.cjdb.config.ConfigModule;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.dml.SelectQuery;

import javax.inject.Inject;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryParserImplTest {

    @Inject
    QueryParser queryParser;

    @Before
    public void setup() {
        ObjectGraph.create(new QueryParserTestModule()).inject(this);
    }

    @Test
    public void testParseSimpleSelectQuery() {
        Query query = queryParser.parseQuery("select test from test");

        assertTrue(query instanceof SelectQuery);
        SelectQuery select = (SelectQuery) query;
        assertEquals("test", select.getFrom());
        assertEquals(Arrays.asList("test"), select.getProjections());
    }


    @Module(injects = QueryParserImplTest.class, includes = ConfigModule.class)
    public static final class QueryParserTestModule {
        @Provides
        public QueryParser provideStorage() {
            return new QueryParserImpl();
        }
    }
}