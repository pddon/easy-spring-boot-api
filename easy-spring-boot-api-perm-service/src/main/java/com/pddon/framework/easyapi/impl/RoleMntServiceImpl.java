package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.RoleMntService;
import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.annotation.LockDistributed;
import com.pddon.framework.easyapi.config.SecurityConfigProperties;
import com.pddon.framework.easyapi.consts.ApiPermOperateType;
import com.pddon.framework.easyapi.consts.PermissionResourceType;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.PermItemDao;
import com.pddon.framework.easyapi.dao.RoleItemDao;
import com.pddon.framework.easyapi.dao.RolePermDao;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dao.entity.RolePerm;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.dto.resp.PermTreeDataDto;
import com.pddon.framework.easyapi.dto.resp.RoleDetailDto;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName: RoleMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:22
 * @Addr: https://pddon.cn
 */
@Service
@IgnoreTenant
@Slf4j
public class RoleMntServiceImpl implements RoleMntService {
    @Autowired
    private RoleItemDao roleItemDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private PermItemDao permItemDao;

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private SecurityConfigProperties securityConfigProperties;

    @Transactional
    @Override
    public IdResponse addRole(AddRoleRequest req) {
        if(roleItemDao.exists(req.getRoleId())){
            throw new BusinessException("角色ID已存在，请重新命名!");
        }
        RoleItem role = new RoleItem();
        BeanUtils.copyProperties(req, role);
        boolean re = roleItemDao.saveRole(role);
        List<RolePerm> rolePermList = req.getPermIds().stream().map(permId -> {
            RolePerm rolePerm = new RolePerm();
            rolePerm.setRoleId(role.getRoleId())
                    .setPermId(permId);
            return rolePerm;
        }).collect(Collectors.toList());
        rolePermDao.saveBatchItems(rolePermList);
        return new IdResponse(role.getId());
    }

    @Transactional
    @Override
    public boolean updateRole(UpdateRoleRequest req) {
        RoleItem role = roleItemDao.getByItemId(req.getId());
        if(role == null){
            throw new BusinessException("角色ID错误，未找到该角色，修改失败!");
        }
        String lastRoleId = role.getRoleId();
        BeanUtils.copyProperties(req, role);
        boolean re = roleItemDao.updateRole(role);
        //删除历史角色权限列表
        rolePermDao.removeByRoleId(lastRoleId);
        //插入新权限列表
        List<RolePerm> rolePermList = req.getPermIds().stream().map(permId -> {
            RolePerm rolePerm = new RolePerm();
            rolePerm.setRoleId(role.getRoleId())
                    .setPermId(permId);
            return rolePerm;
        }).collect(Collectors.toList());
        rolePermDao.saveBatchItems(rolePermList);
        return re;
    }

    @Transactional
    @Override
    public boolean removeRoleByIds(String[] ids) {
        roleItemDao.removeByIds(ids);
        rolePermDao.removeByRoleIds(ids);
        return false;
    }

    @Override
    public PaginationResponse<RoleItem> listRole(RoleListRequest req) {
        IPage<RoleItem> itemPage = roleItemDao.pageQuery(req);
        PaginationResponse<RoleItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public RoleDetailDto roleDetail(String roleId) {
        RoleItem role = roleItemDao.getByRoleId(roleId);
        if(role == null){
            throw new BusinessException("角色ID错误，未找到!");
        }
        RoleDetailDto dto = new RoleDetailDto();
        BeanUtils.copyProperties(role, dto);
        List<RolePerm> rolePermList = rolePermDao.getByRoleIds(Arrays.asList(roleId));
        List<String> permIdList = rolePermList.stream().map(RolePerm::getPermId).collect(Collectors.toList());
        List<PermItem> items = permItemDao.getByPermIds(permIdList);
        dto.setPerms(items);
        return dto;
    }

    @Override
    public PaginationResponse<PermItem> listPerm(PermListRequest req) {
        IPage<PermItem> itemPage = permItemDao.pageQuery(req);
        PaginationResponse<PermItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public IdResponse addPerm(AddPermRequest req) {
        String permId = String.format("%s:*", req.getPermId());
        if(permItemDao.existsPermId(permId)){
            throw new BusinessException("权限{0}已存在，请检查").setParam(req.getPermId());
        }
        List<PermItem> perms = new ArrayList<>();
        PermItem permItem = new PermItem();
        permItem.setPermId(permId)
                .setPermName(req.getPermName())
                .setResourceType(req.getResourceType())
                .setIntro(req.getIntro());
        perms.add(permItem);
        Arrays.stream(ApiPermOperateType.values()).forEach(type -> {
            PermItem item = new PermItem();
            item.setPermId(String.format("%s:%s", req.getPermId(), type.getKey()))
                    .setPermName(String.format("%s%s", type.getName(), req.getPermName()))
                    .setResourceType(req.getResourceType())
                    .setParentPermId(permId);
            perms.add(item);
        });
        permItemDao.saveItems(perms);
        return new IdResponse(permItem.getId());
    }

    @Override
    public boolean updatePerm(UpdatePermRequest req) {
        PermItem permItem = permItemDao.geByItemId(req.getId());
        if(permItem == null){
            throw new BusinessException("权限未找到!");
        }
        BeanUtils.copyProperties(req, permItem);
        return permItemDao.updateByItemId(permItem);
    }

    @Override
    public boolean removePermByIds(String[] ids) {
        return permItemDao.removeByPermIds(ids);
    }

    @Override
    public List<PermTreeDataDto> getUserPerms(String userId) {
        if(StringUtils.isEmpty(userId)){
            userId = RequestContext.getContext().getSession().getUserId();
        }else{
            BaseUser user = userSecurityService.queryByUserId(userId);
            if(user == null){
                throw new BusinessException("账号未找到!");
            }
            if(!RequestContext.getContext().getSession().getUserId().equalsIgnoreCase(user.getUserId())){
                //修改他人账号需要用户修改权限
                // 获取当前Subject
                Subject subject = SecurityUtils.getSubject();
                subject.checkPermission("role:query");
            }
        }
        List<String> permIds = new ArrayList<>(userSecurityService.getUserPermissions(userId, false));
        List<PermItem> perms = permItemDao.getAllPerms();
        Map<String, Set<String>> map = new HashMap<>();
        perms.forEach(perm -> {
            String rootPermId = perm.getParentPermId();
            if(StringUtils.isEmpty(perm.getParentPermId())){
                rootPermId = perm.getPermId() ;
            }
            Set<String> subs = map.get(rootPermId);
            if(subs == null){
                subs = new HashSet<>();
            }
            if(StringUtils.isNotEmpty(perm.getParentPermId())){
                subs.add(perm.getPermId()) ;
            }
            map.put(rootPermId, subs);
        });
        List<PermTreeDataDto> dtos = perms.stream().filter(perm -> map.containsKey(perm.getPermId())).map(perm -> {
            PermTreeDataDto rootDto = new PermTreeDataDto();
            rootDto.setValue(perm.getPermId())
                    .setLabel(perm.getPermName());
            if(!permIds.contains(perm.getPermId())){
                rootDto.setDisabled(true);
            }
            Set<String> subs = map.get(perm.getPermId());
            if(subs != null && !subs.isEmpty()){
                rootDto.setChildren(perms.stream().filter(subPerm -> subs.contains(subPerm.getPermId())).map(subPerm -> {
                    PermTreeDataDto dto = new PermTreeDataDto();
                    dto.setLabel(subPerm.getPermName())
                            .setValue(subPerm.getPermId());
                    if(rootDto.isDisabled() && !permIds.contains(subPerm.getPermId())){
                        dto.setDisabled(true);
                    }else{
                        dto.setDisabled(false);
                    }
                    return dto;
                }).collect(Collectors.toList()));
            }else{
                rootDto.setChildren(new ArrayList<>());
            }
            return rootDto;
        }).collect(Collectors.toList());
        return dtos.stream().filter(dto -> {
            if(dto.isDisabled()){
                return dto.getChildren().stream().filter(subDto -> !subDto.isDisabled()).count() > 0;
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    @LockDistributed
    public void checkAndInitPerms(){
        Map<String, String> types = securityConfigProperties.getApiPermTypes();
        types.put("user", "用户信息");
        types.put("role", "角色信息");
        types.put("perm", "权限信息");
        types.put("partner", "商户信息");
        types.put("application", "应用信息");
        types.put("log", "系统日志");
        types.put("dict", "字典信息");
        types.put("page", "html页面");
        types.put("emailTemplate", "邮件模板");
        types.put("emailRecord", "邮件发送记录");
        types.put("file", "文件信息");
        types.put("department", "组织与部门");
        types.put("dataPerm", "数据权限");
        List<String[]> rootPermIds = types.keySet().stream().map(key -> new String[]{String.format("%s:*", key), key}).collect(Collectors.toList());
        List<PermItem> perms = new ArrayList<>();
        rootPermIds.forEach(items -> {
            if(permItemDao.existsPermId(items[0])){
                return;
            }
            //初始化权限和子权限
            PermItem permItem = new PermItem();
            permItem.setPermId(items[0])
                    .setPermName(types.get(items[1]))
                    .setResourceType(PermissionResourceType.API.name());
            perms.add(permItem);
            Arrays.stream(ApiPermOperateType.values()).forEach(type -> {
                PermItem item = new PermItem();
                item.setPermId(String.format("%s:%s", items[1], type.getKey()))
                        .setPermName(String.format("%s%s", type.getName(), types.get(items[1])))
                        .setResourceType(PermissionResourceType.API.name())
                        .setParentPermId(items[0]);
                perms.add(item);
            });
        });
        if(!perms.isEmpty()){
            permItemDao.saveItems(perms);
        }
    }
}
