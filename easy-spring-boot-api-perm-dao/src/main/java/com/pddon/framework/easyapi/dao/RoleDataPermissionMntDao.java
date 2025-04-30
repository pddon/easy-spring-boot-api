package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.RoleDataPermission;

import java.util.List;

/**
 * @ClassName: RoleDataPermissionMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
public interface RoleDataPermissionMntDao {
    boolean deletePerms(String roleId, String permId, List<Object> values);

    List<RoleDataPermission> getByRoleIds(List<String> roleIds);

    boolean saveItems(List<RoleDataPermission> perms);
}
