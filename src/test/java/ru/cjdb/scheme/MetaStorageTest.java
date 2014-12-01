package ru.cjdb.scheme;

import ru.cjdb.config.ConfigModule;
import ru.cjdb.config.ConfigStorage;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.scheme.dto.*;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.storage.MetaStorageImpl;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

import static java.util.Arrays.asList;
import static ru.cjdb.scheme.dto.Index.IndexColumnDef;
import static ru.cjdb.scheme.dto.Index.IndexType;

public class MetaStorageTest {
    @Inject
    MetaStorage metaStorage;
    @Inject
    MetainfoService metainfoService;
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
        metaStorage.saveMetainfo(new Metainfo());
    }

    @Test
    public void testIndexCreated() {
        Table table = new Table("test_table");
        table.addColumn(new Column("test", Types.INT));
        metainfoService.addTable(table);
        String idxName = TestUtils.createRandomName();
        Index index = new Index(idxName, "2", false, IndexType.HASH, asList(new IndexColumnDef("test", Order.ASC)));
        metainfoService.addIndex(index);

        Assert.assertTrue(metaStorage.getMetainfo().getIndexes().contains(index));
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

        @Provides
        @Singleton
        public MetainfoService provideMetaindoService(MetaStorage metaStorage) {
            return new MetainfoServiceImpl(metaStorage);
        }
    }
}