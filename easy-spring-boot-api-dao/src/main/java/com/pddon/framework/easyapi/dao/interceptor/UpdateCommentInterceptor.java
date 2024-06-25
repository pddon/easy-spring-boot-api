package com.pddon.framework.easyapi.dao.interceptor;

import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.entity.BaseEntity;
import com.pddon.framework.easyapi.dao.entity.BaseHardEntity;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * 记录更新拦截器，用于对更新记录时增加备注信息
 * 更新时间
 * 更新人
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class UpdateCommentInterceptor extends SqlExplainInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (SqlCommandType.UPDATE != ms.getSqlCommandType()) {
            return invocation.proceed();
        }
        Object wrap = args[1];
        Object param = null;
        if(wrap instanceof Map){
            Map wrapMap = (Map) wrap;
            if(wrapMap.containsKey("et")){
                param = wrapMap.get("et");
            }
        }
        if(param == null){
            return invocation.proceed();
        }
        String userId = RequestContext.getContext().getUserId();
        if(param instanceof BaseHardEntity){
            BaseHardEntity entity = (BaseHardEntity) param;
            entity.setChgTime(new Date());
            if(!StringUtils.isEmpty(userId)){
                entity.setChgUserId(userId);
            }
        }else if(param instanceof Map) {
            Map map = (Map) param;
            map.put("chgTime", new Date());
            if(!StringUtils.isEmpty(userId)){
                map.put("chgUserId", Long.valueOf(userId));
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
