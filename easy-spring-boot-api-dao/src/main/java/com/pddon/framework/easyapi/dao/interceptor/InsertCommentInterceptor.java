package com.pddon.framework.easyapi.dao.interceptor;

import com.baomidou.mybatisplus.extension.plugins.SqlExplainInterceptor;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.entity.BaseEntity;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * 记录新增拦截器，用于对新增记录时增加备注信息
 * 更新时间
 * 更新人
 */
@Intercepts({@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
public class InsertCommentInterceptor extends SqlExplainInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (SqlCommandType.INSERT != ms.getSqlCommandType()) {
            return invocation.proceed();
        }
        Object param = args[1];
        if(param == null){
            return invocation.proceed();
        }
        String userId = RequestContext.getContext().getUserId();
        if(param instanceof BaseEntity){
            BaseEntity entity = (BaseEntity) param;
            entity.setCrtTime(new Date());
            if(!StringUtils.isEmpty(userId)){
                entity.setCrtUserId(userId);
            }
        }else if(param instanceof Map) {
            Map map = (Map) param;
            map.put("crtTime", new Date());
            if(!StringUtils.isEmpty(userId)){
                map.put("crtUserId", Long.valueOf(userId));
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
