package ru.cjdb.sql.expressions.conditions;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;

/**
 * Условие
 *
 * @author Sergey Tselovalnikov
 * @since 31.10.14
 */
public class ConditionOrAnd extends BooleanExpression {
    private final CondType condType;
    private final BooleanExpression left;
    private final BooleanExpression right;

    public ConditionOrAnd(CondType condType, BooleanExpression left, BooleanExpression right) {
        this.condType = condType;
        this.left = left;
        this.right = right;
    }

    public CondType getCondType() {
        return condType;
    }

    public BooleanExpression getLeft() {
        return left;
    }

    public BooleanExpression getRight() {
        return right;
    }

    @Override
    public Comparable getValue(Row row) {
        return condType.apply(left, right, row);
    }

    public static enum CondType {
        OR {
            @Override
            public boolean apply(BooleanExpression left, BooleanExpression right, Row row) {
                return left.apply(row) || right.apply(row);
            }
        }, AND {
            @Override
            public boolean apply(BooleanExpression left, BooleanExpression right, Row row) {
                return left.apply(row) && right.apply(row);
            }
        };

        public abstract boolean apply(BooleanExpression left, BooleanExpression right, Row row);
    }
}
