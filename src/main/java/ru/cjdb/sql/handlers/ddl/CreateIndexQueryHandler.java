package ru.cjdb.sql.handlers.ddl;

import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.ddl.CreateIndexQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.impl.OkQueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Обработчик создания индекса
 *
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
@Singleton
public class CreateIndexQueryHandler extends RegisterableQueryHandler<CreateIndexQuery> {

    @Inject
    MetainfoService metainfoService;

    public CreateIndexQueryHandler() {
        super(CreateIndexQuery.class);
    }

    @Override
    public QueryResult execute(CreateIndexQuery query) {
        Index index = new Index(query.getName(), query.getTable(), query.isUnique(), query.getIndexColDef());
        metainfoService.addIndex(index);
        return OkQueryResult.INSTANCE;
    }
}
