package sql.parser;

import sql.queries.Query;
import sql.queries.dml.SelectQuery;

import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class QueryParserImpl implements QueryParser {

    @Override
    public Query parseQuery(String sql) {
        return new SelectQuery("test", Arrays.asList("test"));
    }
}
