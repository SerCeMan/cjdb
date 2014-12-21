package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.result.Row;

import java.util.List;
import java.util.function.Consumer;

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
    Row nextRow();

    /**
     * Тип возвращаемых колонок
     */
    List<Type> types();

    /**
     * Текущая обрабатываемая страница
     * <p>
     * //TODO перенести в FullScanCursor
     */
    int currentPageId();

    /**
     * ID текущей строки на странице
     * <p>
     * //TODO перенести в FullScanCursor
     */
    int currentRowId();

    default void forEach(Consumer<? super Row> action) {
        Row row;
        while ((row = nextRow()) != null) {
            action.accept(row);
        }
    }
}
