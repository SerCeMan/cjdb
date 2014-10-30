package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.result.QueryResult;


/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class SelectQueryResult implements QueryResult {

    private Cursor cursor;

    public SelectQueryResult(Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public boolean hasResult() {
        return true;
    }

    @Override
    public int rowsAffected() {
        return 0;
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }
}
