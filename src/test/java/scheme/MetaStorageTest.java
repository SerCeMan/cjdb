package scheme;

import config.ConfigModule;
import config.ConfigStorage;
import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scheme.dto.Column;
import scheme.dto.Metainfo;
import scheme.dto.Table;
import sql.Type;

import javax.inject.Inject;
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
        table.addColumns(new Column("test", Type.INT));
        metainfo.addTables(table);

        metaStorage.saveMetainfo(metainfo);


        Metainfo actual = metaStorage.getMetainfo();
        Assert.assertEquals(metainfo, actual);
    }

    @Module(injects = MetaStorageTest.class, includes = ConfigModule.class)
    public static final class MetaStorageTestModule {
        @Provides
        public MetaStorage provideStorage(ConfigStorage configStorage) {
            return new MetaStorageImpl(configStorage);
        }
    }
}