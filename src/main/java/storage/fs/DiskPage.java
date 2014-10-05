package storage.fs;

import java.util.Optional;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskPage {
    private byte[] data;
    private Optional<DiskPage> next;
    private Optional<DiskPage> previous;

    public DiskPage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public Optional<DiskPage> getNext() {
        return next;
    }

    public void setNext(DiskPage next) {
        this.next = Optional.ofNullable(next);
    }

    public Optional<DiskPage> getPrevious() {
        return previous;
    }

    public void setPrevious(DiskPage previous) {
        this.previous = Optional.ofNullable(previous);
    }
}
