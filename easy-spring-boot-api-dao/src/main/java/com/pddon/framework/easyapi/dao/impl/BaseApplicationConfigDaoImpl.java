package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.BaseApplicationConfigDao;
import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;
import com.pddon.framework.easyapi.dao.mapper.BaseApplicationConfigMapper;

/**
 * @ClassName: BaseApplicationConfigDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
public class BaseApplicationConfigDaoImpl<T extends BaseApplicationConfigMapper<K>, K extends BaseApplicationConfig> extends ServiceImpl<T, K> implements BaseApplicationConfigDao<K> {

    @Override
    public K getByAppId(String appId) {
        Wrapper<K> wrapper = new LambdaQueryWrapper<K>()
                .eq(BaseApplicationConfig::getAppId, appId)
                .ne(BaseApplicationConfig::getDeleted , 1);
        return super.getOne(wrapper);
    }
}
