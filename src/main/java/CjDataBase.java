import sql.DataSet;

/**
 * Database
 *
 * @author Sergey Tselovalnikov
 * @since 18.09.14
 */
public interface CjDataBase {

    String execPrint(String sql);

    DataSet exec(String sql);
}
