package ru.cjdb.printer;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.sql.parser.QueryParser;
import ru.cjdb.sql.parser.QueryParserImpl;

@Module(injects = ResultPrinter.class)
public class ResultPrinterModule {
    @Provides
    public ResultPrinter provideQueryParser() {
        return new ResultPrinterImpl();
    }
}
