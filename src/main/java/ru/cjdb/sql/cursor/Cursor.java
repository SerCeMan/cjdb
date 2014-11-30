package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.sql.result.impl.RowImpl;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

/**
 * Курсор, пробегающийся по таблице и возвращающий результат
 *
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class Cursor {
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

    public Cursor(Table table, List<Column> columns, BooleanExpression condition,
                  int bytesPerRow, DiskManager manager, List<Type> types) {
        this.allColumns = table.getColumns();
        this.columns = columns;
        this.condition = condition;
        this.bytesPerRow = bytesPerRow;
        this.manager = manager;
        this.types = types;
        metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);
        rowCount = DiskPageUtils.calculateRowCount(bytesPerRow);
    }

    public Row nextRow() {
        while (nextPageId < manager.pageCount()) {
            DiskPage page = manager.getPage(nextPageId);

            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            BitSet freePagesBitSet = DiskPageUtils.getPageBitMask(metaDataSize, buffer);

            for (; nextRowId < rowCount; nextRowId++) {
                boolean busy = freePagesBitSet.get(nextRowId);
                if (busy) {
                    RowImpl row = buildRow(buffer);
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

    private RowImpl buildRow(ByteBuffer buffer) {
        buffer.position(nextRowId * bytesPerRow + metaDataSize);

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

    public List<Type> types() {
        return types;
    }

    public int currentPageId() {
        return nextPageId;
    }

    public int currentRowId() {
        return nextRowId - 1;
    }
}
