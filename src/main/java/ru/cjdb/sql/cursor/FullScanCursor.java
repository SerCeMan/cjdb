package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.sql.result.impl.RowImpl;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by serce on 01.12.14.
 */
public class FullScanCursor implements Cursor {
    private final List<Column> allColumns;
    private final List<Column> columns;
    private final BooleanExpression condition;
    private final int bytesPerRow;
    private final DiskManager manager;
    private final List<Type> types;
    private final int rowCount;
    private final int metaDataSize;
    private int nextRowId = 0;
    private int nextPageId = 0;

    public FullScanCursor(List<Column> allColumns, List<Column> columns, BooleanExpression condition,
                          int bytesPerRow, DiskManager manager) {
        this.allColumns = allColumns;
        this.columns = columns;
        this.condition = condition;
        this.bytesPerRow = bytesPerRow;
        this.manager = manager;
        this.types = columns
                .stream()
                .map(Column::getType)
                .collect(Collectors.toList());

        metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);
        rowCount = DiskPageUtils.calculateRowCount(bytesPerRow);
    }

    public void setNextRowId(int nextRowId) {
        this.nextRowId = nextRowId;
    }

    public void setNextPageId(int nextPageId) {
        this.nextPageId = nextPageId;
    }

    @Override
    public Row nextRow() {
        while (nextPageId < manager.pageCount()) {
            DiskPage page = manager.getPage(nextPageId);

            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            BitSet freePagesBitSet = DiskPageUtils.getPageBitMask(metaDataSize, buffer);

            for (; nextRowId < rowCount; nextRowId++) {
                boolean busy = freePagesBitSet.get(nextRowId);
                if (busy) {
                    RowImpl row = buildRow(buffer, nextRowId);
                    if (condition.apply(row)) {
                        nextRowId++;
                        return row;
                    }
                }
            }
            nextPageId++;
            nextRowId = 0;
        }
        return null;
    }

    private RowImpl buildRow(ByteBuffer buffer, int rowId) {
        buffer.position(rowId * bytesPerRow + metaDataSize);

        Object[] objects = new Object[allColumns.size()];
        int j = 0; // result count
        for (Column column : allColumns) {
            Type type = column.getType();
            if (columns.contains(column)) {
                // Колонка в запросе
                Object result = type.read(buffer);
                objects[j++] = result;
            } else {
                buffer.position(buffer.position() + type.bytes());
            }
        }
        return new RowImpl(columns, objects);
    }

    @Override
    public List<Type> types() {
        return types;
    }

    @Override
    public int currentPageId() {
        return nextPageId;
    }

    @Override
    public int currentRowId() {
        return nextRowId - 1;
    }
}
