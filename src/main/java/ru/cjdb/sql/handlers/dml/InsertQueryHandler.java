package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.indexes.IndexService;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.result.impl.OkQueryResult;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.storage.DiskPage;
import ru.cjdb.storage.DiskPageUtils;
import ru.cjdb.storage.fs.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
@Singleton
public class InsertQueryHandler extends RegisterableQueryHandler<InsertQuery> {

    @Inject
    MetainfoService metainfoService;
    @Inject
    ConfigStorage configStorage;
    @Inject
    DiskManagerFactory diskManagerFactory;
    @Inject
    IndexService indexService;

    @Inject
    public InsertQueryHandler() {
        super(InsertQuery.class);
    }

    @Override
    public QueryResult execute(InsertQuery query) {
        String tableName = query.getName();

        DiskManager manager = diskManagerFactory.get(tableName);
        DiskPage freePage = manager.getFreePage();

        Table table = metainfoService.getTable(tableName);

        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData());
        int bytesPerRow = metainfoService.bytesPerRow(table);

        int rowCount = DiskPageUtils.calculateRowCount(bytesPerRow);
        int metaDataSize = DiskPageUtils.metadataSize(bytesPerRow);
        int freeRowId = DiskPageUtils.findFreeRowIdAndSetAsBusy(rowCount, metaDataSize, buffer);
        int freeRowOffset = metaDataSize + freeRowId * bytesPerRow;

        buffer.position(freeRowOffset);
        List<Type> types = metainfoService.getColumnTypes(table);
        for (int i = 0; i < types.size(); i++) {
            Type type = types.get(i);
            type.write(buffer, query.getValues()[i]);
        }

        freePage.setDirty(true);

        // добавим строчку в индексы
        List<Index> indexes = metainfoService.getIndexes(table);
        for(Index index: indexes) {
            indexService.addRow(table, index, freePage.getId(), freeRowId, query.getValues());
        }

        manager.flush();
        return OkQueryResult.INSTANCE;
    }

}
