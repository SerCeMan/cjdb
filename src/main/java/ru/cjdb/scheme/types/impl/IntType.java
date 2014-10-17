package ru.cjdb.scheme.types.impl;

import com.google.common.base.Preconditions;
import ru.cjdb.scheme.types.Type;

import java.nio.ByteBuffer;

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
    public void write(ByteBuffer buffer, Object o) {
        Preconditions.checkArgument(o instanceof Integer, "Object should be Integer when type IntType");
        buffer.putInt((Integer)o);
    }

    @Override
    public Object read(ByteBuffer buffer) {
        return buffer.getInt();
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
