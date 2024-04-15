package com.pddon.framework.easyapi.filter;

import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * @ClassName: UserAuthenticatingFilter
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 23:45
 * @Addr: https://pddon.cn
 */
@Slf4j
public class UserAuthenticatingFilter extends AuthenticatingFilter {

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        String sessionId = RequestContext.getContext().getSessionId();
        if(StringUtils.isEmpty(sessionId)){
            return null;
        }
        UserAuthenticationToken token = new UserAuthenticationToken(sessionId);
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        //获取请求token，如果token不存在，直接返回401
        String sessionId = RequestContext.getContext().getSessionId();
        if (StringUtils.isBlank(sessionId)) {
            throw new BusinessException(ErrorCodes.INVALID_SESSION_ID);
        }

        return executeLogin(request, response);
    }
}
