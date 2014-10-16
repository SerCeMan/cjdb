package ru.cjdb.printer;

import ru.cjdb.sql.result.QueryResult;

public interface ResultPrinter {
    void print(QueryResult result);
}
