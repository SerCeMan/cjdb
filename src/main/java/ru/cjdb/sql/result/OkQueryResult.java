package ru.cjdb.sql.result;

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
    public DataSet getResult() {
        return null;
    }
}
