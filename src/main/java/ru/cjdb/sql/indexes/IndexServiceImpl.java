package ru.cjdb.sql.indexes;

import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.cursor.FullScanCursor;
import ru.cjdb.sql.cursor.btree.BTree;
import ru.cjdb.sql.cursor.btree.RowLink;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.storage.Constants;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;
import ru.cjdb.utils.IndexUtils;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

import static ru.cjdb.scheme.dto.Index.IndexType;
import static ru.cjdb.sql.expressions.BooleanExpression.TRUE_EXPRESSION;
import static ru.cjdb.storage.DiskPageUtils.metadataSize;
import static ru.cjdb.utils.IndexUtils.indexBytesPerRow;

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
        TreeBuilder prepare = new TreeBuilder(table, index, values).invoke();
        BTree tree = prepare.getTree();
        Comparable value = prepare.getValue();
        tree.add(value, new RowLink(pageId, rowId));
    }

    private void addHashIndexRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        int bucket = getBucket(table, index, values);
        DiskManager manager = diskManagerFactory.getForHashIndex(index.getFileName(bucket));
        DiskPage freePage = manager.getFreePage();
        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData());
        int bytesPerRow = indexBytesPerRow();

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

    @Override
    public void updateRow(Table table, Index index, int pageId, int rowId, Object[] values, Object[] newValues) {
        removeRow(table, index, pageId, rowId, values);
        addRow(table, index, pageId, rowId, newValues);
    }

    @Override
    public void removeRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        if (index.getType() == IndexType.HASH) {
            removeHashIndexRow(table, index, pageId, rowId, values);
        } else {
            removeBTreeIndexRow(table, index, pageId, rowId, values);
        }
    }

    private void removeBTreeIndexRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        TreeBuilder prepare = new TreeBuilder(table, index, values).invoke();
        BTree tree = prepare.getTree();
        Comparable value = prepare.getValue();
        tree.remove(value, new RowLink(pageId, rowId));
    }

    private void removeHashIndexRow(Table table, Index index, int pageId, int rowId, Object[] values) {
        int bucket = getBucket(table, index, values);

        DiskManager diskManager = diskManagerFactory.getForHashIndex(index.getFileName(bucket));
        List<Column> idXcolumns = IndexUtils.indexColumns();
        int bytesPerIdxRow = 2 * Integer.BYTES; //page, row
        FullScanCursor cursor = new FullScanCursor(idXcolumns, idXcolumns, TRUE_EXPRESSION, bytesPerIdxRow, diskManager);
        while (cursor.nextRow() != null) {
            DiskPage page = diskManager.getPage(cursor.currentPageId());
            ByteBuffer buffer = ByteBuffer.wrap(page.getData());
            BitSet freePagesBitSet = DiskPageUtils.getPageBitMask(metadataSize(indexBytesPerRow()), buffer);
            int currentRowId = cursor.currentRowId();
            freePagesBitSet.clear(currentRowId);
            DiskPageUtils.savePageBitMask(buffer, freePagesBitSet);
            page.setDirty(true);
        }
    }

    private int getBucket(Table table, Index index, Object[] values) {
        int bucket = -1;
        List<Column> columns = table.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (index.getColumns().stream().anyMatch(def -> def.getName().equals(column.getName()))) {
                bucket = values[i].hashCode() % index.getBucketCount();
            }
        }
        assert bucket != -1;
        return bucket;
    }

    private class TreeBuilder {
        private Table table;
        private Index index;
        private Object[] values;
        private Object value;
        private BTree tree;

        public TreeBuilder(Table table, Index index, Object... values) {
            this.table = table;
            this.index = index;
            this.values = values;
        }

        public Comparable getValue() {
            return (Comparable) value;
        }

        public BTree getTree() {
            return tree;
        }

        public TreeBuilder invoke() {
            int metadataSize = 2 * Integer.BYTES; // page_id, is_leaf
            int nextPageSize = Integer.BYTES;
            int elCountsize = Integer.BYTES;
            Object[] values = this.values;
            Column column = updateValue(values);
            Type type = column.getType();
            int maxLeafElementCount = (Constants.PAGE_SIZE - metadataSize - nextPageSize - elCountsize) / (type.bytes() + nextPageSize + nextPageSize);
            int maxNodeElementCount = (Constants.PAGE_SIZE - metadataSize - nextPageSize - elCountsize) / (type.bytes() + nextPageSize + nextPageSize);

            DiskManager manager = diskManagerFactory.getForBTreeIndex(index.getBTreeName());
            tree = new BTree(type, manager, maxNodeElementCount, maxLeafElementCount);
            return this;
        }

        public Column updateValue(Object[] values) {
            value = null;
            Column column = null;
            List<Column> columns = table.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Column col = columns.get(i);
                if (index.getColumns().stream().anyMatch(def -> def.getName().equals(col.getName()))) {
                    value = values[i];
                    column = col;
                }
            }
            return column;
        }
    }
}
