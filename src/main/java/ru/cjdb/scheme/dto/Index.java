package ru.cjdb.scheme.dto;

import ru.cjdb.scheme.dto.xml.IndexTypeAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Индекс в базе данных
 *
 * TODO подклассы
 *
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
@XmlRootElement(name = "Index")
public class Index {
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "table")
    private String table;
    @XmlAttribute(name = "unique")
    private boolean unique;
    @XmlAttribute(name = "type")
    private IndexType type;
    @XmlAttribute(name = "buckets")
    private int bucketCount;
    @XmlElement(name = "column")
    private List<IndexColumnDef> columns = new ArrayList<>();

    // JAXB
    Index() {
    }

    public Index(String name, String table, boolean unique, IndexType type, List<IndexColumnDef> columns) {
        this.unique = unique;
        this.name = name;
        this.table = table;
        this.type = type;
        this.columns = columns;
        this.bucketCount = 20;
    }

    public String getName() {
        return name;
    }

    public String getTable() {
        return table;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<IndexColumnDef> getColumns() {
        return columns;
    }

    public int getBucketCount() {
        return bucketCount;
    }

    public IndexType getType() {
        return type;
    }

    public String getFileName(int bucket) {
        return getName() + "_" + bucket;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, table, type, unique, columns);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Index index = (Index) o;
        return unique == index.unique &&
                columns.equals(index.columns) &&
                name.equals(index.name) &&
                type.equals(index.type) &&
                table.equals(index.table);

    }

    public String getBTreeName() {
        return getTable() + "_" + getName() + "_btree";
    }

    @XmlJavaTypeAdapter(IndexTypeAdapter.class)
    public static enum IndexType {
        HASH,
        BTREE
    }

    public static class IndexColumnDef {
        @XmlAttribute(name = "name")
        private String name;
        @XmlAttribute(name = "order")
        private Order order = Order.ASC;

        // JAXB
        IndexColumnDef() {
        }

        public IndexColumnDef(String name, Order order) {
            this.name = name;
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public Order getOrder() {
            return order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            IndexColumnDef that = (IndexColumnDef) o;
            return name.equals(that.name) && order == that.order;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, order);
        }
    }
}
