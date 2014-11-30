package ru.cjdb.sql.parser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.update.Update;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.scheme.types.Type;
import ru.cjdb.scheme.types.Types;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.ddl.CreateTableQuery;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sergey Tselovalnikov
 * @since 05.10.14
 */
public class QueryParserImpl implements QueryParser {

    private final MetainfoService metainfoService;

    public QueryParserImpl(MetainfoService metainfoService) {
        this.metainfoService = metainfoService;
    }


    @Override
    public Query parseQuery(String sql) {
        // Все в нижний регистр, чтобы не было случайных ошибок,
        sql = sql.toLowerCase();

        Statement parse;
        try {
            parse = CCJSqlParserUtil.parse(sql);
        } catch (JSQLParserException e) {
            throw new SqlParseException("Error parsing sql: " + sql + "\nError:", e);
        }

        if (parse instanceof CreateTable) {
            CreateTable createTable = (CreateTable) parse;
            String name = createTable.getTable().getName();
            List<CreateTableQuery.ColumnDefinition> colDefs = createTable
                    .getColumnDefinitions()
                    .stream()
                    .map(this::convertColumnDef)
                    .collect(Collectors.toList());

            return new CreateTableQuery(name, colDefs);
        }
        if (parse instanceof Insert) {
            Insert insert = (Insert) parse;
            String name = insert.getTable().getName();
            List<Object> values = new ArrayList<>();
            ExpressionList list = (ExpressionList) insert.getItemsList();
            values.addAll(list.getExpressions().stream()
                    .map(this::getValue)
                    .collect(Collectors.toList()));
            return new InsertQuery(name, values.toArray());
            //TODO
        }
        if (parse instanceof Update) {
            //TODO
        }
        if (parse instanceof CreateIndex) {
            //TODO
        }

        if (parse instanceof Select) {
            PlainSelect selectBody = (PlainSelect) ((Select) parse).getSelectBody();
            String tableName = ((Table) selectBody.getFromItem()).getName();

            List<String> columns;
            if (selectBody.getSelectItems().get(0) instanceof AllColumns) {
                columns = metainfoService.getTable(tableName)
                        .getColumns().stream()
                        .map(ru.cjdb.scheme.dto.Column::getName)
                        .collect(Collectors.toList());
            } else {
                columns = selectBody.getSelectItems().stream()
                        .map(SelectExpressionItem.class::cast)
                        .map(SelectExpressionItem::getExpression)
                        .map(Column.class::cast)
                        .map(Column::getColumnName)
                        .collect(Collectors.toList());
            }


            BooleanExpression where = BooleanExpression.TRUE_EXPRESSION;
            if (selectBody.getWhere() instanceof EqualsTo) { // TODO >,<, etc...
                EqualsTo eq = (EqualsTo) selectBody.getWhere();
                ru.cjdb.sql.expressions.Expression exprLeft = parseExpression(eq.getLeftExpression(), tableName);
                ru.cjdb.sql.expressions.Expression exprRight = parseExpression(eq.getRightExpression(), tableName);
                where = new Comparison(exprLeft, exprRight, Comparison.BinOperator.EQUAL);
            }
            return new SelectQuery(tableName, columns, where);
        }
        return new InsertQuery("test", 1);
    }

    private Object getValue(Expression expr) {
        if (expr instanceof LongValue) {
            return (int)((LongValue) expr).getValue();
        }
        if(expr instanceof DoubleValue) {
            return ((DoubleValue) expr).getValue();
        }
        if(expr instanceof StringValue) {
            return ((StringValue) expr).getValue();
        }
        throw new SqlParseException("Unknown value type " + expr.getClass());
    }

    private CreateTableQuery.ColumnDefinition convertColumnDef(ColumnDefinition columnDefinition) {
        String columnName = columnDefinition.getColumnName();
        Type type = convertDataType(columnDefinition.getColDataType());
        return new CreateTableQuery.ColumnDefinition(columnName, type);
    }

    private Type convertDataType(ColDataType colDataType) {
        switch (colDataType.getDataType()) {
            case "int":
                return Types.INT;
            case "double":
                return Types.DOUBLE;
            case "varchar":
                int length = Integer.valueOf(colDataType.getArgumentsStringList().get(0));
                return Types.varchar(length);
            default:
                throw new SqlParseException("Unsupported data type " + colDataType.getDataType());
        }
    }

    private ru.cjdb.sql.expressions.Expression parseExpression(Expression expr, String tableName) {
        if (expr instanceof LongValue) {
            return new ValueExpression(((LongValue) expr).getStringValue());
        }
        if (expr instanceof StringValue) {
            return new ValueExpression(((StringValue) expr).getValue());
        }
        if (expr instanceof Column) {
            String columnName = ((Column) expr).getColumnName();
            ru.cjdb.scheme.dto.Column column = metainfoService.getTable(tableName).getColumns()
                    .stream()
                    .filter(col -> col.getName().equals(columnName))
                    .findAny()
                    .orElseThrow(() -> new SqlParseException("Column " + columnName + " not found!"));
            return new ColumnValueExpr(columnName, column.getType());
        }
        return null;
    }
}
