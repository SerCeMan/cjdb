package ru.cjdb.sql.parser;

import ru.cjdb.sql.queries.Query;

/**
 * Парсер запросов. Принимает строчку SQL, возвращает объект, реализующий Query
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryParser {
    Query parseQuery(String sql);
}
