package ru.cjdb.sql.handlers;

import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

/**
 * Обработчик конкретного запроса к базе.
 *
 * При создании обработчика необходимо зарегистрировать его в QueryExecutor
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryHandler<T extends Query> {
    QueryResult execute(T query);

    Class<T> getQueryClass();
}
