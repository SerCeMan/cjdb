package ru.cjdb.scheme;

import ru.cjdb.scheme.dto.Table;

/**
 * Сервис работы с метаинформацией
 *
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public interface MetainfoService {
    void addTable(Table table);

    Table getTable(String name);

    int bytesPerRow(Table table);
}
