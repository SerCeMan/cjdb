package ru.cjdb.sql.handlers.dml;

import ru.cjdb.config.ConfigStorage;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Index;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.sql.cursor.*;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.Expression;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.handlers.RegisterableQueryHandler;
import ru.cjdb.sql.queries.dml.SelectQuery;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.impl.SelectQueryResult;
import ru.cjdb.storage.fs.DiskManager;
import ru.cjdb.storage.fs.DiskManagerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.cjdb.scheme.dto.Index.IndexType;

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
    DiskManagerFactory diskManagerFactory;

    @Inject
    public SelectQueryHandler() {
        super(SelectQuery.class);
    }

    @Override
    public QueryResult execute(SelectQuery query) {
        Table table = metainfoService.getTable(query.getFrom());

        List<Column> columns = table.getColumns()
                .stream()
                .filter(column -> query.getProjections().contains(column.getName()))
                .collect(Collectors.toList());
        BooleanExpression condition = query.getCondition();

        Cursor cursor = createCursor(table, columns, condition);

        if (query.hasJoin()) {
            Table joinTable = metainfoService.getTable(query.getJoinTable());
            cursor = new JoinCursor(cursor, query.getJoinExpression(),
                    () -> createCursor(joinTable, joinTable.getColumns(), BooleanExpression.TRUE_EXPRESSION)
            );
        }

        return new SelectQueryResult(cursor);
    }

    private Cursor createCursor(Table table, List<Column> columns, BooleanExpression condition) {
        DiskManager diskManager = diskManagerFactory.get(table.getName());
        int bytesPerRow = metainfoService.bytesPerRow(table);

        List<Index> indexes = metainfoService.getIndexes(table);
        Cursor cursor = null;
        if (!indexes.isEmpty()) {
            cursor = tryCreateIndexCursor(table, bytesPerRow, diskManager, columns, condition, indexes);
        }
        if (cursor == null) {
            cursor = new FullScanCursor(table.getColumns(), columns, condition, bytesPerRow, diskManager);
        }
        return cursor;
    }

    /**
     * Адовая функция, проверяет значение, если это name=value, то пытается создать HashIndex
     */
    private Cursor tryCreateIndexCursor(Table table, int bytesPerRow, DiskManager diskManager,
                                        List<Column> columns, BooleanExpression condition, List<Index> indexes) {
        if (condition instanceof Comparison) {
            Comparison comparison = (Comparison) condition;
            if (comparison.getOperator() != Comparison.BinOperator.EQUAL) {
                // Для хэш индекса нас интересуют только =
                return null;
            }
            Expression left = comparison.getLeft();
            Expression right = comparison.getRight();
            String colName = null;
            Comparable<?> value = null;
            if (left instanceof ColumnValueExpr && right instanceof ValueExpression) {
                colName = ((ColumnValueExpr) left).getName();
                value = right.getValue(null);
            }
            if (right instanceof ColumnValueExpr && left instanceof ValueExpression) {
                colName = ((ColumnValueExpr) right).getName();
                value = left.getValue(null);
            }
            String columnName = colName;
            Optional<Index> idx = indexes.stream()
                    .filter(index -> index.getColumns()
                            .stream()
                            .filter(colDef -> colDef.getName().equals(columnName)).findAny().isPresent())
                    .findAny();
            if (idx.isPresent()) {
                Column column = table.getColumns().stream().filter(col -> col.getName().equals(columnName)).findAny().get();
                if (idx.get().getType() == IndexType.HASH) {
                    int hash = column.getType().valueOf(value).hashCode();
                    return new HashIndexCursor(diskManagerFactory, table.getColumns(), columns, condition,
                            bytesPerRow, diskManager, idx.get(), hash);
                } else {
                    Comparable<?> val = column.getType().valueOf(value);
                    return new BTreeCursor(diskManagerFactory.getForBTreeIndex(idx.get().getBTreeName()), table.getColumns(), columns, condition,
                            bytesPerRow, diskManager, val, column.getType());
                }
            }
        }
        return null;
    }
}
