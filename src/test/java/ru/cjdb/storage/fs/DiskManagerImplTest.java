package ru.cjdb.storage.fs;

import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.config.ConfigStorage;
import ru.cjdb.testutils.TestUtils;

import javax.inject.Inject;
import java.io.File;
import java.nio.ByteBuffer;

public class DiskManagerImplTest {

    private static final String TEST_DB_PATH = "testDb";

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
        DiskManagerImpl manager = new DiskManagerImpl(configStorage.getRootPath() + TestUtils.createRandomName());
        Assert.assertEquals(0, manager.pageCount());
    }

    @Test
    public void testExpand() {
        DiskManagerImpl manager = new DiskManagerImpl(configStorage.getRootPath() + TestUtils.createRandomName());
        DiskPage page = manager.getFreePage();
        Assert.assertEquals(page.getId(), 0);
        Assert.assertTrue(manager.pageCount() > 0);
    }

    @Test
    public void testFlush() {
        DiskManagerImpl manager = new DiskManagerImpl(getTestDbFilePath());

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
        Assert.assertTrue(manager.diskCache.asMap().isEmpty());

        DiskPage page2 = manager.getPage(page.getId());

        ByteBuffer buffer2 = ByteBuffer.wrap(page2.getData());
        buffer2.position(20);

        byte[] hello2 = new byte[hello.length];
        buffer2.get(hello2);

        Assert.assertArrayEquals(hello, hello2);
    }


    @Test
    public void testFileCreated() {
        DiskManagerImpl manager = new DiskManagerImpl(getTestDbFilePath());
        Assert.assertTrue(new File(getTestDbFilePath()).exists());
    }

    @Test
    public void getFreePageTest() {
        DiskManagerImpl manager = new DiskManagerImpl(getTestDbFilePath());
        DiskPage page = manager.getFreePage();
        Assert.assertNotNull(page);
    }

    private String getTestDbFilePath() {
        return configStorage.getRootPath() + TEST_DB_PATH;
    }

    @Module(injects = DiskManagerImplTest.class)
    public static final class DiskManagerTestModule {
    }
}