package ru.cjdb;

import dagger.Module;
import ru.cjdb.sql.QueryExecutorModule;
import ru.cjdb.sql.parser.QueryParserModule;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Module(
        injects = { CjDatabase.class },
        includes = { QueryParserModule.class, QueryExecutorModule.class}
)
public class CjDbModule {

}
