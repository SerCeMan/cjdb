package ru.cjdb.sql.indexes;

import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.cursor.FullScanCursor;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.storage.Constants;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;
import ru.cjdb.utils.IndexUtils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static ru.cjdb.scheme.dto.Index.IndexType;

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
        if (index.getType() == IndexType.HASH) {
            addHashIndexRow(table, index, pageId, rowId, values);
        } else {
            addBTreeIndexRow(table, index, pageId, rowId, values);
        }
    }

    private void addBTreeIndexRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        DiskManager manager = diskManagerFactory.getForHashIndex(index.getTable() + "_" + index.getName() + "_btree");
        int metadataSize = 2 * Integer.BYTES; // page_id, is_leaf
        int nextPageSize = Integer.BYTES;
        int elCountsize = Integer.BYTES;
        Column column = null;
        Object value = null;
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            if (index.getColumns().stream().anyMatch(def -> def.getName().equals(col.getName()))) {
                value = values[i];
                column = col;
            }
        }
        Type type = column.getType();
        int maxLeafElementCount = (Constants.PAGE_SIZE - metadataSize - nextPageSize - elCountsize) / (type.bytes() + nextPageSize + nextPageSize);
        int maxNodeElementCount = (Constants.PAGE_SIZE - metadataSize - nextPageSize - elCountsize) / (type.bytes() + nextPageSize + nextPageSize);

        DiskPage root = manager.getFreePage();
        if (manager.pageCount() == 1) {
            //init
            DiskPage leftLeaf = manager.getFreePage();
            DiskPage rightLeaf = manager.getFreePage();

            ByteBuffer rootBb = ByteBuffer.wrap(root.getData());
            rootBb.position(Integer.BYTES);
            rootBb.put((byte) 1);
            rootBb.putInt(1);

            rootBb.putInt(leftLeaf.getId());
            type.write(rootBb, value);
            rootBb.putInt(rightLeaf.getId());
        } else {
            DiskPage leaf = getLeaf(root, type, (Comparable) value, manager);
            ByteBuffer pageBb = ByteBuffer.wrap(leaf.getData());
            pageBb.position(Integer.BYTES + 1);
        }
    }

    private DiskPage getLeaf(DiskPage page, Type type, Comparable eqValue, DiskManager manager) {
        ByteBuffer pageBb = ByteBuffer.wrap(page.getData());
        pageBb.position(Integer.BYTES);
        boolean isLeaf = pageBb.get() != 0;
        if (isLeaf) {
            return page;
        }
        int elCount = pageBb.getInt();
        for (int i = 0; i < elCount; i++) {
            int pageId = pageBb.getInt();
            Comparable value = type.read(pageBb);
            if (eqValue.compareTo(value) <= 0) {
                return getLeaf(manager.getPage(pageId), type, eqValue, manager);
            }
        }
        int pageId = pageBb.getInt();
        return getLeaf(manager.getPage(pageId), type, eqValue, manager);
    }

    private void addHashIndexRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        int bucket = -1;
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (index.getColumns().stream().anyMatch(def -> def.getName().equals(column.getName()))) {
                bucket = values[i].hashCode() % index.getBucketCount();
            }
        }
        assert bucket != -1;

        DiskManager manager = diskManagerFactory.getForHashIndex(index.getFileName(bucket));
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
