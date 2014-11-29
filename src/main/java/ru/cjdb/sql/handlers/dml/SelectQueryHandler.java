package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;
import ru.cjdb.sql.result.impl.SelectQueryResult;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Singleton
public class SelectQueryHandler extends RegisterableQueryHandler<SelectQuery> {

    @Inject
    MetainfoService metainfoService;
    @Inject
    ConfigStorage configStorage;
    @Inject
    DiskManagerFactory diskManagerFactory;

    @Inject
    public SelectQueryHandler() {
        super(SelectQuery.class);
    }

    @Override
    public QueryResult execute(SelectQuery query) {
        Table table = metainfoService.getTable(query.getFrom());
        int bytesPerRow = metainfoService.bytesPerRow(table);

        DiskManager diskManager = diskManagerFactory.get(table.getName());
        List<Type> types = metainfoService.getColumnTypes(table);

        List<Column> columns = table.getColumns()
                .stream()
                .filter(column -> query.getProjections().contains(column.getName()))
                .collect(Collectors.toList());
        BooleanExpression condition = query.getCondition();

        Cursor cursor = new Cursor(columns, condition, bytesPerRow, diskManager, types);
        return new SelectQueryResult(cursor);
    }
}
