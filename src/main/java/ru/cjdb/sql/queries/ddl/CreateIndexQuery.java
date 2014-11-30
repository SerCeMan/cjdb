package ru.cjdb.sql.queries.ddl;

import ru.cjdb.sql.queries.Query;

import java.util.List;

import static ru.cjdb.scheme.dto.Index.IndexColumnDef;

/**
 * Query создания индекса
 *
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
public class CreateIndexQuery implements Query {
    private String name;
    private String table;
    private boolean unique;
    private List<IndexColumnDef> indexColDef;

    public CreateIndexQuery(String name, String table, boolean unique, List<IndexColumnDef> indexColDef) {
        this.name = name;
        this.table = table;
        this.unique = unique;
        this.indexColDef = indexColDef;
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

    public List<IndexColumnDef> getIndexColDef() {
        return indexColDef;
    }
}
