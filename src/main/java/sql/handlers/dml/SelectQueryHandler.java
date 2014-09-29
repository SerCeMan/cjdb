package sql.handlers.dml;

import sql.QueryExecutor;
import sql.handlers.RegisterableQueryHandler;
import sql.queries.dml.SelectQuery;
import sql.result.QueryResult;

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
