package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.result.OkQueryResult;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.storage.fs.DiskManagerImpl;
import ru.cjdb.storage.fs.DiskPage;
import ru.cjdb.storage.fs.DiskPageUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.ByteBuffer;
import java.util.BitSet;

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
    public InsertQueryHandler() {
        super(InsertQuery.class);
    }

    @Override
    public QueryResult execute(InsertQuery query) {
        String tableName = query.getName();

        DiskManagerImpl manager = new DiskManagerImpl(configStorage.getRootPath() + "/" + tableName);
        DiskPage freePage = manager.getFreePage();

        Table table = metainfoService.getTable(tableName);
        int bytesPerRow = metainfoService.bytesPerRow(table);
        int rowCount = DiskPageUtils.calculateRowCount(bytesPerRow);

        ByteBuffer buffer = ByteBuffer.wrap(freePage.getData());
        buffer.position(Integer.BYTES); // пропускаем ссылку на другую страничку

        int freeRowId = findFreeRowId(rowCount, buffer);

        int freeRowOffset = Integer.BYTES + /*metadata*/ + freeRowId * bytesPerRow;

        buffer.position(freeRowOffset);
        buffer.putInt((Integer) query.getValues()[0]); // TODO put bytes;
        freePage.setDirty(true);
        manager.flush();
        return OkQueryResult.INSTANCE;
    }

    private int findFreeRowId(int rowCount, ByteBuffer buffer) {
        int freeRowId = -1;
        BitSet freePagesBitSet = BitSet.valueOf(buffer);
        for (int i = 0; i < rowCount; i++) {
            boolean busy = freePagesBitSet.get(i);
            if (!busy) {
                // нашли свободную страничку
                freeRowId = i;
                break;
            }
        }
        return freeRowId;
    }
}
