package ru.cjdb.sql.queries.dml;

import ru.cjdb.sql.queries.Query;

import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public class SelectQuery implements Query {
    private final String from;
    private final List<String> projections;

    public SelectQuery(String from, List<String> projections) {
        this.from = from;
        this.projections = projections;
    }

    public List<String> getProjections() {
        return projections;
    }

    public String getFrom() {
        return from;
    }
}
