package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public class InsertQueryResult implements QueryResult {

    private final int count;

    public InsertQueryResult(int count) {
        this.count = count;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public boolean hasResult() {
        return false;
    }

    @Override
    public int rowsAffected() {
        return count;
    }

    @Override
    public Cursor getCursor() {
        return null;
    }
}
