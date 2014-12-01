package ru.cjdb.scheme.dto.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public abstract class EnumXmlAdapter<T extends Enum<T>> extends XmlAdapter<String, T> {
    @Override
    public T unmarshal(String v) throws Exception {
        return Enum.valueOf(getEnumClass(), v);
    }

    @Override
    public String marshal(T v) throws Exception {
        return v.name();
    }

    protected abstract Class<T> getEnumClass();
}
