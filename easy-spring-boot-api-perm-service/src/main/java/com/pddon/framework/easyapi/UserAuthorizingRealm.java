package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dao.consts.UserAccountStatus;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
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
    /**
     * 默认会话ID一天时间过期
     */
    @Value("${easyapi.session.liveTimeSeconds:86400}")
    private Long sessionLiveTimeSeconds;

    @Autowired
    private UserSecurityService userSecurityService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UserAuthenticationToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        BaseUser user = (BaseUser) principals.getPrimaryPrincipal();
        String userId = user.getUserId();

        //用户权限列表
        Set<String> permsSet = userSecurityService.getUserPermissions(userId);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String sessionId = (String) token.getPrincipal();

        //根据sessionId，查询用户信息
        BaseUser user = userSecurityService.queryBySessionId(sessionId);
        //token失效
        if (user == null || (user.getLastLoginTime().getTime() + sessionLiveTimeSeconds * 1000) < System.currentTimeMillis()) {
            throw new IncorrectCredentialsException("sessionId失效，请重新登录");
        }

        //账号锁定
        if (UserAccountStatus.DISABLE.name().equalsIgnoreCase(user.getAccountStatus())) {
            throw new LockedAccountException("账号已被禁用,请联系管理员");
        }else if (UserAccountStatus.FROZEN.name().equalsIgnoreCase(user.getAccountStatus())) {
            throw new LockedAccountException("账号已被冻结,请联系管理员");
        }

        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
    }
}
