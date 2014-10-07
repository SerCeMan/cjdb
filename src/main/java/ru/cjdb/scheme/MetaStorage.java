package ru.cjdb.scheme;

import ru.cjdb.scheme.dto.Metainfo;

/**
 * Сервис, сохраняющий и позволяющий получить метаинформацию
 *
 * TODO Сделать бы какую-нибудь правильную оберточку, позволяющую удобно работать с метаинфой
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface MetaStorage {

    Metainfo getMetainfo();

    void saveMetainfo(Metainfo metainfo);
}
