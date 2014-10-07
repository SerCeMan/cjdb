package ru.cjdb.sql.handlers;

import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryHandler<T extends Query> {
    QueryResult execute(T query);
}
