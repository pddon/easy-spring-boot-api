package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.config.SecurityConfigProperties;
import com.pddon.framework.easyapi.consts.ErrorCodes;
import com.pddon.framework.easyapi.dao.consts.UserAccountStatus;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @ClassName: UserAuthorizingRealm
 * @Description: 用户登录/获取授权信息
 * @Author: Allen
 * @Date: 2024-04-15 23:41
 * @Addr: https://pddon.cn
 */
@Component
public class UserAuthorizingRealm extends AuthorizingRealm {

    @Autowired
    private SecurityConfigProperties securityConfigProperties;

    @Autowired
    private UserSecurityService userSecurityService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UserAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userId = (String) principals.getPrimaryPrincipal();
        //用户权限列表
        Set<String> permsSet = userSecurityService.getUserPermissions(userId, securityConfigProperties.isCacheable());

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UserAuthenticationToken userToken = (UserAuthenticationToken) token;
        String userId = userToken.getUserId();
        String sessionId = userToken.getSessionId();

        //根据sessionId，查询用户信息
        BaseUser user = userSecurityService.queryByUserId(userId);
        //账户未找到
        if (user == null) {
            throw new UnknownAccountException(ErrorCodes.ACCOUNT_NOT_FOUND.getMsgCode());
        }
        if(StringUtils.isNotEmpty(sessionId)){
            //非登录请求才需要判断会话是否需要失效
            if(securityConfigProperties.sessionCanExpire() && ((user.getLastLoginTime().getTime() + securityConfigProperties.getSessionLiveTimeSeconds() * 1000) < System.currentTimeMillis())){
                throw new ExpiredCredentialsException(ErrorCodes.INVALID_SESSION_ID.getMsgCode());
            }
        }

        //账号锁定
        if (UserAccountStatus.DISABLE.name().equalsIgnoreCase(user.getAccountStatus())) {
            throw new DisabledAccountException(ErrorCodes.ACCOUNT_DISABLED.getMsgCode());
        }else if (UserAccountStatus.FROZEN.name().equalsIgnoreCase(user.getAccountStatus())) {
            throw new LockedAccountException(ErrorCodes.ACCOUNT_LOCKED.getMsgCode());
        }

        return new SimpleAuthenticationInfo(user, EncryptUtils.encryptMD5Hex(user.getPassword()), getName());
    }
}
