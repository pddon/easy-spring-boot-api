package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.DictService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.DictItemDao;
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
    public DictItem getTenantDict(String tenantId, String appId, String dictId) {
        return dictItemDao.getTenantDict(tenantId, appId, dictId);
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
    public List<DictItem> getTenantDictsByGroupId(String tenantId, String appId, String groupId) {
        return dictItemDao.getTenantDictsByGroupId(tenantId, appId, groupId);
    }

    @Override
    public List<DictItem> getDefaultTenantDictsByGroupId(String appId, String groupId) {
        return dictItemDao.getDefaultTenantDictsByGroupId(appId, groupId);
    }

    @Override
    public List<DictItem> getTenantDictsByUserId(String userId, String groupId) {
        return dictItemDao.getTenantDictsByUserId(userId, groupId);
    }

    @Override
    public List<DictGroup> getByParentDictGroupId(String parentId) {
        return dictGroupDao.getByParentDictGroupId(parentId);
    }
}
