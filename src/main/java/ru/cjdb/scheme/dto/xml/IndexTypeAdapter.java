package ru.cjdb.scheme.dto.xml;

import static ru.cjdb.scheme.dto.Index.IndexType;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public class IndexTypeAdapter extends EnumXmlAdapter<IndexType> {
    @Override
    protected Class<IndexType> getEnumClass() {
        return IndexType.class;
    }
}
