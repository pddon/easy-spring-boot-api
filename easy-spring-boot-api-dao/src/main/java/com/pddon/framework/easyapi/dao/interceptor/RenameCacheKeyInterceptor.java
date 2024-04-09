package com.pddon.framework.easyapi.dao.interceptor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Map;
import java.util.Properties;

@Intercepts({ @Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class RenameCacheKeyInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        if(args.length > 2){
            if(args[1] instanceof Map){
                Map map = (Map) args[1];
                if(map.containsKey("page")){
                    Object param = map.get("page");
                    if(param instanceof Page){
                        Page page = (Page) param;
                        if(args[2] instanceof RowBounds){
                            RowBounds rowBounds = (RowBounds) args[2];
                            args[2] = new RowBounds(Long.valueOf((page.getCurrent() - 1) * page.getSize()).intValue(), Long.valueOf(page.getSize()).intValue());
                        }
                    }
                }
            }
        }
        Object re =  invocation.proceed();

        return re;
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
