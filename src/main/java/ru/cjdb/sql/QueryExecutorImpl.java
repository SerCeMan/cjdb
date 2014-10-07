package ru.cjdb.sql;

import ru.cjdb.sql.handlers.QueryHandler;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Singleton
public class QueryExecutorImpl implements QueryExecutor {

    private final ConcurrentHashMap<Class<? extends Query>, QueryHandler<? extends Query>> handlers = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public QueryResult execute(Query query) {
        QueryHandler<Query> handler = (QueryHandler<Query>) handlers.get(query.getClass());
        if (handler == null) {
            throw new RuntimeException("Handler for " + query.getClass() + " not configured!");
        }
        return handler.execute(query);
    }

    @Override
    public <T extends Query> void registerHandler(Class<T> clazz, QueryHandler<T> handler) {
        handlers.put(clazz, handler);
    }
}
