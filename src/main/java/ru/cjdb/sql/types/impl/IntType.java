package ru.cjdb.sql.types.impl;

import ru.cjdb.sql.types.Type;

/**
 * Целочисленный тип инт
 *
 * @author Sergey Tselovalnikov
 */
public class IntType implements Type {
    @Override
    public String name() {
        return "INT";
    }

    @Override
    public int bytes() {
        return Integer.BYTES;
    }
}
