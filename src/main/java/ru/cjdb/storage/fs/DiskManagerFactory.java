package ru.cjdb.storage.fs;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.storage.PageCache;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Фабрика DiskManager.
 *
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
            synchronized (this) {
                manager = managers.get(tableName);
                if (manager == null) {
                    int bytesPerRow = metainfoService.bytesPerRow(metainfoService.getTable(tableName));
                    manager = new DiskManagerImpl(configStorage.getRootPath() + tableName, tableName, bytesPerRow, pageCache);
                    managers.put(tableName, manager);
                    return manager;
                }
            }
        }
        return managers.get(tableName);
    }

    //TODO Dirty hack, bytesPerFow
    public DiskManager getForIndex(String tableName) {
        DiskManager manager = managers.get(tableName);
        if (manager == null) {
            synchronized (this) {
                manager = managers.get(tableName);
                if (manager == null) {
                    int bytesPerRow = 2 * Integer.BYTES;
                    manager = new DiskManagerImpl(configStorage.getRootPath() + tableName, tableName, bytesPerRow, pageCache);
                    managers.put(tableName, manager);
                    return manager;
                }
            }
        }
        return managers.get(tableName);
    }
}
