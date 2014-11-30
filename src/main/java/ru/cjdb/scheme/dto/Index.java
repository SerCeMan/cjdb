package ru.cjdb.scheme.dto;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Индекс в базе данных
 *
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
@XmlRootElement(name = "Index")
public class Index {

    @XmlAttribute(name = "unique")
    private boolean unique;
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "table")
    private String table;
    @XmlElement(name = "column")
    private List<IndexColumnDef> columns = new ArrayList<>();

    // JAXB
    Index() {
    }

    public Index(String name, String table, boolean unique, List<IndexColumnDef> columns) {
        this.unique = unique;
        this.name = name;
        this.table = table;
        this.columns = columns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, table, unique, columns);
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
                table.equals(index.table);

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
