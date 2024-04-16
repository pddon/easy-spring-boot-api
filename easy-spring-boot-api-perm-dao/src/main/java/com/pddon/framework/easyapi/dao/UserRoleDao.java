package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.UserRole;

import java.util.List;

/**
 * @ClassName: UserRoleDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface UserRoleDao {
    List<UserRole> getRolesByUserId(String userId);
}
