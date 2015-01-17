package ru.cjdb.sql.indexes;

import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.result.Row;

import java.util.Map;

/**
 * Сервис работы с индексами
 *
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public interface IndexService {
    void addRow(Table table, Index index, int id, int freeRowId, Object[] values);

    void createIndex(Table table, Index index);

    void updateRow(Table table, Index index, int pageId, int rowId, Object[] values, Object[] newValues);

    void removeRow(Table table, Index index, int pageId, int rowId, Object[] values);
}
