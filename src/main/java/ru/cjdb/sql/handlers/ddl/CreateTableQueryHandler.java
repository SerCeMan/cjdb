package ru.cjdb.sql.handlers.ddl;

import ru.cjdb.scheme.MetaStorage;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.QueryExecutor;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.result.OkQueryResult;
import ru.cjdb.sql.result.QueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Sergey Tselovalnikov
 * @since 03.10.14
 */
@Singleton
public class CreateTableQueryHandler extends RegisterableQueryHandler<CreateTableQuery> {

    @Inject
    MetaStorage metaStorage;

    @Inject
    public CreateTableQueryHandler(QueryExecutor queryExecutor) {
        super(CreateTableQuery.class, queryExecutor);
    }

    @Override
    public QueryResult execute(CreateTableQuery query) {
        Metainfo metainfo = metaStorage.getMetainfo();
        Table table = new Table(query.getName());
        List<Column> columns = query.getRows()
                    .stream()
                    .map(def -> new Column(def.getName(), def.getType()))
                    .collect(toList());
        table.addColumns(columns);
        metainfo.addTable(table);
        metaStorage.saveMetainfo(metainfo);
        return OkQueryResult.INSTANCE;
    }
}
