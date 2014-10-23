package ru.cjdb.storage;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class DiskPageUtils {
    public static int calculateRowCount(int bytePerRowCount) {
        int pagesizeInBits = Byte.SIZE * Constants.PAGE_SIZE - Integer.SIZE;
        return pagesizeInBits / (bytePerRowCount * Byte.SIZE + 1); // 1 бит на хранение флага заполненности
    }

    public static int metadataSize(int bytesPerRow) {
        int rowCount = calculateRowCount(bytesPerRow);
        int nextPagePointer = Integer.BYTES;
        int bitMaskBytes = (int) Math.ceil(rowCount / (double)Byte.SIZE);
        return nextPagePointer + bitMaskBytes;
    }

    public static void savePageBitMask(ByteBuffer buffer, BitSet freePagesBitSet) {
        buffer.position(Integer.BYTES);
        buffer.put(freePagesBitSet.toByteArray());
    }

    public static BitSet getPageBitMask(int metaDataSize, ByteBuffer buffer) {
        buffer.position(Integer.BYTES); // пропускаем ссылку на другую страничку
        byte[] bitmask = new byte[metaDataSize - Integer.BYTES];
        buffer.get(bitmask);
        return BitSet.valueOf(bitmask);
    }
}
