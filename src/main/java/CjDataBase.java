import sql.query.DataSet;

/**
 * Database
 *
 * @author Sergey Tselovalnikov
 * @since 18.09.14
 */
public interface CjDataBase {

    void execPrint(String sql);

    sql.query.QueryResult exec(String sql);
}
