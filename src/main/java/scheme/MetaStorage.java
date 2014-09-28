package scheme;

import scheme.dto.Metainfo;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface MetaStorage {

    Metainfo getMetainfo();

    void saveMetainfo(Metainfo metainfo);
}
