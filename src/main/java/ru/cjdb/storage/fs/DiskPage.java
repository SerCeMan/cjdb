package ru.cjdb.storage.fs;

import java.nio.ByteBuffer;

/**
 * Страничка с данными
 * <p>
 * Первые 4 байта - номер следующей свободной страницы
 * Затем битовая маска свободных строчек (1 - занята, 0 - свободна)
 * Затем строчки с данными
 *
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskPage {
    private final int id;
    private int nextFreePage;
    private byte[] data;
    private boolean dirty;

    public DiskPage(int id, byte[] data) {
        this.id = id;
        this.data = data;
        nextFreePage = ByteBuffer.wrap(data).getInt();
    }

    public int getNextFreePage() {
        return nextFreePage;
    }

    public void setNextFreePage(int nextFreePage) {
        ByteBuffer.wrap(data).putInt(nextFreePage);
        this.nextFreePage = nextFreePage;
    }

    public int getId() {
        return id;
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
