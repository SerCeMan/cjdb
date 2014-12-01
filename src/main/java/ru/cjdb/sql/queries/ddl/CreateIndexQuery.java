package ru.cjdb.sql.queries.ddl;

import ru.cjdb.scheme.dto.Index;
import ru.cjdb.sql.queries.Query;

import java.util.List;

import static ru.cjdb.scheme.dto.Index.IndexColumnDef;
import static ru.cjdb.scheme.dto.Index.IndexType;

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
    private IndexType indexType;
    private List<IndexColumnDef> indexColDef;

    public CreateIndexQuery(String name, String table, boolean unique, IndexType indexType, List<IndexColumnDef> indexColDef) {
        this.name = name;
        this.table = table;
        this.unique = unique;
        this.indexType = indexType;
        this.indexColDef = indexColDef;
    }

    public IndexType getIndexType() {
        return indexType;
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
