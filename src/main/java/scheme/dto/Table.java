package scheme.dto;

import javafx.scene.control.Tab;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@XmlRootElement(name = "Table")
public class Table {

    @XmlElement(name = "column")
    private List<Column> columns = new ArrayList<>();
    @XmlAttribute(name = "name")
    private String name;

    // для JAXB
    Table() {
    }

    public Table(String name) {
        this.name = name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getName() {
        return name;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public void addColumns(Collection<Column> columns) {
        this.columns.addAll(columns);
    }

    public void addColumns(Column... columns) {
        this.columns.addAll(asList(columns));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Table table = (Table) o;
        return columns.equals(table.columns) && name.equals(table.name);

    }

    @Override
    public int hashCode() {
        int result = columns.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
