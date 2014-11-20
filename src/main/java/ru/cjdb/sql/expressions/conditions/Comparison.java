package ru.cjdb.sql.expressions.conditions;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.Expression;

import java.util.Objects;

/**
 * @author Sergey Tselovalnikov
 * @since 21.11.14
 */
public class Comparison extends BooleanExpression {

    private Expression left;
    private Expression right;
    private BinOperator operator;

    public Comparison(Expression left, Expression right, BinOperator operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    public BinOperator getOperator() {
        return operator;
    }

    @Override
    public Comparable getValue() {
        return operator.apply(left, right);
    }


    public static enum BinOperator {
        LESS {
            @Override
            public boolean apply(Expression left, Expression right) {
                return left.getValue().compareTo(right.getValue()) < 0;
            }
        },
        GREATER {
            @Override
            public boolean apply(Expression left, Expression right) {
                return left.getValue().compareTo(right.getValue()) > 0;
            }
        },
        EQUAL {
            @Override
            public boolean apply(Expression left, Expression right) {
                return Objects.equals(left.getValue(), right.getValue());
            }
        };

        public abstract boolean apply(Expression left, Expression right);
    }
}
