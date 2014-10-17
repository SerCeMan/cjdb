package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.result.Row;

public class RowImpl implements Row {
    private final Object[] values;

    public RowImpl(Object... values) {
        this.values = values;
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
    public Object[] values() {
        return new Object[0];
    }
}
