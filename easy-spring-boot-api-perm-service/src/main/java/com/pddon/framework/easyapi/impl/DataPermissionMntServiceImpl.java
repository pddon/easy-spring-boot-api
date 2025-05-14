package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.pddon.framework.easyapi.DataPermissionMntService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.consts.DataPermissionQueryType;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.ListResponse;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.*;
import com.pddon.framework.easyapi.dao.dto.DataPermDto;
import com.pddon.framework.easyapi.dao.entity.*;
import com.pddon.framework.easyapi.dao.tenant.EntityManager;
import com.pddon.framework.easyapi.dto.DataPermItemDto;
import com.pddon.framework.easyapi.dto.TableInfoDto;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        List<DataPermItemDto> permItems = new ArrayList<>();
        //获取用户的所有数据权限
        List<UserDataPermission> userDataPermissions = userDataPermissionMntDao.getByUserId(currentUserId);
        if(userDataPermissions != null && !userDataPermissions.isEmpty()){
            permItems.addAll(userDataPermissions.stream().map(item -> {
                DataPermItemDto dto = new DataPermItemDto();
                BeanUtils.copyProperties(item, dto);
                return dto;
            }).collect(Collectors.toList()));
        }
        //获取用户拥有的角色
        List<String> roleIds = userRoleDao.getRolesByUserId(currentUserId).stream().map(UserRole::getRoleId).collect(Collectors.toList());
        //获取角色下的所有数据权限
        List<RoleDataPermission> roleDataPermissions = roleDataPermissionMntDao.getByRoleIds(roleIds);
        if(roleDataPermissions != null && !roleDataPermissions.isEmpty()){
            permItems.addAll(roleDataPermissions.stream().map(item -> {
                DataPermItemDto dto = new DataPermItemDto();
                BeanUtils.copyProperties(item, dto);
                return dto;
            }).collect(Collectors.toList()));
        }
        Map<String, List<String>> convertDataPerms = new HashMap<>();
        if(!permItems.isEmpty()){
            List<String> permIds = permItems.stream().map(DataPermItemDto::getPermId).collect(Collectors.toList());
            //查找需要转换的数据权限
            List<DataPermission> dataPermissions = dataPermissionMntDao.getByPermIds(permIds, DataPermissionQueryType.TABLE_FIELD.name());
            if(!dataPermissions.isEmpty()){
                //处理需要转换的数据权限
                Map<String, List<DataPermission>> convertPermMap = dataPermissions.stream().collect(Collectors.groupingBy(DataPermission::getPermId));
                Map<String, List<String>> convertPerms = permItems.stream().filter(item -> convertPermMap.containsKey(item.getPermId()))
                        .collect(Collectors.groupingBy(DataPermItemDto::getPermId,
                                Collectors.mapping(DataPermItemDto::getPermValue, Collectors.toList())));
                //查询并转换
                convertPerms.forEach((permId, values) -> {
                    DataPermission dataPermission = convertPermMap.get(permId).get(0);
                    DataPermission realPermission = dataPermissionMntDao.getByPermId(dataPermission.getRealPermId());
                    String idArrStr = values.stream().map(value -> String.format("'%s'", value)).collect(Collectors.joining(","));
                    List<Map<String, Object>> targetIds = jdbcTemplate.queryForList(
                            String.format("select %s from %s where %s in (%s)",
                                    realPermission.getQueryField(), realPermission.getQueryTable(), dataPermission.getRealField(), idArrStr));
                    List<String> targetValues = targetIds.stream().flatMap(map -> map.values().stream().map(obj -> (String) obj)).collect(Collectors.toList());
                    convertDataPerms.put(permId, targetValues);
                });
                //移除转换的数据权限值集合
                List<DataPermItemDto> realPermItems = permItems.stream()
                        .filter(item -> !convertPermMap.containsKey(item.getPermId()))
                        .collect(Collectors.toList());
                permItems.clear();
                permItems.addAll(realPermItems);
            }

            List<DataPermission> fieldDataPermissions = dataPermissionMntDao.getByPermIds(permIds, DataPermissionQueryType.FIELD.name());
            Map<String, DataPermission> fieldDataPermMap = new HashMap<>();
            if(!fieldDataPermissions.isEmpty()){
                Map<String, List<DataPermission>> map = fieldDataPermissions.stream().collect(Collectors.groupingBy(DataPermission::getPermId));
                map.forEach((key, list) -> {
                    fieldDataPermMap.put(key, list.get(0));
                });
            }
            List<DataPermissionResource> resources = dataPermissionResourceMntDao.getByPermIds(permIds);
            Map<String, List<DataPermissionResource>> map = resources.stream()
                    .collect(Collectors.groupingBy(DataPermissionResource::getPermId));
            Map<String, List<String>> dataPerms = permItems.stream()
                    .collect(Collectors.groupingBy(DataPermItemDto::getPermId, Collectors.mapping(DataPermItemDto::getPermValue, Collectors.toList())));
            dataPerms.forEach((key, values) -> {
                List<DataPermissionResource> tableFields = map.get(key);

                if(fieldDataPermMap.containsKey(key) && fieldDataPermMap.get(key).getDisabled()){
                    //禁用该数据权限，所有字段均允许查询
                    values.clear();
                    values.add("*");
                } else if(convertDataPerms.containsKey(key)){
                    //查找并合并值
                    values.addAll(convertDataPerms.get(key));
                }
                perms.addAll(tableFields.stream().map(item -> {
                    //每张表都需要添加权限值
                    DataPermDto dto = new DataPermDto();
                    dto.setPermId(item.getPermId());
                    dto.setResType(item.getResType());
                    dto.setResName(item.getResName());
                    dto.setResField(item.getResField());
                    if(item.getDisabled()){
                        //禁用该数据权限资源，所有字段均允许查询
                        dto.setValues(Arrays.asList("*"));
                    }else{
                        dto.setValues(values);
                    }
                    return dto;
                }).collect(Collectors.toList()));
            });
        }
        return perms;
    }

    @Override
    public ListResponse<TableInfoDto> listTables() {
        //查找所有的table
        List<TableInfo> list = EntityManager.getAllTableInfos();
        List<TableInfoDto> tables = list.stream().map(item -> {
            TableInfoDto dto = new TableInfoDto();
            dto.setTableName(item.getTableName());
            if(item.getFieldList() == null){
                log.warn("table: {} fileds is empty.", item.getTableName());
                dto.setFields(Collections.emptyList());
                return dto;
            }
            dto.setFields(item.getFieldList().stream().map(field -> {
                return field.getColumn();
            }).collect(Collectors.toList()));
            dto.getFields().add(item.getKeyColumn());
            return dto;
        }).collect(Collectors.toList());
        return new ListResponse<>(tables);
    }

    @Override
    public ListResponse<String> getOwnerDataPerms(String permId, String ownerId, Boolean roleOwner) {
        List<String> ids = Collections.emptyList();
        if(roleOwner){
            List<RoleDataPermission> perms = roleDataPermissionMntDao.getByPermId(ownerId, permId);
            if(perms.isEmpty()){
                return new ListResponse<>(ids);
            }
            ids = perms.stream().map(RoleDataPermission::getPermValue).collect(Collectors.toList());
        }else{
            List<UserDataPermission> perms = userDataPermissionMntDao.getByPermId(ownerId, permId);
            if(perms.isEmpty()){
                return new ListResponse<>(ids);
            }
            ids = perms.stream().map(UserDataPermission::getPermValue).collect(Collectors.toList());
        }
        return new ListResponse<>(ids);
    }
}
