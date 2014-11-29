package ru.cjdb.sql.result.impl;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.sql.result.Row;

import java.util.List;

public class RowImpl implements Row {
    private final List<Column> columns;
    private final Object[] values;

    public RowImpl(List<Column> columns, Object... values) {
        this.columns = columns;
        this.values = values;
    }

    public List<Column> getColumns() {
        return columns;
    }

    @Override
    public int getColumnCount() {
        return values.length;
    }

    @Override
    public Object getAt(int columnNumber) {
        return values[columnNumber];
    }

    @Override
    public Object getByColName(String name) {
        //TODO speed up
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equals(name)) {
                return values[i];
            }
        }
        throw new RuntimeException("Value of column '" + name + "' not found");
    }

    @Override
    public Object[] values() {
        return values;
    }
}
