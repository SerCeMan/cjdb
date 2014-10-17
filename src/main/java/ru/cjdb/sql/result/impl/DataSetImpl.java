package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.result.DataSet;
import ru.cjdb.sql.result.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class DataSetImpl implements DataSet {

    private final List<Row> rows;

    public DataSetImpl(List<Row> rows) {
        this.rows = rows;
    }

    public DataSetImpl() {
        rows = new ArrayList<>();
    }

    public void addRow(Row row) {
        rows.add(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Row getRow(int number) {
        return rows.get(number);
    }

    @Override
    public Collection<Row> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
