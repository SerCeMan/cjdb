package sql;

import sql.handlers.QueryHandler;
import sql.queries.Query;
import sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryExecutor {
    QueryResult execute(Query query);

    <T extends Query> void registerHandler(Class<T> clazz, QueryHandler<T> handler);
}
