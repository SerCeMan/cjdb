package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.types.Type;
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
    private final int bytesPerRow;
    private final DiskManager manager;
    private final List<Type> types;
    private final int rowCount;
    private final int metaDataSize;

    private int nextRowId = 0;
    private int nextPageId = 0;

    public Cursor(int bytesPerRow, DiskManager manager, List<Type> types) {
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
                    buffer.position(nextRowId * bytesPerRow + metaDataSize);

                    Object[] objects = new Object[types.size()];
                    for (int i = 0; i < types.size(); i++) {
                        Type type = types.get(i);
                        objects[i] = type.read(buffer);
                    }

                    nextRowId++;
                    return new RowImpl(objects);
                }
            }
            nextPageId++;
            nextRowId=0;
        }
        return null;
    }
}
