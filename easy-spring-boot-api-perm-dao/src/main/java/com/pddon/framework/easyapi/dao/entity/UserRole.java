package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 用户拥有的角色信息
 * @author pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("user_role")
public class UserRole extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     * @author pddon.com
     */
    private String userId;

    /**
     * 角色ID
     * @author pddon.com
     */
     private String roleId;

}