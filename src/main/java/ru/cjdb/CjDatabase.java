package ru.cjdb;

import ru.cjdb.config.Props;
import dagger.ObjectGraph;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.QueryExecutor;
import ru.cjdb.sql.parser.QueryParser;
import ru.cjdb.sql.result.QueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * CjDB
 *
 * @author Sergey Tselovalnikov
 * @since 28.09.14
 */
@Singleton
public class CjDatabase {

    private QueryParser queryParser;
    private QueryExecutor queryExecutor;

    @Inject
    public CjDatabase(QueryParser queryParser, QueryExecutor queryExecutor) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
    }

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new CjDbModule());
        CjDatabase db = objectGraph.get(CjDatabase.class);
        db.execPrint(Props.PATH);
    }

    public void execPrint(String sql) {
        QueryResult result = exec(sql);
        System.out.println("successful: " + result.isSuccessful());
        if(result.hasResult()) {
            System.out.println(result.getResult());
        }
    }

    public QueryResult exec(String sql) {
        Query query = queryParser.parseQuery(sql);
        return queryExecutor.execute(query);
    }
}
