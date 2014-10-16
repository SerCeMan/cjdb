package ru.cjdb.storage.fs;

/**
 * @author Sergey Tselovalnikov
 * @since 16.10.14
 */
public interface DiskManager {
    DiskPage getPage(int id);

    int pageCount();

    DiskPage getFreePage();
}
