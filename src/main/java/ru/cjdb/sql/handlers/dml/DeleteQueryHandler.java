package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.cursor.FullScanCursor;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.DeleteQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.impl.RowAffectedQueryResult;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by flire on 05.12.14.
 */
public class DeleteQueryHandler extends RegisterableQueryHandler<DeleteQuery> {

    @Inject
    MetainfoService metainfoService;
    @Inject
    ConfigStorage configStorage;
    @Inject
    DiskManagerFactory diskManagerFactory;

    @Inject
    public DeleteQueryHandler() {
        super(DeleteQuery.class);
    }

    @Override
    public QueryResult execute(DeleteQuery query) {
        Table table = metainfoService.getTable(query.getTable());
        int bytesPerRow = metainfoService.bytesPerRow(table);
        int metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);

        DiskManager diskManager = diskManagerFactory.get(table.getName());

        List<Column> columns = table.getColumns();
        BooleanExpression condition = query.getCondition();

        Cursor cursor = new FullScanCursor(table.getColumns(), columns, condition, bytesPerRow, diskManager);
        int rowsAffected = 0;
        while (cursor.nextRow() != null) {
            DiskPage page = diskManager.getPage(cursor.currentPageId());
            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            BitSet freePagesBitSet = DiskPageUtils.getPageBitMask(metaDataSize, buffer);

            int currentRowId = cursor.currentRowId();
            freePagesBitSet.clear(currentRowId);
            DiskPageUtils.savePageBitMask(buffer, freePagesBitSet);
            page.setDirty(true);
            rowsAffected++;
        }
        return new RowAffectedQueryResult(rowsAffected);
    }
}
