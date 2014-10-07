package ru.cjdb.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static MappedByteBuffer map(String path, FileChannel.MapMode mode) {
        try (RandomAccessFile file = new RandomAccessFile(new File(path), "rwd")) {
            FileChannel fc = file.getChannel();
            return fc.map(mode, 0, fc.size());
        } catch (Exception e) {
            throw new RuntimeException("Error mapping file " + path, e);
        }
    }

    public static MappedByteBuffer map(String path, FileChannel.MapMode mode, int size) {
        try (RandomAccessFile file = new RandomAccessFile(new File(path), "rwd")) {
            FileChannel fc = file.getChannel();
            return fc.map(mode, 0, size);
        } catch (Exception e) {
            throw new RuntimeException("Error mapping file " + path, e);
        }
    }

    public static boolean exists(String filePath) {
        return new File(filePath).exists();
    }
}
