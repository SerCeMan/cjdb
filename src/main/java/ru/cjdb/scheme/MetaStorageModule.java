package ru.cjdb.scheme;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.storage.MetaStorageImpl;

@Module(injects = {MetaStorage.class, MetainfoService.class })
public class MetaStorageModule {
    @Provides
    public MetaStorage provideMetaStorage(ConfigStorage configStorage) {
        return new MetaStorageImpl(configStorage);
    }

    @Provides
    public MetainfoService provideMetainfoService(MetaStorage metaStorage) {
        return new MetainfoServiceImpl(metaStorage);
    }
}
