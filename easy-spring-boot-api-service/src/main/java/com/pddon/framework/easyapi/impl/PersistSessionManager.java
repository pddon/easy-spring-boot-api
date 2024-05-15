package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.CacheManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 默认不启用会话，需要配置开启才支持持久化用户登录会话状态
 */
@Service
@ConditionalOnProperty(name={"easyapi.enablePersistSession"},havingValue = "true")
@Primary
@IgnoreTenant
@Slf4j
public class PersistSessionManager extends DefaultSessionManagerImpl {

    @Resource
    @Lazy
    private BaseUserDao baseUserDao;

    public PersistSessionManager() {
        super.expireSeconds = 180;
    }

    @Resource
    @Lazy
    @Override
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Session recoverSession(String sessionId,  BaseUser user){
        Session session = new Session();
        RequestContext context = RequestContext.getContext();
        session.setSessionId(sessionId)
                .setAppId(context.getAppId())
                .setChannelId(context.getChannelId())
                .setClientId(context.getClientId())
                .setClientIp(context.getClientIp())
                .setCountryCode(context.getCountryCode())
                .setCurrency(context.getCurrency())
                .setLocale(user.getLocale())
                .setTimeZone(context.getTimeZone())
                .setUserId(user.getUserId())
                .setVersionCode(context.getVersionCode())
        ;
        session.setUserId(user.getUserId().toString())
                .setChannelId(user.getTenantId())
                .setCountryCode(user.getCountryCode())
                .setUsername(user.getUsername())
                .setNewSession(false)
                .setRecoverFromDB(true);

        this.update(session);
        return session;
    }

    @Override
    public Session get(String sessionId) {
        Session session = super.get(sessionId);

        if(session == null || StringUtils.isBlank(session.getUserId())){
            //尝试从数据库中恢复会话
            BaseUser user = baseUserDao.getBySessionId(sessionId);
            if(user != null){
                session = this.recoverSession(sessionId, user);
            }
        }
        return session;
    }

    @Override
    public boolean exists(String sessionId) {
        boolean re = super.exists(sessionId);

        if(!re){
            if(baseUserDao.getBySessionId(sessionId) != null){
                re = true;
            }
        }
        return re;
    }
}
