package storage.fs;

import storage.fs.DiskPage;
import utils.FileUtils;

import java.nio.MappedByteBuffer;

import static java.nio.channels.FileChannel.MapMode;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskManager {

    private String filePath;

    private DiskPage freePage;
    private DiskPage fullPage;

    private MappedByteBuffer byteBuffer;


    public DiskManager(String filePath) {
        this.filePath = filePath;
        init();
    }

    private void init() {
        byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE);
    }


    public DiskPage getFreePage() {
        return null;
    }
}
