package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.DataPermissionMntService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.*;
import com.pddon.framework.easyapi.dao.dto.DataPermDto;
import com.pddon.framework.easyapi.dao.entity.*;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: DataPermissionMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 12:13
 * @Addr: https://pddon.cn
 */
@Service
@Primary
@Slf4j
public class DataPermissionMntServiceImpl implements DataPermissionMntService {

    @Autowired
    private DataPermissionMntDao dataPermissionMntDao;

    @Autowired
    private DataPermissionResourceMntDao dataPermissionResourceMntDao;

    @Autowired
    private RoleDataPermissionMntDao roleDataPermissionMntDao;

    @Autowired
    private UserDataPermissionMntDao userDataPermissionMntDao;

    @Autowired
    private RoleItemDao roleItemDao;

    @Autowired
    private BaseUserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Override
    public IdResponse add(AddDataPermissionRequest req) {
        DataPermission dataPermission = new DataPermission();
        BeanUtils.copyProperties(req, dataPermission);
        dataPermissionMntDao.saveItem(dataPermission);
        return new IdResponse(dataPermission.getId());
    }

    @Override
    public void update(UpdateDataPermissionRequest req) {
        DataPermission dataPermission = dataPermissionMntDao.getByItemId(req.getId());
        if(dataPermission == null){
            throw new BusinessException("记录未找到!");
        }
        BeanUtils.copyProperties(req, dataPermission);
        dataPermissionMntDao.updateByItemId(dataPermission);
    }

    @Override
    public void remove(IdsRequest req) {
        dataPermissionMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<DataPermission> list(DataPermissionListRequest req) {
        IPage<DataPermission> itemPage = dataPermissionMntDao.pageQuery(req);
        PaginationResponse<DataPermission> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public IdResponse addResource(AddDataPermissionResoruceRequest req) {
        if(!dataPermissionMntDao.exists(req.getPermId())){
            throw new BusinessException("参数非法，数据权限未找到!");
        }
        DataPermissionResource resource = new DataPermissionResource();
        BeanUtils.copyProperties(req, resource);
        dataPermissionResourceMntDao.saveItem(resource);
        return new IdResponse(resource.getId());
    }

    @Override
    public void updateResource(UpdateDataPermissionResourceRequest req) {
        DataPermissionResource resource = dataPermissionResourceMntDao.getByItemId(req.getId());
        if(resource == null){
            throw new BusinessException("记录未找到!");
        }
        if(StringUtils.isNotEmpty(req.getPermId()) &&  !dataPermissionMntDao.exists(req.getPermId())){
            throw new BusinessException("参数非法，数据权限未找到!");
        }
        BeanUtils.copyProperties(req, resource);
        dataPermissionResourceMntDao.updateByItemId(resource);
    }

    @Override
    public void removeResource(IdsRequest req) {
        dataPermissionResourceMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<DataPermissionResource> listResource(DataPermissionResourceListRequest req) {
        IPage<DataPermissionResource> itemPage = dataPermissionResourceMntDao.pageQuery(req);
        PaginationResponse<DataPermissionResource> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Transactional
    @Override
    public void authToRole(AuthToRoleRequest req) {
        if(!roleItemDao.exists(req.getRoleIds())){
            throw new BusinessException("角色ID异常，未找到角色信息!");
        }
        if(req.isUnAuth()){
            //删除
            req.getRoleIds().forEach(roleId -> {
                req.getPerms().forEach(permDto -> {
                    roleDataPermissionMntDao.deletePerms(roleId, permDto.getPermId(), permDto.getValues());
                });
            });
            return;
        }
        //添加权限
        List<RoleDataPermission> perms = new ArrayList<>();
        req.getPerms().forEach(permDto -> {
            String permId = permDto.getPermId();
            List<RoleDataPermission> oldPerms = roleDataPermissionMntDao.getByRoleIds(req.getRoleIds());
            req.getRoleIds().forEach(roleId -> {
                permDto.getValues().forEach(permValue -> {
                    if(oldPerms.stream().filter(perm -> perm.getRoleId().equals(roleId)
                                    && perm.getPermId().equals(permId)
                                    && perm.getPermValue().equals(permValue.toString()))
                            .count() > 0){
                        return;
                    }
                    RoleDataPermission perm = new RoleDataPermission();
                    perm.setRoleId(roleId);
                    perm.setPermId(permId);
                    perm.setPermValue(permValue.toString());
                    perms.add(perm);
                });
            });
        });
        roleDataPermissionMntDao.saveItems(perms);
    }

    @Override
    public void authToUser(AuthToUserRequest req) {
        if(!userDao.exists(req.getUserIds())){
            throw new BusinessException("角色ID异常，未找到角色信息!");
        }
        if(req.isUnAuth()){
            //删除
            req.getUserIds().forEach(userId -> {
                req.getPerms().forEach(permDto -> {
                    userDataPermissionMntDao.deletePerms(userId, permDto.getPermId(), permDto.getValues());
                });
            });
            return;
        }
        //添加权限
        List<UserDataPermission> perms = new ArrayList<>();
        req.getPerms().forEach(permDto -> {
            String permId = permDto.getPermId();
            List<UserDataPermission> oldPerms = userDataPermissionMntDao.getByUserIds(req.getUserIds());
            req.getUserIds().forEach(userId -> {
                permDto.getValues().forEach(permValue -> {
                    if(oldPerms.stream().filter(perm -> perm.getUserId().equals(userId)
                                    && perm.getPermId().equals(permId)
                                    && perm.getPermValue().equals(permValue.toString()))
                            .count() > 0){
                        return;
                    }
                    UserDataPermission perm = new UserDataPermission();
                    perm.setUserId(userId);
                    perm.setPermId(permId);
                    perm.setPermValue(permValue.toString());
                    perms.add(perm);
                });
            });
        });
        userDataPermissionMntDao.saveItems(perms);
    }

    @CacheMethodResult(prefix = "User:DataPerms", id = "currentUserId", keyMode = CacheKeyMode.CUSTOM_ID, needCacheField = "cacheable", expireSeconds = 3600)
    @Override
    public List<DataPermDto> getDataPermsByUserId(String currentUserId, boolean cacheable) {
        if(StringUtils.isEmpty(currentUserId)){
            return new ArrayList<>();
        }
        List<DataPermDto> perms = new ArrayList<>();
        //获取用户的所有数据权限
        List<UserDataPermission> userDataPermissions = userDataPermissionMntDao.getByUserId(currentUserId);
        if(userDataPermissions != null && !userDataPermissions.isEmpty()){
            List<DataPermissionResource> resources = dataPermissionResourceMntDao.getByPermIds(userDataPermissions.stream().map(UserDataPermission::getPermId).collect(Collectors.toList()));
            Map<String, List<DataPermissionResource>> map = resources.stream()
                    .collect(Collectors.groupingBy(DataPermissionResource::getPermId));
            Map<String, List<String>> dataPerms = userDataPermissions.stream()
                    .collect(Collectors.groupingBy(UserDataPermission::getPermId, Collectors.mapping(UserDataPermission::getPermValue, Collectors.toList())));
            dataPerms.forEach((key, values) -> {
                List<DataPermissionResource> tableFields = map.get(key);
                perms.addAll(tableFields.stream().map(item -> {
                    //每张表都需要添加权限值
                    DataPermDto dto = new DataPermDto();
                    dto.setPermId(item.getPermId());
                    dto.setResType(item.getResType());
                    dto.setResName(item.getResName());
                    dto.setResField(item.getResField());
                    dto.setValues(values);
                    return dto;
                }).collect(Collectors.toList()));
            });
        }
        //获取用户拥有的角色
        List<String> roleIds = userRoleDao.getRolesByUserId(currentUserId).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        //获取角色下的所有数据权限
        List<RoleDataPermission> roleDataPermissions = roleDataPermissionMntDao.getByRoleIds(roleIds);
        if(roleDataPermissions != null && !roleDataPermissions.isEmpty()){
            List<DataPermissionResource> resources = dataPermissionResourceMntDao.getByPermIds(roleDataPermissions.stream().map(RoleDataPermission::getPermId).collect(Collectors.toList()));
            Map<String, List<DataPermissionResource>> map = resources.stream()
                    .collect(Collectors.groupingBy(DataPermissionResource::getPermId));
            Map<String, List<String>> dataPerms = roleDataPermissions.stream()
                    .collect(Collectors.groupingBy(RoleDataPermission::getPermId, Collectors.mapping(RoleDataPermission::getPermValue, Collectors.toList())));
            dataPerms.forEach((key, values) -> {
                List<DataPermissionResource> tableFields = map.get(key);
                perms.addAll(tableFields.stream().map(item -> {
                    //每张表都需要添加权限值
                    DataPermDto dto = new DataPermDto();
                    dto.setPermId(item.getPermId());
                    dto.setResType(item.getResType());
                    dto.setResName(item.getResName());
                    dto.setResField(item.getResField());
                    dto.setValues(values);
                    return dto;
                }).collect(Collectors.toList()));
            });
        }
        return perms;
    }
}
