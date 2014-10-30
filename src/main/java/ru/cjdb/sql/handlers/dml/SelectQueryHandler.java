package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
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
        Cursor cursor = new Cursor(bytesPerRow, diskManager, types);
        return new SelectQueryResult(cursor);
    }
}
