package ru.cjdb.sql.queries.ddl;

import ru.cjdb.sql.queries.Query;
import ru.cjdb.scheme.types.Type;

import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 03.10.14
 */
public class CreateTableQuery implements Query {
    private final String name;
    private final List<ColumnDefinition> rows;

    public CreateTableQuery(String name, List<ColumnDefinition> rows) {
        this.name = name;
        this.rows = rows;
    }

    public String getName() {
        return name;
    }

    public List<ColumnDefinition> getRows() {
        return rows;
    }

    public static final class ColumnDefinition {
        private final String name;
        private final Type type;

        public ColumnDefinition(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Type getType() {
            return type;
        }
    }
}
