package sql.handlers.ddl;

import scheme.MetaStorage;
import scheme.dto.Column;
import scheme.dto.Metainfo;
import scheme.dto.Table;
import sql.QueryExecutor;
import sql.handlers.RegisterableQueryHandler;
import sql.queries.ddl.CreateTableQuery;
import sql.result.OkQueryResult;
import sql.result.QueryResult;

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
