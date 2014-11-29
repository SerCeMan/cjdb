package ru.cjdb.sql.result;

/**
 * Строчка с результатом
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface Row {
    int getColumnCount();

    Object getAt(int columnNumber);

    Object getByColName(String name);

    Object[] values();
}
