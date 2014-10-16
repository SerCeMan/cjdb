package ru.cjdb;

import dagger.Module;
import ru.cjdb.printer.ResultPrinterModule;
import ru.cjdb.scheme.MetaStorageModule;
import ru.cjdb.sql.QueryExecutorModule;
import ru.cjdb.sql.parser.QueryParserModule;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Module(
        injects = {CjDatabase.class},
        includes = {
            QueryParserModule.class,
            QueryExecutorModule.class,
            MetaStorageModule.class,
            ResultPrinterModule.class
        }
)
public class CjDbModule {

}
