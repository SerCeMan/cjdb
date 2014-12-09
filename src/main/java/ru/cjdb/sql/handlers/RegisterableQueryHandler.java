package ru.cjdb.sql.handlers;

import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.QueryExecutor;

/**
 * Базовый класс для всех обработчиков запросов.
 *
 * Позволяет регистрировать обработчик в ru.cjdb.sql.QueryExecutorModule.
 *
 * @author Sergey Tselovalnikov
 * @since 07.10.14
 */
public abstract class RegisterableQueryHandler<T extends Query> implements QueryHandler<T> {
    private final Class<T> queryClass;

    public RegisterableQueryHandler(Class<T> queryClazz) {
        this.queryClass = queryClazz;
    }

    @Override
    public final Class<T> getQueryClass() {
        return queryClass;
    }
}
