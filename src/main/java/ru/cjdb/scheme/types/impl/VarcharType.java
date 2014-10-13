package ru.cjdb.scheme.types.impl;

import ru.cjdb.scheme.types.Type;

import javax.xml.bind.annotation.XmlAttribute;

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
