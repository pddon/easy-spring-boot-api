package com.pddon.framework.easyapi.dao.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.RequireDataPermission;
import com.pddon.framework.easyapi.utils.BeanPropertyUtil;
import lombok.extern.slf4j.Slf4j;
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

        RequireDataPermission requireDataPermission = invocation.getMethod().getAnnotation(RequireDataPermission.class);
        if (RequestContext.getContext().isSuperManager()
                || RequestContext.getContext().isIgnoreDataPerm()
                || requireDataPermission == null
                || !requireDataPermission.value()
                ) {
            return invocation.proceed();
        } else {
            //拼接数据权限校验sql
            // TODO:
            Map<String, Object> dataPerms = RequestContext.getContext().getDataPermissions();
            String[] tableFields = requireDataPermission.tableFields();
            String[] tableFieldAlias = requireDataPermission.tableFieldAlias();
            for(int i=0; i < tableFields.length; i++){
                if(dataPerms.containsKey(tableFields[i])){
                    if(filterSql.indexOf(" where ") < 0){
                        filterSql.append("where 1=1 ");
                    }
                    Object permValue = dataPerms.get(tableFields[i]);
                    if(permValue == null){
                        //该用户没有此数据权限，直接让条件不成立，返回空查询结果
                        filterSql.append(" and 1=2 ");
                        continue;
                    }
                    if(BeanPropertyUtil.isBaseType(permValue)){
                        if(permValue instanceof String){
                            filterSql.append(" and " + tableFieldAlias[i] + "='" + permValue + "' ");
                        }else{
                            filterSql.append(" and " + tableFieldAlias[i] + "=" + permValue + " ");
                        }
                    }else if (permValue.getClass().isArray()){
                        Object[] values = (Object[])permValue;
                        if(values.length == 0){
                            //该用户没有此数据权限，直接让条件不成立，返回空查询结果
                            filterSql.append(" and 1=2 ");
                            continue;
                        }
                        filterSql.append(" and " + tableFieldAlias[i] + " in (");
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
            metaObject.setValue("delegate.boundSql.sql", filterSql.toString());
            return invocation.proceed();
        }
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
