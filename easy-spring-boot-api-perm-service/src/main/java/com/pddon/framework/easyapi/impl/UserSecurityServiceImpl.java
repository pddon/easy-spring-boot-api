package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.dao.*;
import com.pddon.framework.easyapi.dao.entity.*;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
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
    public Set<String> getUserPermissions(String userId) {
        if(StringUtils.isEmpty(userId)){
            return new HashSet<>();
        }
        if(SUPER_ADMIN_USER_ID.equalsIgnoreCase(userId)){
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
}
