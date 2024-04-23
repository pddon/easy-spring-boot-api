package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.RoleMntService;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.PermItemDao;
import com.pddon.framework.easyapi.dao.RoleItemDao;
import com.pddon.framework.easyapi.dao.RolePermDao;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dao.entity.RolePerm;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.dto.resp.RoleDetailDto;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName: RoleMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 22:22
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class RoleMntServiceImpl implements RoleMntService {
    @Autowired
    private RoleItemDao roleItemDao;

    @Autowired
    private RolePermDao rolePermDao;

    @Autowired
    private PermItemDao permItemDao;

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
        dto.setPermIds(items);
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
        PermItem item = new PermItem();
        BeanUtils.copyProperties(req, item);
        permItemDao.saveItem(item);
        return new IdResponse(item.getId());
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
}
