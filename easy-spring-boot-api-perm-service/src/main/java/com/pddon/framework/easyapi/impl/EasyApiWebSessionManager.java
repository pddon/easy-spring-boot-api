package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.AdaptorSession;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.HttpHelper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.AllArgsConstructor;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * @ClassName: EasyApiWebSessionManager
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-21 22:47
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
public class EasyApiWebSessionManager extends DefaultWebSessionManager {

    private SessionManager sessionManager;

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        Serializable sessionId = super.getSessionId(request, response);
        if(StringUtils.isEmpty(sessionId)){
            sessionId = HttpHelper.getParam(request,
                    SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID));
        }
        return sessionId;
    }

    @Override
    public Session getSession(SessionKey key) throws SessionException {
        /*if(RequestContext.getContext().isShiroSessionEnable()){
            return super.getSession(key);
        }else{
            String sessionId = null;
            if(key != null && StringUtils.isNotEmpty(key.getSessionId())){
                sessionId = key.getSessionId().toString();
            }
            if(StringUtils.isEmpty(sessionId)){
                sessionId = RequestContext.getContext().getSessionId();
            }
            com.pddon.framework.easyapi.dto.Session sessionDto = sessionId != null ? sessionManager.get(sessionId) : sessionManager.getCurrentSession(true);
            RequestContext.getContext().setSession(sessionDto);
            return new AdaptorSession(sessionDto);
        }*/
        return super.getSession(key);
    }
}
