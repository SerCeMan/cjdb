package ru.cjdb.sql.expressions.conditions;

import ru.cjdb.scheme.types.HasType;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.Expression;
import ru.cjdb.sql.result.Row;

import java.util.Objects;
import java.util.Optional;

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
            public boolean compare(Comparable leftValue, Comparable rightValue) {
                return leftValue.compareTo(rightValue) < 0;
            }
        },
        GREATER {
            @Override
            public boolean compare(Comparable leftValue, Comparable rightValue) {
                return leftValue.compareTo(rightValue) > 0;
            }
        },
        EQUAL {
            @Override
            public boolean compare(Comparable leftValue, Comparable rightValue) {
                return Objects.equals(leftValue, rightValue);
            }
        };

        public abstract boolean compare(Comparable leftValue, Comparable rightValue);

        public final boolean apply(Expression left, Expression right, Row row) {
            Comparable leftValue = left.getValue(row);
            Comparable rightValue = right.getValue(row);
            Type type = tryInferType(left, right);
            if (type != null) {
                leftValue = type.valueOf(leftValue);
                rightValue = type.valueOf(rightValue);
            }
            return compare(leftValue, rightValue);
        }

        private Type tryInferType(Expression leftValue, Expression rightValue) {
            if (leftValue instanceof HasType) {
                return ((HasType) leftValue).getType();
            }
            if (rightValue instanceof HasType) {
                return ((HasType) rightValue).getType();
            }
            return null;
        }
    }
}
