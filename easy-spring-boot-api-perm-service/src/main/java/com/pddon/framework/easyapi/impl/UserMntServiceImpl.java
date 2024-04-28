package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.UserMntService;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.UserPermDao;
import com.pddon.framework.easyapi.dao.UserRoleDao;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserPassRequest;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.UserPerm;
import com.pddon.framework.easyapi.dao.entity.UserRole;
import com.pddon.framework.easyapi.dao.dto.request.UserListRequest;
import com.pddon.framework.easyapi.dao.dto.request.AddUserRequest;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserRequest;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EncryptUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    @Autowired
    private UserPermDao userPermDao;

    @Autowired
    private BaseUserDao baseUserDao;

    @Override
    public UserInfoDto getUserInfo(String userId) {
        UserInfoDto userInfoDto = new UserInfoDto();
        if(StringUtils.isEmpty(userId)){
            userId = RequestContext.getContext().getSession().getUserId();
        }
        Set<String> perms = userSecurityService.getUserPermissions(userId, true);
        BaseUser user = userSecurityService.queryByUserId(userId);
        BeanUtils.copyProperties(user, userInfoDto);
        userInfoDto.setPerms(perms);
        //获取用户权限
        List<UserRole> userRoles = userRoleDao.getRolesByUserId(userId);
        userInfoDto.setRoleIds(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toSet()));
        return userInfoDto;
    }

    @Override
    public PaginationResponse<BaseUser> list(UserListRequest req) {
        IPage<BaseUser> itemPage = baseUserDao.pageQuery(req);
        PaginationResponse<BaseUser> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Transactional
    @Override
    public void add(AddUserRequest req) {
        if(baseUserDao.existUsername(req.getUsername())){
            throw new BusinessException("存在该用户名，请更换!");
        }
        BaseUser user = new BaseUser();
        BeanUtils.copyProperties(req, user);
        String userId = UUIDGenerator.getUUID();
        user.setUserId(userId);
        baseUserDao.saveUser(user);
        if(req.getRoleIds() != null && !req.getRoleIds().isEmpty()){
            List<UserRole> userRoles = req.getRoleIds().stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId)
                        .setRoleId(roleId);
                return userRole;
            }).collect(Collectors.toList());
            userRoleDao.saveItems(userRoles);
        }
        if(req.getPermIds() != null && !req.getPermIds().isEmpty()){
            List<UserPerm> userPerms = req.getPermIds().stream().map(permId -> {
                UserPerm userPerm = new UserPerm();
                userPerm.setUserId(userId)
                        .setPermId(permId);
                return userPerm;
            }).collect(Collectors.toList());
            userPermDao.saveItems(userPerms);
        }
    }

    @Transactional
    @Override
    public void update(UpdateUserRequest req) {
        BaseUser user = baseUserDao.getByItemId(req.getId());
        if(user == null){
            throw new BusinessException("账号未找到，修改失败!");
        }
        BeanUtils.copyProperties(req, user);
        baseUserDao.updateUser(user);
        String userId = user.getUserId();
        userRoleDao.removeByUserId(userId);
        userPermDao.removeByUserId(userId);
        if(req.getRoleIds() != null && !req.getRoleIds().isEmpty()){
            List<UserRole> userRoles = req.getRoleIds().stream().map(roleId -> {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId)
                        .setRoleId(roleId);
                return userRole;
            }).collect(Collectors.toList());
            userRoleDao.saveItems(userRoles);
        }
        if(req.getPermIds() != null && !req.getPermIds().isEmpty()){
            List<UserPerm> userPerms = req.getPermIds().stream().map(permId -> {
                UserPerm userPerm = new UserPerm();
                userPerm.setUserId(userId)
                        .setPermId(permId);
                return userPerm;
            }).collect(Collectors.toList());
            userPermDao.saveItems(userPerms);
        }
    }

    @Override
    public void delete(IdsRequest req) {
        baseUserDao.delete(Arrays.asList(req.getIds()));
    }

    @Override
    public void updatePass(UpdateUserPassRequest req) {
        BaseUser user = baseUserDao.getBySessionId(RequestContext.getContext().getSessionId());
        if(user == null){
            throw new BusinessException("账号未找到，修改失败!");
        }
        if(!EncryptUtils.encryptMD5Hex(req.getPassword()).equalsIgnoreCase(user.getPassword())){
            throw new BusinessException("原密码错误，修改失败！");
        }
        if(req.getNewPassword().equalsIgnoreCase(req.getPassword())){
            throw new BusinessException("新密码与原密码相同！");
        }
        baseUserDao.updateUserPass(user.getId(), EncryptUtils.encryptMD5Hex(req.getNewPassword()));
    }

    @Override
    public void resetPass(Long id) {
        BaseUser user = baseUserDao.getByItemId(id);
        if(user == null){
            throw new BusinessException("账号未找到，修改失败!");
        }
        baseUserDao.updateUserPass(id, EncryptUtils.encryptMD5Hex(EncryptUtils.encryptMD5Hex("88888888")));
    }
}
