package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.RolePerm;

import java.util.List;

/**
 * @ClassName: RolePermDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface RolePermDao {
    List<RolePerm> getByRoleIds(List<String> roleIds);

    void saveBatchItems(List<RolePerm> rolePermList);

    boolean removeByRoleId(String roleId);

    boolean removeByRoleIds(String[] ids);
}
