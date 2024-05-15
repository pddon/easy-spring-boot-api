package com.pddon.framework.easyapi.dao.tenant;

import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.AllArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

/**
 * @ClassName: EasyApiTenantHandler
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-11 22:34
 * @Addr: https://pddon.cn
 */
public class EasyApiTenantHandler implements TenantHandler {

    // 用于设置租户id的值
    @Override
    public Expression getTenantId() {
        String channelId = RequestContext.getContext().getChannelId();
        if(RequestContext.getContext().getSession() != null && StringUtils.isNotEmpty(RequestContext.getContext().getSession().getChannelId())){
            channelId = RequestContext.getContext().getSession().getChannelId();
        }
        if(StringUtils.isNotEmpty(channelId)){
            return new StringValue(channelId);
        }
        return new StringValue("");
    }

    // 设置租户id所对应的表字段
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }

    // 设置表级过滤器用于设置哪些表不需要这个多租户操作，即操作sql的时候，不带shopId
    @Override
    public boolean doTableFilter(String tableName) {
        return EntityManager.isIgnoreTable(tableName);
    }
}
