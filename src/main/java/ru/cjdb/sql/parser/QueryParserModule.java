package ru.cjdb.sql.parser;

import dagger.Module;
import dagger.Provides;

@Module(injects = QueryParser.class)
public class QueryParserModule {
    @Provides
    public QueryParser provideQueryParser() {
        return new QueryParserImpl();
    }
}
