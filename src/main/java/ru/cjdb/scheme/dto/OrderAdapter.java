package ru.cjdb.scheme.dto;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
public class OrderAdapter extends XmlAdapter<String, Order> {
    @Override
    public Order unmarshal(String v) throws Exception {
        return Enum.valueOf(Order.class, v);
    }

    @Override
    public String marshal(Order v) throws Exception {
        return v.name();
    }
}
