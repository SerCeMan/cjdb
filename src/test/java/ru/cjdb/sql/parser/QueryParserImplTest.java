package ru.cjdb.sql.parser;

import com.google.common.collect.ImmutableMap;
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
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.DeleteQuery;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.queries.dml.UpdateQuery;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.cjdb.sql.queries.ddl.CreateTableQuery.ColumnDefinition;

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
    public void testParseSimpleUpdate() {
        String tableName = TestUtils.createRandomName();
        Table table = new Table(tableName);
        table.addColumn(new Column("test1", Types.INT));
        table.addColumn(new Column("test2", Types.varchar(20)));
        metainfoService.addTable(table);

        Query query = queryParser.parseQuery("update " + tableName + " set test1=1, test2='ok' where test1=2");

        assertTrue(query instanceof UpdateQuery);
        UpdateQuery update = (UpdateQuery) query;
        assertEquals(tableName, update.getTable());
        ImmutableMap<String, Object> map = ImmutableMap.<String, Object>builder()
                .put("test1", 1)
                .put("test2", "ok")
                .build();
        assertEquals(map, update.getValues());
        assertTrue(update.getCondition() instanceof Comparison);
        Comparison comp = (Comparison) update.getCondition();
        ColumnValueExpr left = (ColumnValueExpr) comp.getLeft();
        assertEquals("test1", left.getName());
        assertEquals(Types.INT, left.getType());
        ValueExpression right = (ValueExpression) comp.getRight();
        assertEquals("2", right.getValue(null));
    }


    @Test
    public void testParseSimpleInsert() {
        String tableName = TestUtils.createRandomName();
        Query query = queryParser.parseQuery("insert into " + tableName + " values (1, 2.0, 'hello')");

        assertTrue(query instanceof InsertQuery);
        InsertQuery insert = (InsertQuery) query;

        assertEquals(tableName, insert.getName());
        assertEquals(1, insert.getValues()[0]);
        assertEquals(2.0, insert.getValues()[1]);
        assertEquals("hello", insert.getValues()[2]);
    }

    @Test
    public void testParseSimpleDelete() {
        String tableName = TestUtils.createRandomName();
        Table table = new Table(tableName);
        table.addColumn(new Column("test1", Types.INT));
        table.addColumn(new Column("test2", Types.varchar(20)));
        metainfoService.addTable(table);
        Query query = queryParser.parseQuery("delete from " + tableName + " where test1=2");

        assertTrue(query instanceof DeleteQuery);
        DeleteQuery delete = (DeleteQuery) query;

        assertEquals(tableName, delete.getTable());
        assertTrue(delete.getCondition() instanceof Comparison);
        Comparison comp = (Comparison) delete.getCondition();
        ColumnValueExpr left = (ColumnValueExpr) comp.getLeft();
        assertEquals("test1", left.getName());
        assertEquals(Types.INT, left.getType());
        ValueExpression right = (ValueExpression) comp.getRight();
        assertEquals("2", right.getValue(null));
    }


    @Test
    public void testCreateTableQuery() {
        String cool_table = "cool_table";
        Query tableQuery = queryParser.parseQuery("create table " + cool_table +
                "(test1 INT, test2 DOUBLE, test3 VARCHAR(200))");

        assertTrue(tableQuery instanceof CreateTableQuery);
        CreateTableQuery query = (CreateTableQuery) tableQuery;
        assertEquals(cool_table, query.getName());
        ColumnDefinition colDef1 = query.getRows().get(0);
        assertEquals("test1", colDef1.getName());
        assertEquals(Types.INT, colDef1.getType());

        ColumnDefinition colDef2 = query.getRows().get(1);
        assertEquals("test2", colDef2.getName());
        assertEquals(Types.DOUBLE, colDef2.getType());


        ColumnDefinition colDef3 = query.getRows().get(2);
        assertEquals("test3", colDef3.getName());
        assertEquals(Types.varchar(200), colDef3.getType());
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


    @Module(injects = QueryParserImplTest.class, includes = {ConfigModule.class, MetaStorageTest.MetaStorageTestModule.class})
    public static final class QueryParserTestModule {
        @Provides
        @Singleton
        public QueryParser provideStorage(MetainfoService metainfoService) {
            return new QueryParserImpl(metainfoService);
        }
    }
}