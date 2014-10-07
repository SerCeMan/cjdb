package ru.cjdb.sql.handlers.dml;

import ru.cjdb.sql.QueryExecutor;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Singleton
public class SelectQueryHandler extends RegisterableQueryHandler<SelectQuery> {
    @Inject
    public SelectQueryHandler(QueryExecutor queryExecutor) {
        super(SelectQuery.class, queryExecutor);
    }

    @Override
    public QueryResult execute(SelectQuery query) {
        return null;
    }
}
