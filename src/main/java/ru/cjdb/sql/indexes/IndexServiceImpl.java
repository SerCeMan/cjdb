package ru.cjdb.sql.indexes;

import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.cursor.FullScanCursor;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;
import ru.cjdb.utils.IndexUtils;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public class IndexServiceImpl implements IndexService {

    private final DiskManagerFactory diskManagerFactory;
    private final MetainfoService metainfoService;

    public IndexServiceImpl(DiskManagerFactory diskManagerFactory, MetainfoService metainfoService) {
        this.diskManagerFactory = diskManagerFactory;
        this.metainfoService = metainfoService;
    }

    @Override
    public void addRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        int bucket = -1;
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (index.getColumns().stream().anyMatch(def -> def.getName().equals(column.getName()))) {
                bucket = values[i].hashCode() % index.getBucketCount();
            }
        }
        assert bucket != -1;

        DiskManager manager = diskManagerFactory.getForIndex(index.getFileName(bucket));
        DiskPage freePage = manager.getFreePage();


        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData());
        int bytesPerRow = IndexUtils.indexBytesPerRow();

        int freeRowOffset = DiskPageUtils.calculateFreeRowOffset(buffer, bytesPerRow);

        buffer.position(freeRowOffset);
        buffer.putInt(pageId);
        buffer.putInt(rowId);

        freePage.setDirty(true);
    }

    @Override
    public void createIndex(Table table, Index index) {
        int bytesPerRow = metainfoService.bytesPerRow(table);
        List<Column> columns = table.getColumns();
        Cursor cursor = new FullScanCursor(columns, columns, BooleanExpression.TRUE_EXPRESSION,
                bytesPerRow, diskManagerFactory.get(table.getName()));
        cursor.forEach(row -> addRow(table, index, cursor.currentPageId(), cursor.currentRowId(), row.values()));
    }
}
