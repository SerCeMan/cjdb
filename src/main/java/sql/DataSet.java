package sql;

/**
 * По сути - простой аналог {@link java.sql.ResultSet}
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface DataSet {

    int getRowCount();
}
