package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.RoleDataPermissionMntDao;
import com.pddon.framework.easyapi.dao.entity.RoleDataPermission;
import com.pddon.framework.easyapi.dao.mapper.RoleDataPermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: RoleDataPermissionMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class RoleDataPermissionMntDaoImpl extends ServiceImpl<RoleDataPermissionMapper, RoleDataPermission> implements RoleDataPermissionMntDao {

    @Autowired
    private RoleDataPermissionMapper roleDataPermissionMapper;

    @Override
    public boolean deletePerms(String roleId, String permId, List<Object> values) {
        return this.remove(new LambdaQueryWrapper<RoleDataPermission>()
                .eq(RoleDataPermission::getRoleId, roleId)
                .eq(RoleDataPermission::getPermId, permId)
                .in(RoleDataPermission::getPermValue, values));
    }

    @Override
    public List<RoleDataPermission> getByRoleIds(List<String> roleIds) {
        return this.lambdaQuery().in(RoleDataPermission::getRoleId, roleIds).list();
    }

    @Override
    public boolean saveItems(List<RoleDataPermission> perms) {
        return this.saveBatch(perms);
    }
}
