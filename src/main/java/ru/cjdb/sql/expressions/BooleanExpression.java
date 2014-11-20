package ru.cjdb.sql.expressions;

/**
 * @author Sergey Tselovalnikov
 * @since 21.11.14
 */
public abstract class BooleanExpression extends Expression {
    public boolean getBooleanValue() {
        return (boolean)getValue();
    }
}
