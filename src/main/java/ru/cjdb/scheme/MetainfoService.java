package ru.cjdb.scheme;

import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;

import java.util.List;

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

    List<Type> getColumnTypes(Table table);

    void addIndex(Index index);
}
