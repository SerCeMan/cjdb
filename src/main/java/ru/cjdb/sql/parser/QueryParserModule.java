package ru.cjdb.sql.parser;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(injects = QueryParser.class)
public class QueryParserModule {
    @Provides
    @Singleton
    public QueryParser provideQueryParser() {
        return new QueryParserImpl();
    }
}
