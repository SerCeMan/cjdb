package ru.cjdb.scheme.dto;

import ru.cjdb.scheme.types.Type;
import ru.cjdb.scheme.types.impl.IntType;
import ru.cjdb.scheme.types.impl.VarcharType;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Информация о колонке
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public final class Column {
    @XmlAttribute(name = "name")
    private String name;

    @XmlElements({
            @XmlElement(name = "int", type = IntType.class),
            @XmlElement(name = "varchar", type = VarcharType.class)
    })
    private Type type;

    Column() {
    }

    public Column(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Column column = (Column) o;
        return Objects.equals(name, column.name) && Objects.equals(type, column.type);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
