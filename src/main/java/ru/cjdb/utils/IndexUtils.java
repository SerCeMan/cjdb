package ru.cjdb.utils;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.scheme.types.Types;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Sergey Tselovalnikov
 * @since 01.12.14
 */
public final class IndexUtils {

    public static int indexBytesPerRow() {
        return Integer.BYTES * 2; // page, row
    }

    public static List<Column> indexColumns() {
        return asList(new Column("page", Types.INT), new Column("row", Types.INT));
    }
}
