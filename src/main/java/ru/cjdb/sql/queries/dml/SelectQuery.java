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
    private String joinTable;
    private BooleanExpression joinExpression;

    public SelectQuery() {
    }

    public SelectQuery(String from, List<String> projections, BooleanExpression condition) {
        this.from = from;
        this.projections = projections;
        this.condition = condition;
    }

    public SelectQuery(String from, List<String> projections) {
        this(from, projections, BooleanExpression.TRUE_EXPRESSION);
    }

    public BooleanExpression getCondition() {
        return condition;
    }

    public List<String> getProjections() {
        return projections;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(String joinTable) {
        this.joinTable = joinTable;
    }

    public BooleanExpression getJoinExpression() {
        return joinExpression;
    }

    public void setJoinExpression(BooleanExpression joinExpression) {
        this.joinExpression = joinExpression;
    }

    public String getFrom() {
        return from;
    }

    public boolean hasJoin() {
        return getJoinTable() != null;
    }
}
