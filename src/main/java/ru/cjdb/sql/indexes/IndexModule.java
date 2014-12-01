package ru.cjdb.sql.indexes;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.scheme.MetaStorageModule;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Singleton;

@Module(injects = IndexService.class, includes = MetaStorageModule.class)
public class IndexModule {

    @Provides
    @Singleton
    public IndexService provideIndexService(DiskManagerFactory diskManagerFactory) {
        return new IndexServiceImpl(diskManagerFactory);
    }
}
