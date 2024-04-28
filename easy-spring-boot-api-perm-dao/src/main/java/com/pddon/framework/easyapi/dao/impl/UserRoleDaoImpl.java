package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.UserRoleDao;
import com.pddon.framework.easyapi.dao.entity.UserRole;
import com.pddon.framework.easyapi.dao.mapper.UserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: UserRoleDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class UserRoleDaoImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleDao {

    @Override
    public List<UserRole> getRolesByUserId(String userId) {
        return this.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    @Override
    public boolean removeByUserId(String userId) {
        return this.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
    }

    @Override
    public boolean saveItems(List<UserRole> userRoles) {
        return this.saveBatch(userRoles);
    }
}
