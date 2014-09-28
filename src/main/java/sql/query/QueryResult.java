package sql.query;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
public interface QueryResult {
    boolean isSuccessful();

    boolean hasResult();

    DataSet getResult();
}
