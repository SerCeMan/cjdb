package ru.cjdb.sql.parser;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import ru.cjdb.scheme.MetainfoService;
import ru.cjdb.sql.expressions.BooleanExpression;
import ru.cjdb.sql.expressions.ColumnValueExpr;
import ru.cjdb.sql.expressions.ValueExpression;
import ru.cjdb.sql.expressions.conditions.Comparison;
import ru.cjdb.sql.expressions.conditions.ConditionOrAnd;
import ru.cjdb.sql.queries.Query;
import ru.cjdb.sql.queries.dml.InsertQuery;
import ru.cjdb.sql.queries.dml.SelectQuery;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

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
            //TODO
        }
        if (parse instanceof InsertQuery) {
            //TODO
        }
        if (parse instanceof CreateIndex) {
            //TODO
        }

        if (parse instanceof Select) {
            PlainSelect selectBody = (PlainSelect) ((Select) parse).getSelectBody();
            String tableName = ((Table) selectBody.getFromItem()).getName();

            List<String> columns = selectBody.getSelectItems().stream()
                    .map(SelectExpressionItem.class::cast)
                    .map(SelectExpressionItem::getExpression)
                    .map(Column.class::cast)
                    .map(Column::getColumnName)
                    .collect(Collectors.toList());


            BooleanExpression where = BooleanExpression.TRUE_EXPRESSION;
            if (selectBody.getWhere() instanceof EqualsTo) {
                EqualsTo eq = (EqualsTo) selectBody.getWhere();
                ru.cjdb.sql.expressions.Expression exprLeft = parseExpression(eq.getLeftExpression(), tableName);
                ru.cjdb.sql.expressions.Expression exprRight = parseExpression(eq.getLeftExpression(), tableName);
                where = new Comparison(exprLeft, exprRight, Comparison.BinOperator.EQUAL);
            }
            return new SelectQuery(tableName, columns, where);
        }
        return new InsertQuery("test", 1);
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
