package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.result.QueryResult;

/**
 * @author Sergey Tselovalnikov
 * @since 03.10.14
 */
public final class OkQueryResult implements QueryResult {

    public static OkQueryResult INSTANCE = new OkQueryResult();

    private OkQueryResult() {
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
        return 0;
    }

    @Override
    public Cursor getCursor() {
        return null;
    }
}
