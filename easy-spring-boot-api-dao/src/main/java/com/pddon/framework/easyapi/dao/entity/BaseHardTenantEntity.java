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
public class BaseHardTenantEntity extends BaseHardEntity{
    /**
     * 应用所属渠道ID(租户ID)
     * @author pddon.com
     */
    private String tenantId;

}