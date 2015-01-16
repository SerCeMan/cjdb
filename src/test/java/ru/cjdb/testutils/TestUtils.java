package ru.cjdb.testutils;

import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.result.Row;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

/**
 * @author Sergey Tselovalnikov
 * @since 16.10.14
 */
public class TestUtils {

    private TestUtils() {
    }

    public static String createRandomName() {
        return ("uniqueDb" + UUID.randomUUID() + ThreadLocalRandom.current().nextLong())
                .replaceAll("-", "")
                .toLowerCase();
    }

    public static void assertRow(Cursor cursor, Object... values) {
        Row row = cursor.nextRow();
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], row.getAt(i));
        }
    }

    public static void assertRow(Cursor cursor, String message, Object... values) {
        Row row = cursor.nextRow();
        for (int i = 0; i < values.length; i++) {
            assertEquals(message, values[i], row.getAt(i));
        }
    }
}
