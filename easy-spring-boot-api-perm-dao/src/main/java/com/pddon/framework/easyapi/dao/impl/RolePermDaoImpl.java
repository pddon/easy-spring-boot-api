package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.RolePermDao;
import com.pddon.framework.easyapi.dao.entity.RolePerm;
import com.pddon.framework.easyapi.dao.mapper.RolePermMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: RolePermDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class RolePermDaoImpl extends ServiceImpl<RolePermMapper, RolePerm> implements RolePermDao {

    @Override
    public List<RolePerm> getByRoleIds(List<String> roleIds) {
        return this.list(new LambdaQueryWrapper<RolePerm>().in(RolePerm::getRoleId, roleIds));
    }
}
