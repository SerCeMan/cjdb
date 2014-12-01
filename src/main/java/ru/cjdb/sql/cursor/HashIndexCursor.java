package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;
import ru.cjdb.utils.IndexUtils;

import java.util.List;

import static ru.cjdb.sql.expressions.BooleanExpression.TRUE_EXPRESSION;

/**
 * Курсор пробегающий в соответствии с HASH индексом
 *
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public class HashIndexCursor implements Cursor {

    private final BooleanExpression condition;
    private final Cursor indexCursor;
    private final FullScanCursor tableCursor;

    public HashIndexCursor(DiskManagerFactory managerFactory, List<Column> allColumns, List<Column> columns, BooleanExpression condition,
                           int bytesPerRow, DiskManager manager, Index index, int hash) {
        this.condition = condition;

        int bucket = hash % index.getBucketCount();
        DiskManager indexDiskManager = managerFactory.getForIndex(index.getFileName(bucket));

        List<Column> idXcolumns = IndexUtils.indexColumns();
        int bytesPerIdxRow = 2 * Integer.BYTES; //page, row
        indexCursor = new FullScanCursor(idXcolumns, idXcolumns, TRUE_EXPRESSION, bytesPerIdxRow, indexDiskManager);
        tableCursor = new FullScanCursor(allColumns, columns, TRUE_EXPRESSION, bytesPerRow, manager);
    }

    @Override
    public Row nextRow() {
        Row idxRow;
        while ((idxRow = indexCursor.nextRow()) != null) {
            int pageId = (int) idxRow.getAt(0);
            int rowId = (int) idxRow.getAt(1);
            tableCursor.setNextPageId(pageId);
            tableCursor.setNextRowId(rowId);
            Row row = tableCursor.nextRow();
            if (condition.apply(row)) {
                return row;
            }
        }
        return null;
    }

    @Override
    public List<Type> types() {
        return tableCursor.types();
    }

    @Override
    public int currentPageId() {
        return tableCursor.currentPageId();
    }

    @Override
    public int currentRowId() {
        return tableCursor.currentRowId();
    }
}
