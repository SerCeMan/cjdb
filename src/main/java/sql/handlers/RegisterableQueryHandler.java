package sql.handlers;

import sql.queries.Query;
import sql.QueryExecutor;

public abstract class RegisterableQueryHandler<T extends Query> implements QueryHandler<T> {
    public RegisterableQueryHandler(Class<T> queryClazz, QueryExecutor queryExecutor) {
        queryExecutor.registerHandler(queryClazz, this);
    }
}
