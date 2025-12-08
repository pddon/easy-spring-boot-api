package com.pddon.framework.easyapi.dao.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * 用户账号信息
 * @author pddon.com
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
public class BaseTenantEntity extends BaseEntity{
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
    private String tenantId;

    public String getTenantId() {
        return tenantId;
    }

    public BaseTenantEntity setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
}