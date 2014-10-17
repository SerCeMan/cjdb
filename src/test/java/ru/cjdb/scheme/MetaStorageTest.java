package ru.cjdb.scheme;

import ru.cjdb.config.ConfigModule;
import ru.cjdb.config.ConfigStorage;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.storage.MetaStorageImpl;
import ru.cjdb.scheme.types.Types;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

public class MetaStorageTest {
    @Inject
    MetaStorage metaStorage;
    @Inject
    ConfigStorage configStorage;


    @Before
    public void init() {
        ObjectGraph.create(new MetaStorageTestModule()).inject(this);
        File dir = new File(configStorage.getRootPath());
        if (!dir.exists()) {
            boolean success = dir.mkdir();
            if (!success) {
                throw new RuntimeException("Test directory not created");
            }
        }
    }

    @Test
    public void testSaved() {
        Metainfo metainfo = new Metainfo();
        Table table = new Table("test_table");
        table.addColumns(new Column("test", Types.INT));
        table.addColumns(new Column("test2", Types.varchar(10)));
        metainfo.addTables(table);

        metaStorage.saveMetainfo(metainfo);


        Metainfo actual = metaStorage.getMetainfo();
        Assert.assertEquals(metainfo, actual);
    }

    @Module(injects = MetaStorageTest.class, includes = ConfigModule.class)
    public static final class MetaStorageTestModule {
        @Provides
        @Singleton
        public MetaStorage provideStorage(ConfigStorage configStorage) {
            return new MetaStorageImpl(configStorage);
        }
    }
}