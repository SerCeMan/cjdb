package ru.cjdb.sql.expressions;

import ru.cjdb.scheme.types.Type;

/**
 * Какое-то значение - строка, число и т.д.
 *
 * @author Sergey Tselovalnikov
 * @since 05.11.14
 */
public class ValueExpression extends Expression {
    private final Comparable value;
    private final Type type;

    public ValueExpression(Comparable value, Type type) {
        this.value = value;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Comparable getValue() {
        return value;
    }
}
