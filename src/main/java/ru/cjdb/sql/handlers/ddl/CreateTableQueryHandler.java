package ru.cjdb.sql.handlers.ddl;

import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.result.impl.OkQueryResult;
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
    MetainfoService metainfoService;

    @Inject
    public CreateTableQueryHandler() {
        super(CreateTableQuery.class);
    }

    @Override
    public QueryResult execute(CreateTableQuery query) {
        Table table = new Table(query.getName());
        List<Column> columns = query.getRows()
                    .stream()
                    .map(def -> new Column(def.getName(), def.getType()))
                    .collect(toList());
        table.addColumns(columns);
        metainfoService.addTable(table);
        return OkQueryResult.INSTANCE;
    }
}
