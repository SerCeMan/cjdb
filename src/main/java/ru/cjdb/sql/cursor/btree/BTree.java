package ru.cjdb.sql.cursor.btree;

import com.google.common.base.Preconditions;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.fs.DiskManager;

import java.util.HashSet;
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
        BTreeNode root = getRoot();
        if (root == null) {
            root = createLeaf();
        }
        addToNode(root, value, rowLink);
    }

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
        BTreeNode newNode;
        Comparable median;
        if (node.isLeaf()) {
            newNode = createLeaf();
            node.setNextNode(newNode);
        } else {
            newNode = createNode();
        }
        median = copyOneHalf(node, newNode);
        BTreeNode parent = getParent(node);
        if (parent == null) {
            parent = createNode();
            node.setParent(parent);
        }
        newNode.setParent(parent);
        parent.add(node, newNode, median);
        if (parent.isAlmostFull()) {
            splitNode(parent);
        }
        parent.save();
        return newNode;
    }

    private BTreeNode createNode() {
        DiskPage page = indexDiskManager.getFreePage();
        return BTreeNode.createEmptyNode(page, this);
    }

    private BTreeNode createLeaf() {
        DiskPage page = indexDiskManager.getFreePage();
        return BTreeNode.createEmptyLeaf(page, this);
    }

    private Comparable copyOneHalf(BTreeNode node, BTreeNode newNode) {
        if (node.isLeaf() != newNode.isLeaf()) {
            throw new IllegalStateException("Node should be same leaf state");
        }
        List<Comparable> values = node.getValues();
        int medianNum = values.size() / 2;

        node.setValues(values.subList(0, medianNum));
        newNode.setValues(values.subList(medianNum, values.size()));

        if (node.isLeaf()) {
            List<RowLink> rowLinks = node.getRowLinks();
            node.setRowLinks(rowLinks.subList(0, medianNum));
            newNode.setRowLinks(rowLinks.subList(medianNum, rowLinks.size()));
            return values.get(medianNum);
        } else {
            List<Integer> childsIds = node.getChildsIds();
            node.setChildsIds(childsIds.subList(0, medianNum + 1));
            newNode.setChildsIds(childsIds.subList(medianNum + 1, childsIds.size()));
            return values.get(medianNum);
        }
    }

    private BTreeNode getParent(BTreeNode node) {
        if (node.getParentId() == 0) {
            return null;
        }
        BTreeNode parent = getNode(node.getParentId());
        Preconditions.checkArgument(!parent.isLeaf(), "Parent can't be leaf");
        return parent;
    }

    private BTreeNode findChild(BTreeNode node, Comparable value) {
        Preconditions.checkArgument(!node.isLeaf(), "Node should be leaf");
        List<Comparable> values = node.getValues();
        for (int i = 0; i < values.size(); i++) {
            if (value.compareTo(values.get(i)) < 0) {
                return getNode(node.getChild(i));
            }
        }
        return getNode(node.getChild(values.size()));
    }

    private BTreeNode getNode(int nodeId) {
        if (nodeId >= indexDiskManager.pageCount()) {
            return null;
        }
        DiskPage page = indexDiskManager.getPage(nodeId);
        return BTreeNode.load(page, this);
    }

    private BTreeNode getRoot() {
        BTreeNode node = getNode(0);
        if (node == null) {
            return null;
        }
        while (true) {
            BTreeNode parent = getParent(node);
            if (parent == null) {
                return node;
            }
            node = parent;
        }
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

    public void remove(Comparable value, RowLink rowLink) {
        remove(getRoot(), value, rowLink);
    }

    private void remove(BTreeNode node, Comparable value, RowLink rowLink) {
        if (node.isLeaf()) {
            boolean finish = node.getValues().isEmpty();
            HashSet<Integer> toRemove = new HashSet<>();
            List<Comparable> values = node.getValues();
            for (int i = 0; i < values.size(); i++) {
                Comparable val = values.get(i);
                if (value.compareTo(val) > 0) {
                    finish = true;
                }
                if (value.equals(val) && rowLink.equals(node.getRowLinks().get(i))) {
                    toRemove.add(i);
                }
            }
            toRemove.forEach(node::removeElement);
            node.save();
            if (!finish && node.getNextNodeId() != 0) {
                remove(getNode(node.getNextNodeId()), value, rowLink);
            }
        } else {
            remove(findChild(node, value), value, rowLink);
        }
    }
}
