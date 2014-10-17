package ru.cjdb.scheme;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.storage.MetaStorageImpl;

import javax.inject.Singleton;

@Module(injects = {MetaStorage.class, MetainfoService.class })
public class MetaStorageModule {
    @Provides
    @Singleton
    public MetaStorage provideMetaStorage(ConfigStorage configStorage) {
        return new MetaStorageImpl(configStorage);
    }

    @Provides
    @Singleton
    public MetainfoService provideMetainfoService(MetaStorage metaStorage) {
        return new MetainfoServiceImpl(metaStorage);
    }
}
