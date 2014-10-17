package ru.cjdb.storage.fs;

import jdk.nashorn.internal.runtime.regexp.joni.Config;
import ru.cjdb.config.ConfigStorage;

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

    public DiskManager get(String tableName) {
        DiskManager manager = managers.get(tableName);
        if (manager == null) {
            synchronized (this) {
                manager = managers.get(tableName);
                if (manager == null) {
                    manager = new DiskManagerImpl(configStorage.getRootPath() + tableName);
                    managers.put(tableName, manager);
                    return manager;
                }
            }
        }
        return managers.get(tableName);
    }
}
