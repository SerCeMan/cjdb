package ru.cjdb.scheme.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Вся метаинформация БД
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@XmlRootElement(name = "Metainfo")
public class Metainfo {

    @XmlElementWrapper(name = "tables")
    @XmlElement(name = "table")
    private List<Table> tables = new ArrayList<>();

    @XmlElementWrapper(name = "indexes")
    @XmlElement(name = "index")
    private List<Index> indexes = new ArrayList<>();

    public List<Table> getTables() {
        return tables;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public void addIndexes(Index... indexes) {
        this.indexes.addAll(asList(indexes));
    }

    public void addTable(Table table) {
        tables.add(table);
    }

    public void addTables(Table... tables) {
        this.tables.addAll(asList(tables));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Metainfo metainfo = (Metainfo) o;
        return tables.equals(metainfo.tables);

    }

    @Override
    public int hashCode() {
        return tables.hashCode();
    }
}
