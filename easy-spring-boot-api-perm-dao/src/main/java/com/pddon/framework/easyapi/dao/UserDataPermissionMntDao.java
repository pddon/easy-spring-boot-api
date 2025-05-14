package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.UserDataPermission;

import java.util.List;

/**
 * @ClassName: UserDataPermissionMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
public interface UserDataPermissionMntDao {
    boolean deletePerms(String userId, String permId, List<Object> values);

    List<UserDataPermission> getByUserIds(List<String> userIds);

    boolean saveItems(List<UserDataPermission> perms);

    List<UserDataPermission> getByUserId(String userId);

    List<UserDataPermission> getByPermId(String userId, String permId);
}
