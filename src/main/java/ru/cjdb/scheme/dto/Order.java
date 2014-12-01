package ru.cjdb.scheme.dto;

import ru.cjdb.scheme.dto.xml.OrderAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Sergey Tselovalnikov
 * @since 30.11.14
 */
@XmlJavaTypeAdapter(OrderAdapter.class)
public enum Order {
    ASC, DESC
}
