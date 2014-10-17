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
public class QueryExecutorImpl implements QueryExecutor {

    private final ConcurrentHashMap<Class<? extends Query>, QueryHandler<? extends Query>> handlers = new ConcurrentHashMap<>();

    public void registerHandlers(QueryHandler<?>... queryHandlers) {
        for (QueryHandler<?> handler : queryHandlers) {
            handlers.put(handler.getQueryClass(), handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public QueryResult execute(Query query) {
        QueryHandler<Query> handler = (QueryHandler<Query>) handlers.get(query.getClass());
        if (handler == null) {
            throw new RuntimeException("Handler for " + query.getClass() + " not configured!");
        }
        return handler.execute(query);
    }

}
