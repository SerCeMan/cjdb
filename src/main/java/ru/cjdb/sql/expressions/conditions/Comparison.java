package ru.cjdb.sql.expressions.conditions;

import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.Expression;
import ru.cjdb.sql.result.Row;

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
    public Comparable getValue(Row row) {
        return operator.apply(left, right, row);
    }


    public static enum BinOperator {
        LESS {
            @Override
            public boolean apply(Expression left, Expression right, Row row) {
                return left.getValue(row).compareTo(right.getValue(row)) < 0;
            }
        },
        GREATER {
            @Override
            public boolean apply(Expression left, Expression right, Row row) {
                return left.getValue(row).compareTo(right.getValue(row)) > 0;
            }
        },
        EQUAL {
            @Override
            public boolean apply(Expression left, Expression right, Row row) {
                return Objects.equals(left.getValue(row), right.getValue(row));
            }
        };

        public abstract boolean apply(Expression left, Expression right, Row row);
    }
}
