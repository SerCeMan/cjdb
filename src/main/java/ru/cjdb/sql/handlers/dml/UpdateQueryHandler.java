package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.cursor.FullScanCursor;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.indexes.IndexService;
import ru.cjdb.sql.queries.dml.UpdateQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.impl.RowAffectedQueryResult;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
@Singleton
public class UpdateQueryHandler extends RegisterableQueryHandler<UpdateQuery> {

    @Inject
    MetainfoService metainfoService;
    @Inject
    ConfigStorage configStorage;
    @Inject
    DiskManagerFactory diskManagerFactory;
    @Inject
    IndexService indexService;

    @Inject
    public UpdateQueryHandler() {
        super(UpdateQuery.class);
    }

    @Override
    public QueryResult execute(UpdateQuery query) {
        Table table = metainfoService.getTable(query.getTable());
        int bytesPerRow = metainfoService.bytesPerRow(table);
        int metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);

        DiskManager diskManager = diskManagerFactory.get(table.getName());

        List<Column> columns = table.getColumns()
                .stream()
                .filter(column -> query.getValues().containsKey(column.getName()))
                .collect(Collectors.toList());
        BooleanExpression condition = query.getCondition();

        Cursor cursor = new FullScanCursor(table.getColumns(), columns, condition, bytesPerRow, diskManager);
        List<Index> indexes = metainfoService.getIndexes(table);
        AtomicInteger rowsAffected = new AtomicInteger(0);
        cursor.forEach(row -> {
            DiskPage page = diskManager.getPage(cursor.currentPageId());
            ByteBuffer buffer = ByteBuffer.wrap(page.getData());

            int currentRowId = cursor.currentRowId();
            buffer.position(currentRowId * bytesPerRow + metaDataSize);
            for (Column column : table.getColumns()) {
                Type type = column.getType();
                if (columns.contains(column)) {
                    type.write(buffer, query.getValues().get(column.getName()));
                    page.setDirty(true);
                } else {
                    buffer.position(buffer.position() + type.bytes());
                }
            }
            for(Index index : indexes) {
                indexService.updateRow(table, index, page.getId(), currentRowId, row.values(), query.getValues().values().toArray());
            }
            rowsAffected.incrementAndGet();
        });
        return new RowAffectedQueryResult(rowsAffected.intValue());
    }
}
