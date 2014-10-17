package ru.cjdb.printer;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.sql.parser.QueryParser;
import ru.cjdb.sql.parser.QueryParserImpl;

import javax.inject.Singleton;

@Module(injects = ResultPrinter.class)
public class ResultPrinterModule {
    @Provides
    @Singleton
    public ResultPrinter provideQueryParser() {
        return new ResultPrinterImpl();
    }
}
