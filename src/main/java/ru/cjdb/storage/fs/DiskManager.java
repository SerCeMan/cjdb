package ru.cjdb.storage.fs;

import ru.cjdb.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.LinkedList;
import java.util.stream.Stream;

import static java.nio.channels.FileChannel.MapMode;
import static java.util.stream.Stream.concat;
import static ru.cjdb.storage.fs.Constants.PAGE_SIZE;

/**
 * Дисковый менеджер, отвечает за работу с файлом конкретной таблицы
 *
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskManager {

    public static final int EXPAND_PAGE_COUNT = 4; // сколько страниц будет считано с диска при нехватке

    private final String filePath; //Путь до файла БД

    private final LinkedList<DiskPage> freePages = new LinkedList<>();
    private final LinkedList<DiskPage> fullPages = new LinkedList<>();

    private MappedByteBuffer byteBuffer;

    public DiskManager(String filePath) {
        this.filePath = filePath;
        init();
    }

    private void init() {
        boolean fileExists = FileUtils.exists(filePath);

        if (!fileExists) {
            byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE, Constants.METADATA_SIZE);
            initDbFile();
        } else {
            byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE);
        }
    }

    /**
     * Первоначальная инициализация файла таблицы
     */
    private void initDbFile() {
        for (int i = 0; i < Constants.METADATA_SIZE; i++) {
            byteBuffer.put((byte) 0);
        }
    }

    /**
     * Возвратить свободную для записи страницу
     */
    public DiskPage getFreePage() {
        if (freePages.isEmpty()) {
            expand();
        }
        return freePages.getFirst();
    }

    public void flush() {
        concat(freePages.stream(), fullPages.stream())
                .unordered()
                .filter(DiskPage::isDirty)
                .forEach(page -> {
                    byteBuffer.position(page.getOffset());
                    byteBuffer.put(page.getData());
                });
    }

    /**
     * Расширить файл таблицы - добавить новые свободные страницы в список
     */
    private void expand() {
        int prevPosition = byteBuffer.position();
        int prevLimit = byteBuffer.limit();
        byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE, prevLimit + EXPAND_PAGE_COUNT * PAGE_SIZE);
        byteBuffer.position(prevPosition);
        for (int i = 0; i < EXPAND_PAGE_COUNT; i++) {
            byte[] data = new byte[PAGE_SIZE];
            byteBuffer.get(data);
            freePages.add(new DiskPage(data, prevLimit + i * PAGE_SIZE));
        }
    }
}
