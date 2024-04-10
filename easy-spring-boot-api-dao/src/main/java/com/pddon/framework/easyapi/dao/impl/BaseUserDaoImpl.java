package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.mapper.BaseUserMapper;

import java.util.List;

/**
 * @ClassName: BaseUserDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
public class BaseUserDaoImpl<T extends BaseUserMapper<K>, K extends BaseUser> extends ServiceImpl<T, K> implements BaseUserDao<K> {


    @Override
    public K getBySessionId(String sessionId) {
        List<K> list = this.list(new LambdaQueryWrapper<K>().eq(BaseUser::getSessionId, sessionId).ne(BaseUser::getDeleted, 1));
        return list.size() > 0 ? list.get(0) : null;
    }
}
