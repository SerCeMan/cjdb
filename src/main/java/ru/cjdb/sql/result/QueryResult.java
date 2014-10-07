package ru.cjdb.sql.result;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryResult {
    boolean isSuccessful();

    boolean hasResult();

    DataSet getResult();
}
