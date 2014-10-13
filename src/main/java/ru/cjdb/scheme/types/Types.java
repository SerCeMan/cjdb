package ru.cjdb.scheme.types;

import ru.cjdb.scheme.types.impl.IntType;
import ru.cjdb.scheme.types.impl.VarcharType;

/**
 * Типы SQL
 *
 * @author Sergey Tselovalnikov
 * @since 10/11/14
 */
public final class Types {

    public static final Type INT = new IntType();

    public static Type varchar(int length) {
        return new VarcharType(length);
    }

    private Types() {
    }
}
