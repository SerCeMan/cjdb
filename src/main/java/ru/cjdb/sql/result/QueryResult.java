package ru.cjdb.sql.result;

import ru.cjdb.sql.cursor.Cursor;

/**
 * Результат выполнения запроса
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryResult {

    boolean hasResult();

    int rowsAffected();

    Cursor getCursor();
}
