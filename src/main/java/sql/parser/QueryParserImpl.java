package sql.parser;

import sql.queries.Query;
import sql.queries.dml.InsertQuery;
import sql.queries.dml.SelectQuery;

import javax.inject.Singleton;

import static java.util.Arrays.asList;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
@Singleton
public class QueryParserImpl implements QueryParser {

    @Override
    public Query parseQuery(String sql) {
        sql = sql.toLowerCase();
        if(sql.startsWith("select")) {
            return new SelectQuery("test", asList("test"));
        }
        return new InsertQuery("test", 1);
    }
}
