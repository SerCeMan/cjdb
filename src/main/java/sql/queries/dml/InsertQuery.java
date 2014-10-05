package sql.queries.dml;

import sql.queries.Query;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class InsertQuery implements Query {
    private final String name;
    private final Object[] values;

    public InsertQuery(String name, Object... values) {
        this.name = name;
        this.values = values;
    }

    public Object[] getValues() {
        return values;
    }

    public String getName() {
        return name;
    }
}
