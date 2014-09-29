package sql.handlers;

import sql.queries.Query;
import sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryHandler<T extends Query> {
    QueryResult execute(T query);
}
