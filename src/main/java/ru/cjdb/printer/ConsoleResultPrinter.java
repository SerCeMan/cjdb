package ru.cjdb.printer;

import com.google.common.base.Joiner;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.sql.cursor.Cursor;
import ru.cjdb.sql.result.QueryResult;
import ru.cjdb.sql.result.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * ResultPrinter implementation for INT
 * Created by flire on 17.10.14.
 */
public class ConsoleResultPrinter implements ResultPrinter {
    @Override
    public void print(QueryResult result) {
        if (!result.hasResult()) {
            System.out.println("OK");
            if(result.rowsAffected() != 0) {
                System.out.format("Rows affected:%d\n",result.rowsAffected());
            }
        } else {
            Cursor resultCursor = result.getCursor();
            String format = format(resultCursor.types());
            resultCursor.forEach(currentRow -> {
                System.out.format(format, currentRow.values());
                System.out.println();
            });
            System.out.println();
        }
    }

    private String format(List<Type> types) {
        List<String> formats = new ArrayList<>();
        for(Type type: types) {
            if(type.name().equals("INT")) {
                formats.add("%10d");
            }
            else if(type.name().equals("VARCHAR")) {
                String format = String.format("%%%ds",type.bytes());
                formats.add(format);
            }

            else if(type.name().equals("DOUBLE")) {
                formats.add("%10.5f");
            }
        }
        return Joiner.on(" ").join(formats);
    }
}
