package ru.cjdb.scheme.types.impl;

import com.google.common.base.Preconditions;
import ru.cjdb.scheme.types.Type;

import javax.xml.bind.annotation.XmlAttribute;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * VARCHAR
 *
 * @author Sergey Tselovalnikov
 * @since 10/11/14
 */
public class VarcharType implements Type {
    @XmlAttribute(name = "length")
    private int length;

    // для JAXB
    VarcharType() {
    }

    public VarcharType(int length) {
        this.length = length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String name() {
        return "VARCHAR";
    }

    @Override
    public int bytes() {
        return length;
    }

    @Override
    public void write(ByteBuffer buffer, Object o) {
        Preconditions.checkArgument(o instanceof String, "Object should be String when type VarcharType");
        byte[] array = ((String)o).getBytes(Charset.forName("US-ASCII"));
        for(int i = 0; i<length; ++i) {
            buffer.put(array[i]);
        }
    }

    @Override
    public Object read(ByteBuffer buffer) {
        byte[] array = new byte[length];
        buffer.get(array);
        return new String(array, Charset.forName("US-ASCII"));
    }

    @Override
    public Object valueOf(String value) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        VarcharType that = (VarcharType) o;
        return length == that.length;
    }

    @Override
    public int hashCode() {
        return length;
    }
}
