package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dto.UserSimpleSession;
import lombok.AllArgsConstructor;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;

import java.io.Serializable;


/**
 * @ClassName: EasyApiCacheSessionDAO
 * @Description: 融合shiro会话与easyapi会话管理
 * @Author: Allen
 * @Date: 2024-04-16 16:44
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
public class EasyApiCacheSessionDAO extends EnterpriseCacheSessionDAO {

    private static final String SHIRO_SESSION_KEY = "PERM:S:%s";
    private SessionManager sessionManager;

    private CacheManager cacheManager;

    @Override
    protected Serializable doCreate(Session session) {
        com.pddon.framework.easyapi.dto.Session sessionDto = sessionManager.getCurrentSession(true);
        this.assignSessionId(session, sessionDto.getSessionId());
        cacheManager.set(String.format(SHIRO_SESSION_KEY, sessionDto.getSessionId()), session, null);
        return sessionDto.getSessionId();
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        Session session = super.doReadSession(sessionId);
        if(session != null){
            return session;
        }
        return cacheManager.get(String.format(SHIRO_SESSION_KEY, sessionId), UserSimpleSession.class);
    }

    @Override
    protected void doUpdate(Session session) {
        super.doUpdate(session);
        cacheManager.set(String.format(SHIRO_SESSION_KEY, session.getId().toString()), session, null);
    }

    @Override
    protected void doDelete(Session session) {
        if (session.getId() == null) {
            return;
        }
        super.doDelete(session);
        sessionManager.remove(session.getId().toString());
        cacheManager.remove(String.format(SHIRO_SESSION_KEY, session.getId().toString()));
    }
}
