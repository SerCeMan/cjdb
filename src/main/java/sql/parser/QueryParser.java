package sql.parser;

import sql.queries.Query;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryParser {
    Query parseQuery(String sql);
}
