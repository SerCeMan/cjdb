package ru.cjdb.storage.fs;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.PageCache;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фабрика DiskManager.
 * <p>
 * Для корректной работы всегда дожен существовать только 1 экземпляр
 * дискового менеджера на таблицу
 *
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
@Singleton
public class DiskManagerFactory {
    private final ConcurrentHashMap<String, DiskManager> managers = new ConcurrentHashMap<>();

    @Inject
    ConfigStorage configStorage;
    @Inject
    PageCache pageCache;
    @Inject
    MetainfoService metainfoService;

    public DiskManager get(String tableName) {
        DiskManager manager = managers.get(tableName);
        if (manager == null) {
            int bytesPerRow = metainfoService.bytesPerRow(metainfoService.getTable(tableName));
            manager = new DiskManagerImpl(configStorage.getRootPath() + tableName, tableName, pageCache,
                    (DiskPage page) -> DiskPageUtils.hasFreeRows(page, bytesPerRow));
            managers.putIfAbsent(tableName, manager);
        }
        return managers.get(tableName);
    }

    //TODO Dirty hack, bytesPerFow
    public DiskManager getForHashIndex(String tableName) {
        DiskManager manager = managers.get(tableName);
        if (manager == null) {
            int bytesPerRow = 2 * Integer.BYTES;
            manager = new DiskManagerImpl(configStorage.getRootPath() + tableName, tableName, pageCache,
                    (DiskPage page) -> DiskPageUtils.hasFreeRows(page, bytesPerRow));
            managers.putIfAbsent(tableName, manager);
        }
        return managers.get(tableName);
    }

    private HashSet<Integer> used = new HashSet<>();

    public DiskManager getForBTreeIndex(String tableName) {
        DiskManager manager = managers.get(tableName);
        if (manager == null) {
            manager = new DiskManagerImpl(configStorage.getRootPath() + tableName, tableName, pageCache,
                    (DiskPage page) -> {
                        // ок, странная логика, это хак, времени осталось мало
                        // что делаем, проверяем что страница свободна по записи el_count на страничке индекса
                        // а уж их как-нибудь будем менеджить сами дальше, потому что для B-tree свободные мы просим по
                        // необычной логике, не как для full scan
                        if(used.contains(page.getId()))
                            return false;
                        used.add(page.getId());
                        return true;
//                        ByteBuffer bb = ByteBuffer.wrap(page.getData());
//                        bb.position(2 * Integer.BYTES);
//                        return bb.getInt() == 0;
                    });
            managers.putIfAbsent(tableName, manager);
        }
        return managers.get(tableName);
    }
}
