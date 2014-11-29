package ru.cjdb.sql.expressions;

import ru.cjdb.sql.result.Row;

/**
 * @author Sergey Tselovalnikov
 * @since 21.11.14
 */
public abstract class BooleanExpression extends Expression {
    public static final BooleanExpression TRUE_EXPRESSION = new BooleanExpression() {
        @Override
        public Comparable getValue(Row row) {
            return true;
        }
    };

    public boolean apply(Row row) {
        return (boolean) getValue(row);
    }
}
