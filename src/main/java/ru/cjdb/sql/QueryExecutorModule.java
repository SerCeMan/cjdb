package ru.cjdb.sql;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.scheme.MetaStorageModule;
import ru.cjdb.sql.handlers.ddl.CreateIndexQueryHandler;
import ru.cjdb.sql.handlers.ddl.CreateTableQueryHandler;
import ru.cjdb.sql.handlers.dml.InsertQueryHandler;
import ru.cjdb.sql.handlers.dml.SelectQueryHandler;
import ru.cjdb.sql.handlers.dml.UpdateQueryHandler;
import ru.cjdb.sql.indexes.IndexModule;
import ru.cjdb.sql.queries.ddl.CreateIndexQuery;

import javax.inject.Singleton;

/**
 * TODO Куча бойлерплейт кода, надо что-то придумать с аннотациями!
 *
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Module(injects = QueryExecutor.class, includes = {MetaStorageModule.class, IndexModule.class})
public class QueryExecutorModule {
    @Provides
    @Singleton
    public QueryExecutor provideQueryExecutor(
            InsertQueryHandler insertQueryHandler,
            SelectQueryHandler selectQueryHandler,
            UpdateQueryHandler updateQueryHandler,
            CreateTableQueryHandler createTableQueryHandler,
            CreateIndexQueryHandler createIndexQueryHandler
    ) {
        QueryExecutorImpl queryExecutor = new QueryExecutorImpl();
        queryExecutor.registerHandlers(
                insertQueryHandler,
                selectQueryHandler,
                updateQueryHandler,
                createTableQueryHandler,
                createIndexQueryHandler
        );
        return queryExecutor;

    }
}
