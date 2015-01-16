package ru.cjdb.sql.cursor.btree;

import com.google.common.base.Preconditions;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.fs.DiskManager;

import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 17.01.15
 */
public class BTree {

    private final int maxLeafElementCount;
    private final int maxNodeElementCount;
    private final DiskManager indexDiskManager;
    private final Type valType;

    public BTree(Type valType, DiskManager indexDiskManager, int maxNodeElementCount, int maxLeafElementCount) {
        this.valType = valType;
        this.indexDiskManager = indexDiskManager;
        this.maxNodeElementCount = maxNodeElementCount;
        this.maxLeafElementCount = maxLeafElementCount;
    }

    public void add(Comparable value, RowLink rowLink) {
        BTreeNode root = getNode(0);
        if (root == null) {
            root = createLeaf();
        }
        addToNode(root, value, rowLink);
    }

    // private

    private void addToNode(BTreeNode node, Comparable value, RowLink rowLink) {
        if (node.isLeaf()) {
            node.add(value, rowLink);
            if (node.isAlmostFull()) {
                BTreeNode newNode = splitNode(node);
                newNode.save();
            }
            node.save();
        } else {
            addToNode(findChild(node, value), value, rowLink);
        }
    }

    private BTreeNode splitNode(BTreeNode node) {
        BTreeNode newNode = null;
        return newNode;
    }

    private BTreeNode findChild(BTreeNode node, Comparable value) {
        Preconditions.checkArgument(!node.isLeaf(), "Node should have childs");
        List<Comparable> values = node.getValues();
        for (int i = 0; i < values.size(); i++) {
            if (value.compareTo(values.get(i)) < 0) {
                return getNode(node.getChild(i));
            }
        }
        return getNode(node.getChild(values.size()));
    }

    private BTreeNode createLeaf() {
        DiskPage page = indexDiskManager.getFreePage();
        return BTreeNode.createEmptyLeaf(page, this);
    }

    private BTreeNode getNode(int nodeId) {
        if (nodeId >= indexDiskManager.pageCount()) {
            return null;
        }
        DiskPage page = indexDiskManager.getPage(nodeId);

        BTreeNode node = BTreeNode.load(page, this);
        return node;
    }

    Type valType() {
        return valType;
    }

    int maxLeafElementCount() {
        return maxLeafElementCount;
    }

    int maxNodeElementCount() {
        return maxNodeElementCount;
    }
}
