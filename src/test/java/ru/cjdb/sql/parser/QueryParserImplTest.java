package ru.cjdb.sql.parser;

import ru.cjdb.config.ConfigModule;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.scheme.MetaStorageTest;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.MetainfoServiceImpl;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryParserImplTest {

    @Inject
    QueryParser queryParser;
    @Inject
    MetainfoService metainfoService;

    @Before
    public void setup() {
        ObjectGraph.create(new QueryParserTestModule()).inject(this);
    }

    @Test
    public void testParseSimpleSelectQuery() {
        String tableName = TestUtils.createRandomName();
        Table table = new Table(tableName);
        table.addColumn(new Column("test", Types.INT));
        metainfoService.addTable(table);

        Query query = queryParser.parseQuery("select test from " + tableName);

        assertTrue(query instanceof SelectQuery);
        SelectQuery select = (SelectQuery) query;
        assertEquals(tableName, select.getFrom());
        assertEquals(Arrays.asList("test"), select.getProjections());
    }

    @Test
    public void testParseSimpleSelectQueryWhere() {
        String tableName = TestUtils.createRandomName();
        Table table = new Table(tableName);
        table.addColumn(new Column("test", Types.INT));
        metainfoService.addTable(table);

        Query query = queryParser.parseQuery("select test from " + tableName + " where 'test'=2");

        assertTrue(query instanceof SelectQuery);
        SelectQuery select = (SelectQuery) query;
        assertEquals(tableName, select.getFrom());
        assertEquals(Arrays.asList("test"), select.getProjections());
    }


    @Module(injects = QueryParserImplTest.class, includes = { ConfigModule.class, MetaStorageTest.MetaStorageTestModule.class})
    public static final class QueryParserTestModule {
        @Provides
        @Singleton
        public QueryParser provideStorage(MetainfoService metainfoService) {
            return new QueryParserImpl(metainfoService);
        }
    }
}