import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Проверяет, что тесты запускаются в Java 8
 *
 * @author Sergey Tselovalnikov
 * @since 18.09.14
 */
public class TestRunJava8 {

    @Test
    public void testJava8() {
        Assert.assertEquals(3L, (long)Arrays.asList(1, 2, 3).stream().max(Integer::compare).get());
    }

}
