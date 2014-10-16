package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.DataSet;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Sergey Tselovalnikov
 * @since 29.09.14
 */
@Singleton
public class SelectQueryHandler extends RegisterableQueryHandler<SelectQuery> {

    @Inject
    MetainfoService metainfoService;
    @Inject
    ConfigStorage configStorage;

    @Inject
    public SelectQueryHandler() {
        super(SelectQuery.class);
    }

    @Override
    public QueryResult execute(SelectQuery query) {
        Table table = metainfoService.getTable(query.getFrom());
        int bytesPerRow = metainfoService.bytesPerRow(table);

        // TODO заполнять DataSet
        DiskManager diskManager = new DiskManagerImpl(query.getFrom());
        Cursor cursor = new Cursor(bytesPerRow, diskManager);
        Row last[] = new Row[1];
        Row row;
        while ((row = cursor.nextRow()) != null) {
            System.out.println(row.getAt(0));
            last[0] = row;
        }
        return new QueryResult() {
            @Override
            public boolean isSuccessful() {
                return true;
            }

            @Override
            public boolean hasResult() {
                return true;
            }

            @Override
            public DataSet getResult() {
                return new DataSet() {
                    @Override
                    public int getRowCount() {
                        return 1;
                    }

                    @Override
                    public Row getRow(int number) {
                        return last[0];
                    }

                    @Override
                    public Collection<Row> getRows() {
                        return Arrays.asList(last[0]);
                    }
                };
            }
        };
    }
}
