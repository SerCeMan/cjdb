package sql;

import sql.query.QueryHandler;
import sql.query.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryExecutor {
    QueryResult execute(Query query);

    <T extends Query> void registerHandler(Class<T> clazz, QueryHandler<T> handler);
}
