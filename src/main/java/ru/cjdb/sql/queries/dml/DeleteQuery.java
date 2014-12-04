package ru.cjdb.sql.queries.dml;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.queries.Query;

import java.util.Map;

/**
 * Created by flire on 05.12.14.
 */
public class DeleteQuery implements Query {
    private final String table;
    private final Map<String, Object> values;
    private final BooleanExpression condition;

    public DeleteQuery(String table, Map<String, Object> values, BooleanExpression condition) {
        this.condition = condition;
        this.table = table;
        this.values = values;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public BooleanExpression getCondition() {
        return condition;
    }
}
