package ru.cjdb.sql.queries.dml;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.queries.Query;

import java.util.Map;

/**
 * UPDATE
 *
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public class UpdateQuery implements Query {
    private final String table;
    private final Map<String, Object> values;
    private final BooleanExpression condition;

    public UpdateQuery(String table, Map<String, Object> values, BooleanExpression condition) {
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
