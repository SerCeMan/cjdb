package ru.cjdb.sql.expressions;

import ru.cjdb.sql.result.Row;

/**
 * Какое-то значение - строка, число и т.д.
 *
 * @author Sergey Tselovalnikov
 * @since 05.11.14
 */
public class ValueExpression extends Expression {
    private final String value;

    public ValueExpression(String value) {
        this.value = value;
    }

    @Override
    public Comparable getValue(Row row) {
        return value;
    }
}
