package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.result.Row;

import java.util.List;

/**
 * Курсор, пробегающийся по таблице и возвращающий результат
 *
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public interface Cursor {

    /**
     * Возвращает следующую строчку или null, если строки кончились
     */
    public Row nextRow();

    /**
     * Тип возвращаемых колонок
     */
    public List<Type> types();

    /**
     * Текущая обрабатываемая страница
     */
    public int currentPageId();

    /**
     * ID текущей строки на странице
     */
    public int currentRowId();
}
