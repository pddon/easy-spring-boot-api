package com.pddon.framework.easyapi.dto.resp;

import com.pddon.framework.easyapi.dao.entity.BaseEntity;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @ClassName: RoleDetailDto
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:08
 * @Addr: https://pddon.cn
 */
@Data
@Accessors(chain = true)
public class RoleDetailDto extends BaseEntity {
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
     * 角色名
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
    private List<PermItem> perms;
}
