package ru.cjdb.storage.fs;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class Constants {
    /**
     * 4 KByte
     */
    public static final int PAGE_SIZE = 4 * 1024; // 4 KByte

    /**
     * 32 КByte
     */
    public static final int METADATA_SIZE = 32 * 1024;


    public static final int METAINFO_PAGE_BLOCK_SIZE = 4; // 4 байта на битовую маску
}