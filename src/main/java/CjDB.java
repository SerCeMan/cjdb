import config.Props;
import dagger.ObjectGraph;
import sql.queries.Query;
import sql.QueryExecutor;
import sql.parser.QueryParser;
import sql.result.QueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Singleton
public class CjDB implements CjDataBase {

    private QueryParser queryParser;
    private QueryExecutor queryExecutor;

    @Inject
    public CjDB(QueryParser queryParser, QueryExecutor queryExecutor) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
    }

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new CjDbModule());
        CjDataBase db = objectGraph.get(CjDB.class);
        db.execPrint(Props.PATH);
    }

    @Override
    public void execPrint(String sql) {
        QueryResult result = exec(sql);
        System.out.println("successful: " + result.isSuccessful());
        if(result.hasResult()) {
            System.out.println(result.getResult());
        }
    }

    @Override
    public QueryResult exec(String sql) {
        Query query = queryParser.parseQuery(sql);
        return queryExecutor.execute(query);
    }
}
