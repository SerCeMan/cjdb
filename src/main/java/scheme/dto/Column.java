package scheme.dto;

import sql.types.Type;

import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public final class Column {
    @XmlAttribute(name = "name")
    private String name;
    @XmlAttribute(name = "type")
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
