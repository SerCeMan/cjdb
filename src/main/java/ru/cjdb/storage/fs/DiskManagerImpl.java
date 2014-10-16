package ru.cjdb.storage.fs;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import ru.cjdb.utils.FileUtils;

import java.nio.MappedByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.channels.FileChannel.MapMode;
import static ru.cjdb.storage.fs.Constants.PAGE_SIZE;

/**
 * Дисковый менеджер, отвечает за работу с файлом конкретной таблицы
 *
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class DiskManagerImpl implements DiskManager {

    private static final int EXPAND_PAGE_COUNT = 4; // сколько страниц будет считано с диска при нехватке

    private final AtomicInteger counter = new AtomicInteger(); // page counter
    final Cache<Integer, DiskPage> diskCache = CacheBuilder.<Integer, DiskPage>newBuilder()
            .maximumSize(1000)
            .removalListener(new RemovalListener<Integer, DiskPage>() {
                @Override
                public void onRemoval(RemovalNotification<Integer, DiskPage> notification) {
                    DiskPage page = notification.getValue();
                    flush(page);
                }
            })
            .build();

    private final String filePath; //Путь до файла БД
    private Integer freePageId = null;
    private MappedByteBuffer byteBuffer;

    public DiskManagerImpl(String filePath) {
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
        Preconditions.checkArgument(id >= 0 && id < pageCount(), "Page number should been be between zero and pageCount");
        try {
            return diskCache.get(id, () -> loadPageFromDisk(id));
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while loading page from disk ", e);
        }
    }

    @Override
    public int pageCount() {
        return counter.get();
    }

    /**
     * Возвратить свободную для записи страницу
     */
    public DiskPage getFreePage() {
        while (freePageId != null) {
            DiskPage page = getPage(freePageId);
            if (hasFreeRows(page)) {
                return page;
            } else {
                freePageId = page.getNextFreePage();
            }
        }
        expand();
        return getFreePage();
    }

    // TODO перенести куда-нибудь
    private boolean hasFreeRows(DiskPage page) {
        return true;
    }

    /**
     * Сбрасывает дисковый кэш на диск
     */
    public void flush() {
        diskCache.asMap()
                .values()
                .stream()
                .unordered()
                .filter(DiskPage::isDirty)
                .forEach(this::flush);
        byteBuffer.rewind();
        byteBuffer.putInt(counter.intValue());
        diskCache.invalidateAll();
    }

    /**
     * Сбрасывает конкетную страничку на диск
     */
    private void flush(DiskPage page) {
        if(page.isDirty()) {
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
            if (freePageId == null) {
                freePageId = page.getId();
            } else {
                assert previousPage != null;
                previousPage.setNextFreePage(page.getId());
            }
            previousPage = page;
            diskCache.put(page.getId(), page);
        }
    }

    /**
     * Увеличить файл на нужное количество страниц
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
