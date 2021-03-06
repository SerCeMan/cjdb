package ru.cjdb.scheme.storage;

import ru.cjdb.scheme.dto.Metainfo;

/**
 * Сервис, сохраняющий и позволяющий получить метаинформацию
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface MetaStorage {

    Metainfo getMetainfo();

    void saveMetainfo(Metainfo metainfo);
}
