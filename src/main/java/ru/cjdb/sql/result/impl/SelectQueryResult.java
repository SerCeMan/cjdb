package ru.cjdb.sql.result.impl;

import ru.cjdb.sql.result.DataSet;
import ru.cjdb.sql.result.QueryResult;


/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class SelectQueryResult implements QueryResult {

    private DataSet dataSet;

    public SelectQueryResult(DataSet dataSet) {
        this.dataSet = dataSet;
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
    public DataSet getResult() {
        return dataSet;
    }
}
