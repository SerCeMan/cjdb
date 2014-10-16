package ru.cjdb.printer;

import ru.cjdb.sql.result.QueryResult;

import javax.inject.Singleton;

/**
 * Created by flire on 17.10.14.
 */
@Singleton
public class ResultPrinterImpl implements ResultPrinter {
    @Override
    public void print(QueryResult result) {
        if(!result.hasResult())
        {
            System.out.println("No result");
        }
        else
        {
            System.out.println("Has result");
        }
    }
}
