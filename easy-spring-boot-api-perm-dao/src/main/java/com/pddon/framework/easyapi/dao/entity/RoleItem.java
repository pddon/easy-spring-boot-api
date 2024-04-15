package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户角色
 * @author pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("role_item")
public class RoleItem extends BaseTenantEntity{
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 角色ID
     * @author pddon.com
     */
     private String roleId;

    /**
     * 角色简介
     * @author pddon.com
     */
    private String intro;

    /**
     * 是否可传播
     * @author pddon.com
     */
    private boolean propagable;

}