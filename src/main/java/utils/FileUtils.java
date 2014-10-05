package utils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static MappedByteBuffer map(String path, FileChannel.MapMode mode) {
        try (FileInputStream stream = new FileInputStream(new File(path))) {
            FileChannel fc = stream.getChannel();
            return fc.map(mode, 0, fc.size());
        } catch (Exception e) {
            throw new RuntimeException("Error mapping file " + path, e);
        }
    }
}
