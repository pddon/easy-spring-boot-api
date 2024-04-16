package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.UserPermDao;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.UserPerm;
import com.pddon.framework.easyapi.dao.mapper.UserPermMapper;
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
public class UserPermDaoImpl extends ServiceImpl<UserPermMapper, UserPerm> implements UserPermDao {

    @Override
    public List<UserPerm> getPermsByUserId(String userId) {
        return this.list(new LambdaQueryWrapper<UserPerm>().eq(UserPerm::getUserId, userId));
    }
}
