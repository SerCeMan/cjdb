package ru.cjdb.sql.handlers;

import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.QueryExecutor;

public abstract class RegisterableQueryHandler<T extends Query> implements QueryHandler<T> {
    public RegisterableQueryHandler(Class<T> queryClazz, QueryExecutor queryExecutor) {
        queryExecutor.registerHandler(queryClazz, this);
    }
}
