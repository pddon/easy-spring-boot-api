package com.pddon.framework.easyapi.dao.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.RequireDataPermission;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * @ClassName: DataPermissionInterceptor
 * @Description: 数据权限处理拦截器
 * @Author: Allen
 * @Date: 2025-04-24 22:36
 * @Addr: https://pddon.cn
 */
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class DataPermissionInterceptor extends SqlExplainInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);

        // 不是SELECT操作直接返回
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (!SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }

        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        StringBuilder filterSql = new StringBuilder(boundSql.getSql());

        if (RequestContext.getContext().isSuperManager()
                || RequestContext.getContext().isIgnoreDataPerm()
                || !RequestContext.getContext().getDataPermissionsEnable()
                ) {
            return invocation.proceed();
        } else {
            //拼接数据权限校验sql
            Statement statement = CCJSqlParserUtil.parse(filterSql.toString());
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                SelectBody selectBody = selectStatement.getSelectBody();
                if (selectBody instanceof PlainSelect) {
                    PlainSelect plainSelect = (PlainSelect) selectBody;
                    Expression oldWhere = plainSelect.getWhere();
                    String permSQL = composeDataPermSQL();
                    Expression permExpression = CCJSqlParserUtil.parseCondExpression(permSQL);

                    Expression where = permExpression;
                    if(oldWhere != null){
                        where = new AndExpression(oldWhere, permExpression);
                    }
                    // 修改 WHERE 条件
                    plainSelect.setWhere(where);
                    metaObject.setValue("delegate.boundSql.sql", plainSelect.toString());
                }
            }
            

            return invocation.proceed();
        }
    }

    public String composeDataPermSQL() {
        StringBuffer filterSql = new StringBuffer();
        Map<String, Object> dataPerms = RequestContext.getContext().getDataPermissions();
        String[] tableFields = RequestContext.getContext().getDataPermissionsInfo().get("tableFields");
        String[] tableFieldAlias = RequestContext.getContext().getDataPermissionsInfo().get("tableFieldAlias");
        for(int i=0; i < tableFields.length; i++){
            if(dataPerms.containsKey(tableFields[i])){

                Object permValue = dataPerms.get(tableFields[i]);
                if(permValue == null){
                    //该用户没有此数据权限，直接让条件不成立，返回空查询结果
                    filterSql.append(" AND 1=2 ");
                    continue;
                }
                if(BeanPropertyUtil.isBaseType(permValue)){
                    if(permValue.equals("*")){
                        //拥有该类型所有数据权限，直接不添加该筛选条件
                        continue;
                    }
                    if(permValue instanceof String){
                        filterSql.append(" AND " + tableFieldAlias[i] + "='" + permValue + "' ");
                    }else{
                        filterSql.append(" AND " + tableFieldAlias[i] + "=" + permValue + " ");
                    }
                }else if (permValue.getClass().isArray()){
                    Object[] values = (Object[])permValue;
                    if(values.length == 0){
                        //该用户没有此数据权限，直接让条件不成立，返回空查询结果
                        filterSql.append(" AND 1=2 ");
                        continue;
                    }else if(values.length == 1 && values[0].equals("*")){
                        //拥有该类型所有数据权限，直接不添加该筛选条件
                        continue;
                    }
                    filterSql.append(" AND " + tableFieldAlias[i] + " in (");
                    for(int j=0; j < values.length; j++){
                        if(j > 0){
                            filterSql.append(",");
                        }
                        if(values[j] instanceof String){
                            filterSql.append("'" + values[j] + "'");
                        }else{
                            filterSql.append(values[j]);
                        }
                    }
                    filterSql.append(") ");
                }
            }
        }
        if(filterSql.indexOf(" AND") == 0){
            return filterSql.substring(4);
        }
        return filterSql.toString();
    }
    
    /**
     * 生成拦截对象的代理
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * mybatis配置的属性
     *
     * @param properties mybatis配置的属性
     */
    @Override
    public void setProperties(Properties properties) {

    }
}
