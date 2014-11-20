package ru.cjdb.sql.queries.dml;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.conditions.ConditionOrAnd;
import ru.cjdb.sql.queries.Query;

import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public class SelectQuery implements Query {
    private String from;
    private List<String> projections;
    private BooleanExpression condition;

    public SelectQuery() {
    }

    public SelectQuery(String from, List<String> projections, ConditionOrAnd condition) {
        this(from, projections);
        this.from = from;
    }

    public SelectQuery(String from, List<String> projections) {
        this.from = from;
        this.projections = projections;
    }

    public BooleanExpression getCondition() {
        return condition;
    }

    public List<String> getProjections() {
        return projections;
    }

    public String getFrom() {
        return from;
    }
}
