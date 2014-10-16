package ru.cjdb.storage.fs;

/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class DiskPageUtils {
    public static int calculateRowCount(int bytePerRowCount) {
        int pagesizeInBits = Byte.SIZE * Constants.PAGE_SIZE - Integer.SIZE;
        return pagesizeInBits / (bytePerRowCount * Byte.SIZE + 1); // 1 бит на хранение флага заполненности
    }
}
