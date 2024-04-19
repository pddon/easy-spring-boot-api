package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.dao.entity.BaseUser;

import java.util.Set;

/**
 * @ClassName: UserSecurityService
 * @Description: 用户访问权限控制服务
 * @Author: Allen
 * @Date: 2024-04-15 23:37
 * @Addr: https://pddon.cn
 */
public interface UserSecurityService {
    /**
     * 通过会话ID查询用户信息
     * @param sessionId
     * @return {@link BaseUser}
     * @author: Allen
     * @Date: 2024/4/16 0:13
     */
    BaseUser queryBySessionId(String sessionId);

    /**
     * 通过用户ID查询用户权限信息
     * @param userId
     * @param cacheable
     * @return {@link Set<String>}
     * @author: Allen
     * @Date: 2024/4/16 0:13
     */
    @CacheMethodResult(prefix = "User:Perms", id = "userId", needCacheField = "cacheable", expireSeconds = 3600)
    Set<String> getUserPermissions(String userId, boolean cacheable);
}
