package com.pddon.framework.easyapi.dao.tenant;

import com.baomidou.mybatisplus.core.parser.AbstractJsqlParser;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;

/**
 * @ClassName: PddonTenantSqlParser
 * @Description:
 * @Author: Allen
 * @Date: 2025-12-08 23:08
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PddonTenantSqlParser extends TenantSqlParser {

    private TenantHandler easyApiTenantHandler;

    public Object extractValueFromExpression(Expression expr) {
        if(expr == null){
            return null;
        }
        if (expr instanceof StringValue) {
            return ((StringValue) expr).getValue();  // 字符串值
        } else if (expr instanceof LongValue) {
            return ((LongValue) expr).getValue();    // 整数值
        } else if (expr instanceof DoubleValue) {
            return ((DoubleValue) expr).getValue();  // 浮点数值
        } else if (expr instanceof JdbcParameter) {
            return "?";  // JDBC参数占位符
        } else if (expr instanceof NullValue) {
            return null;  // NULL值
        } else if (expr instanceof Function) {
            return expr.toString();  // 函数表达式
        } else if (expr instanceof Column) {
            return ((Column) expr).getColumnName();  // 列引用
        } else if (expr instanceof DateValue) {
            return ((DateValue) expr).getValue();    // 日期值
        } else if (expr instanceof TimestampValue) {
            return ((TimestampValue) expr).getValue(); // 时间戳值
        }
        return expr.toString();  // 其他情况返回字符串表示
    }

    /**
     * insert 语句处理
     */
    @Override
    public void processInsert(Insert insert) {
        List<Column> columns = insert.getColumns();
        if (columns != null && !columns.isEmpty()) {
            ExpressionList exprList = (ExpressionList) insert.getItemsList();
            List<Expression> values = exprList.getExpressions();

            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                if(column.getColumnName().equalsIgnoreCase(easyApiTenantHandler.getTenantIdColumn())){
                    Expression valueExpr = values.get(i);
                    Object value = extractValueFromExpression(valueExpr);
                    if(value != null){
                        //租户ID字段已经不为空，则不需要再次添加租户ID的值
                        return;
                    }
                }
            }
        }
        super.processInsert(insert);
    }
}
