package ru.cjdb.scheme.types.impl;

import ru.cjdb.scheme.types.Type;

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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntType;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }
}
