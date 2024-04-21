package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.BaseUser;

/**
 * @ClassName: BaseUserDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-10 18:11
 * @Addr: https://pddon.cn
 */
public interface BaseUserDao<K extends BaseUser> {
    BaseUser getBySessionId(String sessionId);

    BaseUser getByUserId(String userId);
}
