package com.pddon.framework.easyapi.filter;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.HttpHelper;
import com.pddon.framework.easyapi.utils.StaticResourceUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * @ClassName: UserAuthenticatingFilter
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 23:45
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
@Slf4j
public class UserAuthenticatingFilter extends AuthenticatingFilter {

    private final SessionManager sessionManager;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求会话ID
        String[] params = HttpHelper.getParams(request,
                SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID),
                SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE));
        String sessionId = params[0];
        String locale = params[1];
        if(StringUtils.isNotEmpty(locale)){
            RequestContext.getContext().setAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE), locale);
        }
        if(StringUtils.isEmpty(sessionId)){
            return null;
        }
        RequestContext.getContext().setAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), sessionId);
        Session session = sessionManager.get(sessionId);
        UserAuthenticationToken token = new UserAuthenticationToken(sessionId, session);
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求会话ID
        String[] params = HttpHelper.getParams(request,
                SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID),
                SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE));
        String sessionId = params[0];
        String locale = params[1];
        if(StringUtils.isNotEmpty(locale)){
            RequestContext.getContext().setAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.LOCALE), locale);
        }
        if (StringUtils.isBlank(sessionId)) {
            if(StaticResourceUtils.isStaticResourceRequest(WebUtils.toHttp(request))){
                super.redirectToLogin(request, response);
                return false;
            }
            throw new BusinessException(ErrorCodes.NEED_SESSION_ID);
        }
        RequestContext.getContext().setAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), sessionId);
        return executeLogin(request, response);
    }
}
