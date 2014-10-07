package ru.cjdb.scheme;

import dagger.Module;
import dagger.Provides;
import ru.cjdb.config.ConfigStorage;

@Module(injects = MetaStorage.class)
public class MetaStorageModule {
    @Provides
    public MetaStorage provideMetaStorage(ConfigStorage configStorage) {
        return new MetaStorageImpl(configStorage);
    }
}
