package com.pddon.framework.easyapi.filter;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.properties.SystemParameterRenameProperties;
import com.pddon.framework.easyapi.utils.HttpHelper;
import com.pddon.framework.easyapi.utils.StaticResourceUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserSecurityService userSecurityService;

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
        BaseUser user = userSecurityService.queryBySessionId(sessionId);
        if(user == null){
            throw new BusinessException(ErrorCodes.ACCOUNT_NOT_FOUND);
        }
        //更新最近登录时间
        userSecurityService.updateLastLoginTime(user.getUserId());
        UserAuthenticationToken token = new UserAuthenticationToken(sessionId, user.getUserId(), user.getPassword());
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
        if(!sessionManager.exists(sessionId)){
            throw new BusinessException(ErrorCodes.INVALID_SESSION_ID).setParam(sessionId);
        }
        RequestContext.getContext().setShiroSessionEnable(true);
        RequestContext.getContext().setAttachment(SystemParameterRenameProperties.DEFAULT_PARAM_MAP.get(SystemParameterRenameProperties.SESSION_ID), sessionId);
        return executeLogin(request, response);
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        AuthenticationToken token = this.createToken(request, response);
        if (token == null) {
            String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken must be created in order to execute a login attempt.";
            throw new IllegalStateException(msg);
        } else {
            Subject subject = this.getSubject(request, response);
            subject.login(token);
            return this.onLoginSuccess(token, subject, request, response);
        }
    }
}
