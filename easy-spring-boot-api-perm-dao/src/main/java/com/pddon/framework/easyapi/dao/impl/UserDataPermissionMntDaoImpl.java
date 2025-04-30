package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.UserDataPermissionMntDao;
import com.pddon.framework.easyapi.dao.entity.UserDataPermission;
import com.pddon.framework.easyapi.dao.mapper.UserDataPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: UserDataPermissionMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class UserDataPermissionMntDaoImpl extends ServiceImpl<UserDataPermissionMapper, UserDataPermission> implements UserDataPermissionMntDao {

    @Autowired
    private UserDataPermissionMapper userDataPermissionMapper;

    @Override
    public boolean deletePerms(String userId, String permId, List<Object> values) {
        return this.remove(new LambdaQueryWrapper<UserDataPermission>().eq(UserDataPermission::getUserId, userId)
                .eq(UserDataPermission::getPermId, permId)
                .in(UserDataPermission::getPermValue, values));
    }

    @Override
    public List<UserDataPermission> getByUserIds(List<String> userIds) {
        return this.lambdaQuery().in(UserDataPermission::getUserId, userIds).list();
    }

    @Override
    public List<UserDataPermission> getByUserId(String userId) {
        return this.lambdaQuery().eq(UserDataPermission::getUserId, userId).list();
    }

    @Override
    public boolean saveItems(List<UserDataPermission> perms) {
        return this.saveBatch(perms);
    }
}
