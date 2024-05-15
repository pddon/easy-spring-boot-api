package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.DictMntService;
import com.pddon.framework.easyapi.cache.MethodCacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.DictGroupMntDao;
import com.pddon.framework.easyapi.dao.DictItemMntDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @ClassName: DictMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 01:08
 * @Addr: https://pddon.cn
 */
@Service
@Primary
@Slf4j
public class DictMntServiceImpl extends DictServiceImpl implements DictMntService {

    @Autowired
    private DictGroupMntDao dictGroupMntDao;

    @Autowired
    private DictItemMntDao dictItemMntDao;

    @Autowired
    @Lazy
    private MethodCacheManager methodCacheManager;

    @Override
    public IdResponse add(AddDictRequest req) {
        if(dictItemMntDao.exists(req.getTenantId(), req.getDictAppId(), req.getDictUserId(), req.getGroupId(), req.getDictId())){
            throw new BusinessException("字典已存在！");
        }
        DictItem item = new DictItem();
        BeanUtils.copyProperties(req, item);
        item.setAppId(req.getDictAppId());
        item.setUserId(req.getDictUserId());
        dictItemMntDao.saveItem(item);
        return new IdResponse(item.getId());
    }

    @Override
    public void update(UpdateDictRequest req) {
        if(!dictItemMntDao.exists(req.getId())){
            throw new BusinessException("id:[{0}]字典不存在！").setParam(req.getId().toString());
        }
        if(StringUtils.isNotEmpty(req.getDictId()) && dictItemMntDao.exists(req.getId(), req.getDictId())){
            throw new BusinessException("该字典ID已存在，请更换命名!");
        }
        DictItem item = dictItemMntDao.getByItemId(req.getId());
        BeanUtils.copyProperties(req, item);
        item.setAppId(req.getDictAppId());
        item.setUserId(req.getDictUserId());
        dictItemMntDao.updateByItemId(item);
        String key = String.format("Dict:%s-%s-%s", item.getTenantId(), item.getAppId(), item.getDictId());
        if(StringUtils.isEmpty(item.getTenantId()) && StringUtils.isEmpty(item.getTenantId())){
            key = String.format("Dict:%s", item.getDictId());
        }else if(StringUtils.isEmpty(item.getTenantId())){
            key = String.format("Dict:%s-%s", item.getAppId(), item.getDictId());
        }else if(StringUtils.isEmpty(item.getAppId())){
            key = String.format("Dict:%s-%s", item.getTenantId(), item.getDictId());
        }
        methodCacheManager.remove(key, null, 120L, CacheExpireMode.EXPIRE_AFTER_REDA);
    }

    @Override
    public void remove(IdsRequest req) {
        dictItemMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<DictItem> list(DictListRequest req) {
        IPage<DictItem> itemPage = dictItemMntDao.pageQuery(req);
        PaginationResponse<DictItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public IdResponse addGroup(AddDictGroupRequest req) {
        if(dictGroupMntDao.exists(req.getGroupId())){
            throw new BusinessException("字典分组已存在！");
        }
        DictGroup item = new DictGroup();
        BeanUtils.copyProperties(req, item);
        dictGroupMntDao.saveItem(item);
        return new IdResponse(item.getId());
    }

    @Override
    public void updateGroup(UpdateDictGroupRequest req) {
        if(!dictGroupMntDao.exists(req.getId())){
            throw new BusinessException("id:[{0}]字典分组不存在！").setParam(req.getId().toString());
        }
        if(StringUtils.isNotEmpty(req.getGroupId()) && dictGroupMntDao.exists(req.getId(), req.getGroupId())){
            throw new BusinessException("该字典ID已存在，请更换命名!");
        }
        DictGroup item = dictGroupMntDao.getByItemId(req.getId());
        BeanUtils.copyProperties(req, item);
        dictGroupMntDao.updateByItemId(item);
    }

    @Override
    public void removeGroup(IdsRequest req) {
        dictGroupMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<DictGroup> listGroup(DictGroupListRequest req) {
        IPage<DictGroup> itemPage = dictGroupMntDao.pageQuery(req);
        PaginationResponse<DictGroup> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public boolean saveOrUpdate(String tenantId, String appId, String dictId, String content){
        DictItem dictItem = dictItemMntDao.getTenantDict(tenantId, appId, dictId);
        if(dictItem == null){
            dictItem = new DictItem();
            dictItem.setDictId(dictId)
                    .setAppId(appId)
                    .setTenantId(tenantId);
        }
        dictItem.setContent(content);
        boolean re = dictItemMntDao.saveOrUpdateItem(dictItem);
        if(re){
            String key = String.format("Dict:%s-%s-%s", tenantId, appId, dictId);
            if(StringUtils.isEmpty(tenantId) && StringUtils.isEmpty(appId)){
                key = String.format("Dict:%s", dictId);
            }else if(StringUtils.isEmpty(tenantId)){
                key = String.format("Dict:%s-%s", appId, dictId);
            }else if(StringUtils.isEmpty(appId)){
                key = String.format("Dict:%s-%s", tenantId, dictId);
            }
            methodCacheManager.remove(key, null, 120L, CacheExpireMode.EXPIRE_AFTER_REDA);
        }
        return re;
    }
}
