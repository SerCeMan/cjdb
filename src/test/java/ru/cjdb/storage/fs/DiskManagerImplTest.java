package ru.cjdb.storage.fs;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetaStorageModule;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.PageCache;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiskManagerImplTest {

    private static final String TEST_DB_PATH = "testDb";

    @Inject
    DiskManagerFactory diskManagerFactory;
    @Inject
    PageCache pageCache;
    @Inject
    ConfigStorage configStorage;

    @Before
    public void init() {
        ObjectGraph.create(new DiskManagerTestModule()).inject(this);

        File testDbFIle = new File(getTestDbFilePath());
        if (testDbFIle.exists()) {
            boolean deleted = testDbFIle.delete();
            if (!deleted) {
                throw new RuntimeException("Test DB file can not be deleted in " + getTestDbFilePath());
            }
        }
    }

    @Test
    public void testPagesEmpty() {
        String tableName = TestUtils.createRandomName();
        DiskManager manager = diskManagerFactory.get(tableName);
        Assert.assertEquals(0, manager.pageCount());
    }

    @Test
    public void testExpand() {
        String tableName = TestUtils.createRandomName();
        DiskManager manager = diskManagerFactory.get(tableName);
        DiskPage page = manager.getFreePage();
        Assert.assertEquals(page.getId(), 0);
        Assert.assertTrue(manager.pageCount() > 0);
    }

    @Test
    public void testFlush() {
        DiskManager manager = diskManagerFactory.get(TEST_DB_PATH);

        // Пишем Hello В страничку
        DiskPage page = manager.getFreePage();
        byte[] data = page.getData();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(20);
        byte[] hello = "Hello".getBytes();
        buffer.put(hello);
        page.setDirty(true); // не забываем пометить "dirty"
        manager.flush();

        // Проверяем, что кэш чистый
        Assert.assertTrue(pageCache.values().isEmpty());

        DiskPage page2 = manager.getPage(page.getId());

        ByteBuffer buffer2 = ByteBuffer.wrap(page2.getData());
        buffer2.position(20);

        byte[] hello2 = new byte[hello.length];
        buffer2.get(hello2);

        Assert.assertArrayEquals(hello, hello2);
    }


    @Test
    public void testFileCreated() {
        DiskManager manager = diskManagerFactory.get(TEST_DB_PATH);
        Assert.assertTrue(new File(getTestDbFilePath()).exists());
    }

    @Test
    public void getFreePageTest() {
        DiskManager manager = diskManagerFactory.get(TEST_DB_PATH);
        DiskPage page = manager.getFreePage();
        Assert.assertNotNull(page);
    }

    private String getTestDbFilePath() {
        return configStorage.getRootPath() + TEST_DB_PATH;
    }

    @Module(injects = DiskManagerImplTest.class)
    public static final class DiskManagerTestModule {
        @Provides
        public MetainfoService metainfoService() {
            MetainfoService service = mock(MetainfoService.class);
            when(service.bytesPerRow(any())).thenReturn(1024);
            return service;
        }
    }
}