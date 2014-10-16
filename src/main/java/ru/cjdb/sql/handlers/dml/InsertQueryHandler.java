package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetaStorage;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.result.OkQueryResult;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.storage.fs.Constants;
import ru.cjdb.storage.fs.DiskManagerImpl;
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
    public InsertQueryHandler() {
        super(InsertQuery.class);
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

        DiskManagerImpl manager = new DiskManagerImpl(configStorage.getRootPath() + "/" + tableName);
        DiskPage freePage = manager.getFreePage();

        int bytePerRowCount = calculateRowByteCount(table);
        int rowCount = calculateRowCount(bytePerRowCount);

        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData());

        int freePageOffset = 0;
        byte current = 0;
        for (int i = 0; i < rowCount; i++) {
            int offset = i % Byte.SIZE;
            if (offset == 0) {
                current = buffer.get();
            }
            byte bit = (byte) ((current >> offset) & 1);
            if (bit == 0) {
                // нашли свободную страницу
                freePageOffset = i * bytePerRowCount + metaDataByteCount(bytePerRowCount);
            }
        }
        buffer.position(freePageOffset);
        buffer.putInt(1); // TODO put bytes;
        freePage.setDirty(true);
        manager.flush();
        return OkQueryResult.INSTANCE;
    }

    private int calculateRowCount(int bytePerRowCount) {
        return (Constants.PAGE_SIZE - metaDataByteCount(bytePerRowCount)) / bytePerRowCount;
    }

    private int metaDataByteCount(int bytePerColumnCount) {
        return (int)Math.ceil(bytePerColumnCount / (float)Byte.SIZE);
    }


    private int calculateRowByteCount(Table table) {
        return table.getColumns()
                .stream()
                .collect(Collectors.summingInt(column -> column.getType().bytes()));
    }
}
