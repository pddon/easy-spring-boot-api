package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.ApplicationManager;
import com.pddon.framework.easyapi.SessionManager;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.CacheMethodResultEvict;
import com.pddon.framework.easyapi.annotation.LockDistributed;
import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.*;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.consts.UserAccountStatus;
import com.pddon.framework.easyapi.dao.entity.*;
import com.pddon.framework.easyapi.dto.Session;
import com.pddon.framework.easyapi.dto.UserAuthenticationToken;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
@IgnoreTenant
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

    @Autowired
    private UserLoginRecordDao userLoginRecordDao;

    @Autowired
    private ApplicationManager applicationManager;

    @Override
    public boolean isSuperManager(String userId){
        return SUPER_ADMIN_USER_ID.equalsIgnoreCase(userId);
    }

    @Override
    @LockDistributed
    @Transactional
    public void checkAndCreateSuperManager(){
        if(baseUserDao.existUserId(SUPER_ADMIN_USER_ID)){
            return;
        }
        BaseUser user = new BaseUser();
        user.setUserId(SUPER_ADMIN_USER_ID)
                .setUsername("EasyApi超管")
                .setAccountStatus(UserAccountStatus.ACTIVE.name())
                .setPassword(EncryptUtils.encryptMD5Hex(EncryptUtils.encryptMD5Hex("88889999")))
                .setTenantId("default")
                .setCrtUserId("system");
        baseUserDao.saveUser(user);
    }

    @Override
    public BaseUser queryBySessionId(String sessionId) {
        return baseUserDao.getBySessionId(sessionId);
    }

    public Set<String> getAllPermissions(){
        List<PermItem> items = permItemDao.getAllPerms();
        return items.stream().map(PermItem::getPermId).collect(Collectors.toSet());
    }

    @Override
    @CacheMethodResult(prefix = "User:Perms", id = "currentUserId", keyMode = CacheKeyMode.CUSTOM_ID, needCacheField = "cacheable", expireSeconds = 3600)
    public Set<String> getUserPermissions(String currentUserId, boolean cacheable) {
        if(StringUtils.isEmpty(currentUserId)){
            return new HashSet<>();
        }
        if(SUPER_ADMIN_USER_ID.equalsIgnoreCase(currentUserId)){
            RequestContext.getContext().setSuperManager(true);
            //超级管理员具有所有权限
            return getAllPermissions();
        }
        Set<String> perms = new HashSet<>();
        //获取用户权限
        List<UserPerm> userPerms = userPermDao.getPermsByUserId(currentUserId);
        if(userPerms != null){
            perms.addAll(userPerms.stream().map(UserPerm::getPermId).collect(Collectors.toList()));
        }
        //获取用户角色
        List<UserRole> roles = userRoleDao.getRolesByUserId(currentUserId);
        List<String> roleIds = roles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        //获取角色下所有权限
        List<RolePerm> rolePerms = rolePermDao.getByRoleIds(roleIds);
        if(rolePerms != null){
            perms.addAll(rolePerms.stream().map(RolePerm::getPermId).collect(Collectors.toList()));
        }
        return perms;
    }

    @CacheMethodResultEvict(prefix = "User:Perms", id = "currentUserId", keyMode = CacheKeyMode.CUSTOM_ID, expireSeconds = 3600)
    @Override
    public void login(String currentUserId, String password, String loginType) {
        // 获取当前用户
        Subject currentUser = SecurityUtils.getSubject();
        // 创建用户令牌，通常是用户名和密码
        UserAuthenticationToken token = new UserAuthenticationToken(null, currentUserId, EncryptUtils.encryptMD5Hex(password));
        RequestContext.getContext().setShiroSessionEnable(true);
        try{
            currentUser.login(token);
        }catch (AuthenticationException e){
            throw e;
        }
        if(SUPER_ADMIN_USER_ID.equalsIgnoreCase(currentUserId)){
            RequestContext.getContext().setSuperManager(true);
        }
        //获取用户信息
        BaseUser user = baseUserDao.getByUserId(currentUserId);
        //创建会话信息
        Session session = sessionManager.getCurrentSession(true);
        session.setCountryCode(user.getCountryCode())
                .setUserId(user.getUserId())
                .setUsername(user.getUsername())
                .setChannelId(user.getTenantId())
                .setSuperManager(SUPER_ADMIN_USER_ID.equalsIgnoreCase(currentUserId));
        sessionManager.update(session);
        RequestContext.getContext().setSession(session);
        Date loginTime = new Date();
        baseUserDao.updateUserSession(session.getSessionId(), loginTime, user.getUserId());
        UserLoginRecord record = new UserLoginRecord();
        record.setUserId(user.getUserId())
                 .setLoginTime(loginTime)
                .setLoginType(loginType)
                .setUsername(user.getUsername())
                .setDeviceId(RequestContext.getContext().getClientId())
                .setSessionId(session.getSessionId())
                .setTenantId(user.getTenantId());
        userLoginRecordDao.addLoginRecord(record);
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
        ((UserSecurityServiceImpl)AopContext.currentProxy()).evictPermsCache(RequestContext.getContext().getUserId());
    }

    @Override
    public BaseUser queryByUserId(String userId) {
        return baseUserDao.getByUserId(userId);
    }

    @CacheMethodResultEvict(prefix = "User:Perms", id = "userId", expireSeconds = 3600)
    public void evictPermsCache(String userId){
        if(log.isTraceEnabled()){
            log.trace("userId: {}, evict perms cache success!");
        }
    }
}
