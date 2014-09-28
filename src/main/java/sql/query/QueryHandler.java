package sql.query;

import sql.Query;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryHandler {
    QueryResult execute(Query query);
}
