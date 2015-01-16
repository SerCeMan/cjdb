package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.fs.DiskManager;

import java.nio.ByteBuffer;
import java.util.List;

import static ru.cjdb.sql.expressions.BooleanExpression.TRUE_EXPRESSION;

/**
 * Структура данных в дереве (is_leaf = false):
 * [next_free][parent_id][is_leaf][el_count] [next_page][value][next_page]...
 * <p>
 * <p>
 * Структура данных в листе (is_leaf = true):
 * [next_free][parent_id][is_leaf][el_count] [value,page_id,row_id]...[next_right]
 *
 * @author Sergey Tselovalnikov
 * @since 21.12.14
 */
public class BTreeCursor implements Cursor {

    private final FullScanCursor tableCursor;
    private final DiskManager diskManager;
    private final BooleanExpression condition;
    private final Comparable eqValue;
    private final Type type;

    private int nextPageId = 0;
    private int currentElement = 0;

    public BTreeCursor(DiskManager diskManager, List<Column> allColumns, List<Column> columns, BooleanExpression condition,
                       int bytesPerRow, DiskManager manager, Comparable eqValue, Type type) {
        this.diskManager = diskManager;
        this.eqValue = eqValue;
        this.condition = condition;
        this.type = type;

        tableCursor = new FullScanCursor(allColumns, columns, TRUE_EXPRESSION, bytesPerRow, manager);

        nextPageId = getRootPageId(nextPageId);
        initRecursive();
    }

    private int getRootPageId(int nextPageId) {
        DiskPage idxPage = diskManager.getPage(nextPageId);
        ByteBuffer buffer = ByteBuffer.wrap(idxPage.getData());
        buffer.position(Integer.BYTES);
        int parent = buffer.getInt();
        if (parent == 0) {
            return nextPageId;
        }
        return getRootPageId(parent);
    }

    public void initRecursive() {
        DiskPage idxPage = diskManager.getPage(nextPageId);
        ByteBuffer idxBuf = ByteBuffer.wrap(idxPage.getData());
        idxBuf.position(2 * Integer.BYTES);
        boolean isLeaf = idxBuf.get() != 0;
        int nodeElementCount = idxBuf.getInt();
        if (!isLeaf) {
            for (int i = 0; i < nodeElementCount; i++) {
                int nextPage = idxBuf.getInt();
                Comparable<?> value = type.read(idxBuf);
                if (eqValue.compareTo(value) <= 0) {
                    nextPageId = nextPage;
                    initRecursive();
                    return;
                }
            }
            nextPageId = idxBuf.getInt();
            initRecursive();
        }
    }


    @Override
    public Row nextRow() {
        DiskPage idxPage = diskManager.getPage(nextPageId);
        ByteBuffer idxBuf = ByteBuffer.wrap(idxPage.getData());
        idxBuf.position(2 * Integer.BYTES + 1);
        int leafElementCount = idxBuf.getInt();

        int metaDataSize = Integer.BYTES * 3 + 1;
        idxBuf.position(metaDataSize + currentElement * (type.bytes() + Integer.BYTES + Integer.BYTES));
        if (currentElement == leafElementCount) {
            nextPageId = idxBuf.getInt();
            if (nextPageId == 0) {
                return null;
            }
            currentElement = 0;
            return nextRow();
        }
        Comparable<?> value = type.read(idxBuf);
        int compResult = eqValue.compareTo(value);
        if (compResult < 0) {
            // не дошли до нужного элемента
            currentElement++;
            return nextRow();
        }
        if (compResult > 0) {
            // нужные элементы кончились
            return null;
        }
        tableCursor.setNextPageId(idxBuf.getInt());
        tableCursor.setNextRowId(idxBuf.getInt());
        currentElement++;
        Row row = tableCursor.nextRow();
        if (!condition.apply(row)) {
            return nextRow();
        }
        return row;
    }

    @Override
    public List<Type> types() {
        return tableCursor.types();
    }

    @Override
    public int currentPageId() {
        return 0;
    }

    @Override
    public int currentRowId() {
        return 0;
    }
}
