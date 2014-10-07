package ru.cjdb.sql;

import ru.cjdb.sql.handlers.QueryHandler;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

/**
 * Обработчик всех запросов к БД.
 *
 * Хранит обработчик для каждого типа запроса.
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryExecutor {
    QueryResult execute(Query query);
}
