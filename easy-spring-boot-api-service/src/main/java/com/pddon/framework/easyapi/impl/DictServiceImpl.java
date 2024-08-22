package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.DictService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.DictItemDao;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: DictServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 20:33
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class DictServiceImpl implements DictService {

    @Autowired
    private DictItemDao dictItemDao;

    @Autowired
    private DictGroupDao dictGroupDao;

    @CacheMethodResult(prefix = "Dict", id = "dictId")
    @Override
    public DictItem getDefaultByDictId(String dictId) {
        return dictItemDao.getDefaultByDictId(dictId);
    }

    @CacheMethodResult(prefix = "Dict")
    @Override
    public DictItem getTenantDict(String tenantId, String dictAppId, String dictId) {
        return dictItemDao.getTenantDict(tenantId, dictAppId, dictId);
    }

    @CacheMethodResult(prefix = "Dict")
    @Override
    public DictItem getTenantDefaultDict(String tenantId, String dictId) {
        return dictItemDao.getTenantDefaultDict(tenantId, dictId);
    }

    @CacheMethodResult(prefix = "Dict")
    @Override
    public DictItem getUserDict(String userId, String dictId) {
        return dictItemDao.getUserDict(userId, dictId);
    }

    @Override
    public List<DictItem> getDefaultDictsByGroupId(String groupId) {
        return dictItemDao.getDefaultDictsByGroupId(groupId);
    }

    @Override
    public List<DictItem> getTenantDictsByGroupId(String tenantId, String dictAppId, String groupId) {
        return dictItemDao.getTenantDictsByGroupId(tenantId, dictAppId, groupId);
    }

    @Override
    public List<DictItem> getDefaultTenantDictsByGroupId(String dictAppId, String groupId) {
        return dictItemDao.getDefaultTenantDictsByGroupId(dictAppId, groupId);
    }

    @Override
    public List<DictItem> getTenantDictsByUserId(String userId, String groupId) {
        return dictItemDao.getTenantDictsByUserId(userId, groupId);
    }

    @Override
    public List<DictGroup> getByParentDictGroupId(String parentId) {
        return dictGroupDao.getByParentDictGroupId(parentId);
    }

    @Override
    public boolean existsGroupId(String groupId) {
        return dictGroupDao.existsGroupId(groupId);
    }

    @Override
    public DictGroup getByGroupId(String groupId) {
        return dictGroupDao.getByGroupId(groupId);
    }

    @Override
    public PaginationResponse<DictItem> list(DictListRequest req) {
        IPage<DictItem> itemPage = dictItemDao.pageQuery(req);
        PaginationResponse<DictItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public PaginationResponse<DictGroup> listGroup(DictGroupListRequest req) {
        IPage<DictGroup> itemPage = dictGroupDao.pageQuery(req);
        PaginationResponse<DictGroup> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }
}
