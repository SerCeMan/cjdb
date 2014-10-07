package ru.cjdb.storage.fs;

import dagger.Module;
import dagger.ObjectGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.cjdb.CjDbModule;
import ru.cjdb.config.ConfigStorage;

import javax.inject.Inject;
import java.io.File;

public class DiskManagerTest {

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
    public void testFileCreated() {
        DiskManager manager = new DiskManager(getTestDbFilePath());
        Assert.assertTrue(new File(getTestDbFilePath()).exists());
    }

    @Test
    public void getFreePageTest() {
        DiskManager manager = new DiskManager(getTestDbFilePath());
        DiskPage page = manager.getFreePage();
        Assert.assertNotNull(page);
    }

    private String getTestDbFilePath() {
        return configStorage.getRootPath() + TEST_DB_PATH;
    }

    @Module(injects = DiskManagerTest.class)
    public static final class DiskManagerTestModule {
    }
}