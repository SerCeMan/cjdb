package ru.cjdb.sql.expressions;

import ru.cjdb.scheme.types.HasType;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.result.Row;

/**
 * Значение колонки
 *
 * @author Sergey Tselovalnikov
 * @since 29.11.14
 */
public class ColumnValueExpr extends Expression implements HasType {

    private final String name;
    private final Type type;

    public ColumnValueExpr(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Comparable getValue(Row row) {
        return (Comparable) row.getByColName(name);
    }
}
