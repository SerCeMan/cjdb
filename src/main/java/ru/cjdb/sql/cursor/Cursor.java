package ru.cjdb.sql.cursor;

import javafx.scene.control.Tab;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.result.Row;
import ru.cjdb.storage.fs.*;

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
    private final int rowCount;

    private int nextRowId = 0;
    private int nextPageId = 0;

    public Cursor(int bytesPerRow, DiskManager manager) {
        this.bytesPerRow = bytesPerRow;
        this.manager = manager;
        rowCount = DiskPageUtils.calculateRowCount(bytesPerRow);
    }

    public Row nextRow() {
        while (nextPageId < manager.pageCount()) {
            DiskPage page = manager.getPage(nextPageId);

            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            buffer.position(Integer.BYTES); // пропускаем ссылку на другую страничку

            BitSet freePagesBitSet = BitSet.valueOf(buffer);
            for (; nextRowId < rowCount; nextRowId++) {
                boolean busy = freePagesBitSet.get(nextRowId);
                if (busy) {
                    return new Row() {
                        @Override
                        public int getColumnCount() {
                            return 1;
                        }

                        @Override
                        public Object getAt(int columnNumber) {
                            buffer.position(nextRowId * bytesPerRow + Integer.BYTES);
                            return null;
                        }

                        @Override
                        public List<Object> values() {
                            return null;
                        }
                    };
                }
            }
            nextPageId++;
            nextRowId=0;
        }
        return null;
    }
}
