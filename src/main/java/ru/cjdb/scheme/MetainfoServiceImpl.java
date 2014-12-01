package ru.cjdb.scheme;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Metainfo;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.storage.MetaStorage;
import ru.cjdb.scheme.types.Type;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergey Tselovalnikov
 * @since 17.10.14
 */
public class MetainfoServiceImpl implements MetainfoService {

    private MetaStorage metaStorage;
    private Metainfo metainfo;

    public MetainfoServiceImpl(MetaStorage metaStorage) {
        this.metaStorage = metaStorage;
        metainfo = metaStorage.getMetainfo();
    }

    @Override
    public void addTable(Table table) {
        metainfo.addTable(table);
        metaStorage.saveMetainfo(metainfo);
    }

    @Override
    public Table getTable(String name) {
        return metainfo.getTables()
                .stream()
                .filter(tbl -> tbl.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Table " + name + " not found!"));
    }

    @Override
    public int bytesPerRow(Table table) {
        return table.getColumns()
                .stream()
                .collect(Collectors.summingInt(column -> column.getType().bytes()));
    }

    @Override
    public List<Type> getColumnTypes(Table table) {
        return table.getColumns()
                .stream()
                .map(Column::getType)
                .collect(Collectors.toList());
    }

    @Override
    public void addIndex(Index index) {
        metainfo.addIndexes(index);
        metaStorage.saveMetainfo(metainfo);
    }

    @Override
    public List<Index> getIndexes(Table table) {
        return metainfo.getIndexes().stream()
                .filter(inx -> inx.getTable().equals(table.getName()))
                .collect(Collectors.toList());
    }
}
