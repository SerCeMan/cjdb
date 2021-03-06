package ru.cjdb.storage.fs;

import com.google.common.base.Preconditions;
import ru.cjdb.storage.Constants;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.PageCache;
import ru.cjdb.utils.FileUtils;

import java.nio.MappedByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.nio.channels.FileChannel.MapMode;
import static ru.cjdb.storage.Constants.PAGE_SIZE;

/**
 * Дисковый менеджер, отвечает за работу с файлом конкретной таблицы
 *
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
class DiskManagerImpl implements DiskManager {

    private static final int EXPAND_PAGE_COUNT = 4; // сколько страниц будет считано с диска при нехватке

    private final AtomicInteger counter = new AtomicInteger(); // page counter

    private final String tableName;
    private final PageCache pageCache;
    private final String filePath; //Путь до файла БД
    private final Predicate<DiskPage> freePageCheker;
    private int freePageId = -1;
    private MappedByteBuffer byteBuffer;

    public DiskManagerImpl(String filePath, String tableName,
                           PageCache pageCache, Predicate<DiskPage> freePageCheker) {
        this.filePath = filePath;
        this.tableName = tableName;
        this.pageCache = pageCache;
        this.freePageCheker = freePageCheker;
        init();
    }

    private void init() {
        boolean fileExists = FileUtils.exists(filePath);

        if (!fileExists) {
            byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE, Constants.METADATA_SIZE);
            initDbFile();
        } else {
            byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE);
            counter.set(byteBuffer.getInt());
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

    @Override
    public DiskPage getPage(int id) {
        int pageCount = pageCount();
        Preconditions.checkArgument(id >= 0 && id < pageCount, "Page number should been be between zero and pageCount(" + pageCount + "), value=" + id);
        return pageCache.get(tableName, id, () -> loadPageFromDisk(id));
    }

    @Override
    public int pageCount() {
        return counter.get();
    }

    /**
     * Возвратить свободную для записи страницу
     */
    @Override
    public DiskPage getFreePage() {
        while (freePageId != -1) {
            DiskPage page = getPage(freePageId);
            if (freePageCheker.test(page)) {
                return page;
            } else {
                freePageId = page.getNextFreePage();
            }
        }
        expand();
        return getFreePage();
    }

    /**
     * Сбрасывает дисковый кэш на диск
     */
    @Override
    public void flush() {
        pageCache.values()
                .stream()
                .unordered()
                .filter(DiskPage::isDirty)
                .forEach(this::flush);
        byteBuffer.rewind();
        byteBuffer.putInt(counter.intValue());
        pageCache.clear();
    }

    /**
     * Сбрасывает конкетную страничку на диск
     */
    @Override
    public void flush(DiskPage page) {
        if (page.isDirty()) {
            byteBuffer.position(calculateOffset(page.getId()));
            byteBuffer.put(page.getData());
        }
    }

    /**
     * Вычисляет смещение конкретной страницы относительно начала файла
     */
    private int calculateOffset(int pageId) {
        return Constants.METADATA_SIZE + pageId * Constants.PAGE_SIZE;
    }

    /**
     * Расширить файл таблицы - добавить новые свободные страницы в список
     */
    private void expand() {
        expandBuffer(EXPAND_PAGE_COUNT);
        DiskPage previousPage = null;
        for (int i = 0; i < EXPAND_PAGE_COUNT; i++) {
            int pageId = counter.getAndIncrement();
            DiskPage page = loadPageFromDisk(pageId);
            page.setNextFreePage(-1);
            if (freePageId == -1) {
                freePageId = page.getId();
            } else {
                assert previousPage != null;
                previousPage.setNextFreePage(page.getId());
            }
            previousPage = page;
            pageCache.put(tableName, page.getId(), page);
        }
    }

    /**
     * Увеличить файл на нужное количество страQниц
     */
    private void expandBuffer(int expandPageCount) {
        int prevPosition = byteBuffer.position();
        int prevLimit = byteBuffer.limit();
        byteBuffer = FileUtils.map(filePath, MapMode.READ_WRITE, prevLimit + expandPageCount * PAGE_SIZE);
        byteBuffer.position(prevPosition);
    }

    /**
     * Подгружает нужную страницу с диска
     */
    private DiskPage loadPageFromDisk(int pageId) {
        byteBuffer.position(calculateOffset(pageId));
        byte[] data = new byte[PAGE_SIZE];
        byteBuffer.get(data);
        return new DiskPage(pageId, data);
    }
}
