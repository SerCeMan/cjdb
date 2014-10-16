package ru.cjdb;

import dagger.ObjectGraph;
import ru.cjdb.printer.ResultPrinter;
import ru.cjdb.sql.QueryExecutor;
import ru.cjdb.sql.parser.QueryParser;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.result.QueryResult;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Scanner;

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
    private ResultPrinter printer;

    @Inject
    public CjDatabase(QueryParser queryParser, QueryExecutor queryExecutor, ResultPrinter printer) {
        this.queryParser = queryParser;
        this.queryExecutor = queryExecutor;
        this.printer = printer;
    }

    public static void main(String[] args) {
        ObjectGraph objectGraph = ObjectGraph.create(new CjDbModule());
        CjDatabase db = objectGraph.get(CjDatabase.class);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            db.execPrint(line);
        }
    }

    public void execPrint(String sql) {
        Query query = queryParser.parseQuery(sql);
        QueryResult result = queryExecutor.execute(query);
        printer.print(result);
    }
}
