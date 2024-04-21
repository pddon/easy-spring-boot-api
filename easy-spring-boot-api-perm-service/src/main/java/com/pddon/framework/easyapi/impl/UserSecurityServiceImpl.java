package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.CacheMethodResultEvict;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.*;
import com.pddon.framework.easyapi.dao.entity.*;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName: UserSecurityServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 23:41
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class UserSecurityServiceImpl implements UserSecurityService {

    private static final String SUPER_ADMIN_USER_ID = "EASY_API_SUPER_ADMIN";

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private BaseUserDao baseUserDao;

    @Autowired
    private UserPermDao userPermDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private PermItemDao permItemDao;

    @Override
    public BaseUser queryBySessionId(String sessionId) {
        return baseUserDao.getBySessionId(sessionId);
    }

    public Set<String> getAllPermissions(){
        List<PermItem> items = permItemDao.getAllPerms();
        return items.stream().map(PermItem::getPermId).collect(Collectors.toSet());
    }

    @Override
    @CacheMethodResult(prefix = "User:Perms", id = "userId", needCacheField = "cacheable", expireSeconds = 3600)
    public Set<String> getUserPermissions(String userId, boolean cacheable) {
        if(StringUtils.isEmpty(userId)){
            return new HashSet<>();
        }
        if(SUPER_ADMIN_USER_ID.equalsIgnoreCase(userId)){
            //超级管理员具有所有权限
            return getAllPermissions();
        }
        Set<String> perms = new HashSet<>();
        //获取用户权限
        List<UserPerm> userPerms = userPermDao.getPermsByUserId(userId);
        if(userPerms != null){
            perms.addAll(userPerms.stream().map(UserPerm::getPermId).collect(Collectors.toList()));
        }
        //获取用户角色
        List<UserRole> roles = userRoleDao.getRolesByUserId(userId);
        List<String> roleIds = roles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        //获取角色下所有权限
        List<RolePerm> rolePerms = rolePermDao.getByRoleIds(roleIds);
        if(rolePerms != null){
            perms.addAll(rolePerms.stream().map(RolePerm::getPermId).collect(Collectors.toList()));
        }
        return perms;
    }

    @Override
    public void login(String userId, String password) {
        // 获取当前用户
        Subject currentUser = SecurityUtils.getSubject();
        // 创建用户令牌，通常是用户名和密码
        UsernamePasswordToken token = new UsernamePasswordToken(userId, password);
        try{
            currentUser.login(token);
        }catch (AuthenticationException e){
            throw e;
        }
        //获取用户信息
        BaseUser user = baseUserDao.getByUserId(userId);
        //更新信息
        RequestContext.getContext().setShiroSessionEnable(true);
        //创建会话信息
        Session session = sessionManager.getCurrentSession(true);
        session.setCountryCode(user.getCountryCode())
                .setUserId(user.getUserId());
        sessionManager.update(session);
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
        ((UserSecurityServiceImpl)AopContext.currentProxy()).evictPermsCache(RequestContext.getContext().getUserId());
    }

    @CacheMethodResultEvict(prefix = "User:Perms", id = "userId", expireSeconds = 3600)
    public void evictPermsCache(String userId){
        if(log.isTraceEnabled()){
            log.trace("userId: {}, evict perms cache success!");
        }
    }
}
