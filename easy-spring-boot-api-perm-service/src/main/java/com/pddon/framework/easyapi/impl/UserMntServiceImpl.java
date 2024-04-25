package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.UserMntService;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.UserPermDao;
import com.pddon.framework.easyapi.dao.UserRoleDao;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.UserPerm;
import com.pddon.framework.easyapi.dao.entity.UserRole;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName: UserMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 17:28
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class UserMntServiceImpl implements UserMntService {
    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public UserInfoDto getCurrentUserInfo() {
        UserInfoDto userInfoDto = new UserInfoDto();
        String userId = RequestContext.getContext().getSession().getUserId();
        Set<String> perms = userSecurityService.getUserPermissions(userId, true);
        BaseUser user = userSecurityService.queryByUserId(userId);
        BeanUtils.copyProperties(user, userInfoDto);
        userInfoDto.setPerms(perms);
        //获取用户权限
        List<UserRole> userRoles = userRoleDao.getRolesByUserId(userId);
        userInfoDto.setRoleIds(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet()));
        return userInfoDto;
    }
}
