package sql.result;

import java.util.List;

/**
 * Строчка с результатом
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface Row {
    int getColumnCount();

    Object getAt(int columnNumber);

    List<Object> values();
}
