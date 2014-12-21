package ru.cjdb.scheme.types.impl;

import com.google.common.base.Preconditions;
import ru.cjdb.scheme.types.Type;

import java.nio.ByteBuffer;

/** DoubleType implementation
 * Created by flire on 21.11.14.
 */
public class DoubleType implements Type {
    @Override
    public String name() {
        return "DOUBLE";
    }

    @Override
    public int bytes() {
        return Double.BYTES;
    }

    @Override
    public void write(ByteBuffer buffer, Object o) {
        Preconditions.checkArgument(o instanceof Double, "Object should be Double when type DoubleType");
        buffer.putDouble((Double)o);
    }

    @Override
    public Comparable read(ByteBuffer buffer) {
        return buffer.getDouble();
    }

    @Override
    public Comparable valueOf(Object value) {
        return Double.valueOf(String.valueOf(value));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DoubleType;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }
}
