package ru.cjdb.testutils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Sergey Tselovalnikov
 * @since 16.10.14
 */
public class TestUtils {

    private TestUtils() {}

    public static String createRandomName() {
        return "uniqueDb" + UUID.randomUUID() + ThreadLocalRandom.current().nextLong();
    }
}
