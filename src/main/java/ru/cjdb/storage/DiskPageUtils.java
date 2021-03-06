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
        int bitMaskBytes = (int) Math.ceil(rowCount / (double) Byte.SIZE);
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

    public static boolean hasFreeRows(DiskPage page, int bytesPerRow) {
        int metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);
        BitSet pageBitMask = getPageBitMask(metaDataSize, ByteBuffer.wrap(page.getData()));
        for (int i = 0; i < metaDataSize; i++) {
            boolean free = !pageBitMask.get(i);
            if (free) {
                return true;
            }
        }
        return false;
    }

    public static int findFreeRowIdAndSetAsBusy(int rowCount, int metaDataSize, ByteBuffer buffer) {
        int freeRowId = -1;
        BitSet freePagesBitSet = getPageBitMask(metaDataSize, buffer);
        for (int i = 0; i < rowCount; i++) {
            boolean busy = freePagesBitSet.get(i);
            if (!busy) {
                // нашли свободную страничку
                freeRowId = i;
                // Пишем ее как занятую
                freePagesBitSet.set(freeRowId, true);
                savePageBitMask(buffer, freePagesBitSet);

                break;
            }
        }
        return freeRowId;
    }

    public static int calculateFreeRowOffset(ByteBuffer buffer, int bytesPerRow) {
        int rowCount = calculateRowCount(bytesPerRow);
        int metaDataSize = metadataSize(bytesPerRow);
        int freeRowId = findFreeRowIdAndSetAsBusy(rowCount, metaDataSize, buffer);
        return metaDataSize + freeRowId * bytesPerRow;
    }
}
