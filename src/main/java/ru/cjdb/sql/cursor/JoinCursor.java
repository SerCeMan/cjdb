package ru.cjdb.sql.cursor;

import ru.cjdb.scheme.dto.Column;
import ru.cjdb.scheme.dto.Table;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.result.Row;
import ru.cjdb.sql.result.impl.RowImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Sergey Tselovalnikov
 * @since 10.12.14
 */
public class JoinCursor implements Cursor {

    private final Cursor cursor;
    private final BooleanExpression joinExpression;
    private final CursorProvider joinCursorProvider;
    private Row row;
    private Cursor joinCursor;

    public JoinCursor(Cursor cursor, BooleanExpression joinExpression, CursorProvider joinCursorProvider) {
        this.cursor = cursor;
        this.joinExpression = joinExpression;
        this.joinCursorProvider = joinCursorProvider;
        row = cursor.nextRow();
        joinCursor = joinCursorProvider.create();
    }

    @Override
    public Row nextRow() {
        Row joinRow = joinCursor.nextRow();
        if (joinRow == null) {
            row = cursor.nextRow();
            if(row == null) {
                return null;
            }
            joinCursor = joinCursorProvider.create();
            joinRow = joinCursor.nextRow();
        }
        Row resultRow = concatRow(row, joinRow);
        if (joinExpression.apply(resultRow)) {
            return resultRow;
        }
        return nextRow();
    }

    private Row concatRow(Row row, Row joinRow) {
        RowImpl row1 = (RowImpl) row;
        RowImpl row2 = (RowImpl) joinRow;
        List<Column> resultColumns = new ArrayList<>();
        resultColumns.addAll(row1.getColumns());
        resultColumns.addAll(row2.getColumns());
        int count = row1.getColumnCount() + row2.getColumnCount();
        Object[] results = new Object[count];
        int i = 0;
        for (int j = 0; j < row1.getColumnCount(); j++) {
            results[i++] = row1.getAt(j);
        }
        for (int j = 0; j < row2.getColumnCount(); j++) {
            results[i++] = row2.getAt(j);
        }
        return new RowImpl(resultColumns, results);
    }

    @Override
    public List<Type> types() {
        return null;
    }

    @Override
    public int currentPageId() {
        return 0;
    }

    @Override
    public int currentRowId() {
        return 0;
    }

    public interface CursorProvider {
        Cursor create();
    }
}
