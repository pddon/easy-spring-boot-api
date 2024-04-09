package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.BaseApplicationConfig;

/**
 * @ClassName: ApplicationConfigDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-03-01 22:04
 * @Addr: https://pddon.cn
 */
public interface BaseApplicationConfigDao<K extends BaseApplicationConfig> {
    K getByAppId(String appId);
}
