package ru.cjdb.sql.parser;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.scheme.MetaStorageModule;
import ru.cjdb.scheme.MetainfoService;

import javax.inject.Singleton;

@Module(injects = QueryParser.class, includes = MetaStorageModule.class)
public class QueryParserModule {
    @Provides
    @Singleton
    public QueryParser provideQueryParser(MetainfoService metainfoService) {
        return new QueryParserImpl(metainfoService);
    }
}
