package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetaStorage;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.QueryExecutor;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.result.OkQueryResult;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.storage.fs.Constants;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskPage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
@Singleton
public class InsertQueryHandler extends RegisterableQueryHandler<InsertQuery> {

    @Inject
    MetaStorage metaStorage;
    @Inject
    ConfigStorage configStorage;

    @Inject
    public InsertQueryHandler(QueryExecutor queryExecutor) {
        super(InsertQuery.class, queryExecutor);
    }

    @Override
    public QueryResult execute(InsertQuery query) {
        String tableName = query.getName();
        Metainfo metainfo = metaStorage.getMetainfo();
        // TODO вынести поиск таблиц и прочее в отдельный сервис,
        // TODO а работу c MetaStorage в MetaService
        Optional<Table> oTable = metainfo.getTables()
                .stream()
                .filter(tbl -> tbl.getName().equals(tableName))
                .findAny();
        if (!oTable.isPresent()) {
            throw new RuntimeException("Table " + tableName + " not found!");
        }
        Table table = oTable.get();

        DiskManager manager = new DiskManager(configStorage.getRootPath() + "/" + tableName);
        DiskPage freePage = manager.getFreePage();

        int bytePerColumnCount = calculateColumnByteCount(table);
        int columnCount = Constants.PAGE_SIZE / bytePerColumnCount;
        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData(), 0, 4);
        int tmp = buffer.getInt();
        for(int i = 0; i < columnCount; i++) {
            // нашли пустую колонку
            if((tmp & columnCount) == 0) {
                ByteBuffer contentBUffer = ByteBuffer.wrap(freePage.getData());
                contentBUffer.position(bytePerColumnCount * i + Constants.METAINFO_PAGE_BLOCK_SIZE);
                break;
            }
        }
        return OkQueryResult.INSTANCE;
    }


    private int calculateColumnByteCount(Table table) {
        return table.getColumns()
                .stream()
                .collect(Collectors.summingInt(column -> column.getType().byteCount()));
    }
}
