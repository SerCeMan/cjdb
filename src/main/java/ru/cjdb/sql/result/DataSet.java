package ru.cjdb.sql.result;

import java.util.Collection;

/**
 * По сути - простой аналог {@link java.sql.ResultSet}
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface DataSet {

    int getRowCount();

    Row getRow(int number);

    Collection<Row> getRows();
}
