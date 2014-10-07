package ru.cjdb.storage.fs;

/**
 * Страничка с данными
 *
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskPage {
    private byte[] data;
    // Ох, в ByteBuffer индекс - инт, итого - 2GB на табличку :(
    // Но, думаю, нам хватит
    private int offset;
    private boolean dirty;

    public DiskPage(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public byte[] getData() {
        return data;
    }
}
