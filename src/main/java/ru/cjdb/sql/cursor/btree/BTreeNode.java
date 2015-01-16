package ru.cjdb.sql.cursor.btree;

import ru.cjdb.scheme.types.Type;
import ru.cjdb.storage.DiskPage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 16.01.15
 */
public class BTreeNode {
    private final DiskPage page;
    private final ByteBuffer buffer;
    private final int pageId;

    private boolean isLeaf;
    private Type valType;
    private int nextNodeId = 0;

    private List<Comparable> values = new ArrayList<>();
    private List<Integer> childsIds;
    private List<RowLink> rowLinks;
    private boolean dirty = false;
    private int maxElCount;

    private BTreeNode(DiskPage page, BTree tree) {
        this.page = page;
        pageId = page.getId();
        buffer = ByteBuffer.wrap(page.getData());
        valType = tree.valType();
    }

    public static BTreeNode load(DiskPage page, BTree tree) {
        BTreeNode node = new BTreeNode(page, tree);
        ByteBuffer idxBuf = node.buffer;
        idxBuf.position(Integer.BYTES);
        node.isLeaf = idxBuf.get() != 0;
        int elementsCount = idxBuf.getInt();
        Type type = node.valType;
        if (node.isLeaf) {
            node.maxElCount = tree.maxLeafElementCount();
            node.rowLinks = new ArrayList<>();
            for (int i = 0; i < elementsCount; i++) {
                Comparable value = type.read(idxBuf);
                int pageId = idxBuf.getInt();
                int rowId = idxBuf.getInt();
                node.values.add(value);
                node.rowLinks.add(new RowLink(pageId, rowId));
            }
            node.nextNodeId = idxBuf.getInt();
        } else {
            node.maxElCount = tree.maxNodeElementCount();
            node.childsIds = new ArrayList<>();
            for (int i = 0; i < elementsCount; i++) {
                int childLink = idxBuf.getInt();
                Comparable value = type.read(idxBuf);
                node.childsIds.add(childLink);
                node.values.add(value);
            }
            node.childsIds.add(idxBuf.getInt());
        }
        return node;
    }

    public static BTreeNode createEmptyLeaf(DiskPage page, BTree tree) {
        BTreeNode node = new BTreeNode(page, tree);
        node.maxElCount = tree.maxLeafElementCount();
        node.isLeaf = true;
        node.rowLinks = new ArrayList<>();
        node.setDirty();
        return node;
    }

    public void save() {
        if (!dirty)
            return;
        buffer.position(0);
        buffer.putInt(pageId);
        buffer.put((byte) (isLeaf ? 1 : 0));
        int elemCount = getElementCount();
        buffer.putInt(elemCount);
        if (isLeaf) {
            for (int i = 0; i < elemCount; i++) {
                valType.write(buffer, values.get(i));
                RowLink link = rowLinks.get(i);
                buffer.putInt(link.getPageId());
                buffer.putInt(link.getRowId());
            }
            buffer.putInt(nextNodeId);
        } else {
            for (int i = 0; i < elemCount; i++) {
                buffer.putInt(childsIds.get(i));
                valType.write(buffer, values.get(i));
            }
            buffer.putInt(childsIds.get(elemCount));
        }
    }

    private void setDirty() {
        page.setDirty(true);
        dirty = true;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public boolean isAlmostFull() {
        return getElementCount() == maxElCount - 1;
    }

    public void add(Comparable value, RowLink rowLink) {
        values.add(value);
        rowLinks.add(rowLink);
        setDirty();
    }

    public List<Comparable> getValues() {
        return values;
    }

    public int getChild(int i) {
        return childsIds.get(i);
    }

    public int getElementCount() {
        return values.size();
    }
}
