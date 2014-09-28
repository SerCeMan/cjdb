package sql.handlers;

import sql.Query;
import sql.QueryExecutor;
import sql.query.QueryHandler;

public abstract class RegisterableQueryHandler<T extends Query> implements QueryHandler<T> {
    public RegisterableQueryHandler(Class<T> queryClazz, QueryExecutor queryExecutor) {
        queryExecutor.registerHandler(queryClazz, this);
    }
}
