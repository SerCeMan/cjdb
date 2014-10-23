package ru.cjdb.storage;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import ru.cjdb.config.ConfigStorage;
import ru.cjdb.config.Props;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Кэш страниц
 * 
 * @author Sergey Tselovalnikov
 * @since 24.10.14
 */
@Singleton
public class PageCache {

    private final Cache<CacheKey, DiskPage> diskCache;

    @Inject
    Provider<DiskManagerFactory> diskManagerFactoryProvider;

    @Inject
    public PageCache(ConfigStorage configStorage) {
        String cacheSize = configStorage.getProperty(Props.CACHE_SIZE);
        if(Strings.isNullOrEmpty(cacheSize)) {
            cacheSize = "512";
        }

        diskCache = CacheBuilder.<Integer, DiskPage>newBuilder()
                .maximumSize(Integer.valueOf(cacheSize))
                .removalListener(new RemovalListener<CacheKey, DiskPage>() {
                    @Override
                    public void onRemoval(RemovalNotification<CacheKey, DiskPage> notification) {
                        DiskPage page = notification.getValue();
                        CacheKey key = notification.getKey();
                        DiskManager manager = diskManagerFactoryProvider.get().get(key.getTable());
                        manager.flush(page);
                    }
                })
                .build();
    }

    /**
     * Достать страничку из кэша
     *
     * @param table таблица
     * @param id id странички
     * @param loader функция загрузки страницы в случае отсутствия
     */
    public DiskPage get(String table, int id, Callable<? extends DiskPage> loader) {
        try {
            return diskCache.get(new CacheKey(table, id), loader);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String table, int id, DiskPage page) {
        diskCache.put(new CacheKey(table, id), page);
    }

    public void clear() {
        diskCache.invalidateAll();
    }

    public Collection<DiskPage> values() {
        return diskCache.asMap().values();
    }

    /**
     * Ключ странички кэша
     */
    public static class CacheKey {
        private final String table;
        private final int id;

        public CacheKey(String table, int id) {
            this.table = table;
            this.id = id;
        }

        public String getTable() {
            return table;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            CacheKey cacheKey = (CacheKey) o;
            return id == cacheKey.id && table.equals(cacheKey.table);

        }

        @Override
        public int hashCode() {
            int result = table.hashCode();
            result = 31 * result + id;
            return result;
        }
    }

}
