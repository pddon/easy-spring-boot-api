package com.pddon.framework.easyapi.dto.req;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName: UpdateRoleRequest
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:30
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class UpdateRoleRequest implements Serializable {
    /**
     * 记录ID
     */
    private Long id;

    /**
     * 角色ID
     * @author pddon.com
     */
    private String roleId;

    /**
     * 权限名
     * @author pddon.com
     */
    private String roleName;

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

    /**
     * 角色拥有的权限ID列表
     */
    private List<String> permIds;
}
