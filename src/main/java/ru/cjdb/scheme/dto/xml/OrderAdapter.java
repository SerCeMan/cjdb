package ru.cjdb.scheme.dto.xml;

import ru.cjdb.scheme.dto.Order;

/**
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
public class OrderAdapter extends EnumXmlAdapter<Order> {
    @Override
    protected Class<Order> getEnumClass() {
        return Order.class;
    }
}
