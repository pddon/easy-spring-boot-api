package com.pddon.framework.easyapi.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @ClassName: UserDataPermission
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:21
 * @Addr: https://pddon.cn
 */
@Getter
@Setter
@ToString
@Accessors(chain=true)
@TableName("user_data_permission")
public class UserDataPermission extends BaseHardTenantEntity {
    /**
     * 记录ID
     * @author pddon.com
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据范围权限id
     * @author pddon.com
     */
    private String permId;

    /**
     * 所属用户的ID
     * @author pddon.com
     */
    private String userId;

    /**
     * 拥有的权限值
     * @author pddon.com
     */
    private String permValue;

    /**
     *
     * @author pddon.com
     */
    private String comment;
}
