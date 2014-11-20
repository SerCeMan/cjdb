package ru.cjdb.sql.expressions;

/**
 * Какое-то выражение SQL
 *
 * @author Sergey Tselovalnikov
 * @since 31.10.14
 */
public abstract class Expression {
    public abstract Comparable getValue();
}
