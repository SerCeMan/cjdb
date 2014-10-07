package ru.cjdb.sql;

import ru.cjdb.sql.handlers.QueryHandler;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryExecutor {
    QueryResult execute(Query query);

    <T extends Query> void registerHandler(Class<T> clazz, QueryHandler<T> handler);
}
