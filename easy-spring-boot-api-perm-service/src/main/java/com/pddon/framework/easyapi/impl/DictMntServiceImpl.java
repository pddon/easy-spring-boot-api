package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.DictMntService;
import com.pddon.framework.easyapi.cache.MethodCacheManager;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.DictGroupMntDao;
import com.pddon.framework.easyapi.dao.DictItemMntDao;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.req.dto.DictDto;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.msg.queue.Message;
import com.pddon.framework.easyapi.msg.queue.MessageManager;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
@IgnoreTenant
public class DictMntServiceImpl extends DictServiceImpl implements DictMntService {

    @Autowired
    private DictGroupMntDao dictGroupMntDao;

    @Autowired
    private DictItemMntDao dictItemMntDao;

    @Autowired
    @Lazy
    private MethodCacheManager methodCacheManager;

    @Autowired
    @Lazy
    private MessageManager messageManager;

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

    @Override
    public DictItem get(String dictId) {
        DictItem dictItem = null;
        if(RequestContext.getContext().getSession() != null){
            //先查租户配置
            dictItem = dictItemMntDao.getTenantDefaultDict(RequestContext.getContext().getSession().getChannelId(), dictId);
        }
        if(dictItem == null){
            dictItem = dictItemMntDao.getDefaultByDictId(dictId);
        }
        if(dictItem == null){
            throw new BusinessException("没有找到字典!");
        }
        return dictItem;
    }

    @Override
    public List<DictItem> getByGroup(String tenantId, String groupId) {
        return dictItemMntDao.getByTenantGroupId(tenantId, groupId);
    }

    @Override
    public void updatesByGroup(UpdatesByGroupRequest req) {
        Map<Long, DictDto> dtos = req.getItems().stream().filter(item -> item.getId() != null).collect(Collectors.toMap(DictDto::getId, item -> item, (item1, item2) -> item1));
        List<DictItem> dictItems = null;
        Map<Long, DictItem> items = new HashMap<>();
        if(!dtos.isEmpty()){
            dictItems = dictItemMntDao.getByItemIds(dtos.keySet());
            items.putAll(dictItems.stream().collect(Collectors.toMap(DictItem::getId, item -> item)));
        }
        List<DictItem> updatedItems = req.getItems().stream().map(dto -> {
            DictItem item = items.get(dto.getId());
            if (item == null) {
                item = new DictItem();
            }
            BeanUtils.copyProperties(dto, item);
            if (req.getTenantId() != null) {
                item.setTenantId(req.getTenantId());
            }
            if (req.getDictAppId() != null) {
                item.setAppId(req.getDictAppId());
            }
            if (req.getUserId() != null) {
                item.setUserId(req.getUserId());
            }
            if (req.getGroupId() != null) {
                item.setGroupId(req.getGroupId());
            }
            return item;
        }).collect(Collectors.toList());
        dictItemMntDao.saveOrUpdateByItemIds(updatedItems);
        if("emailServerConfigs".equalsIgnoreCase(req.getGroupId())){
            messageManager.sendDefaultMessage(Message.builder().type("emailConfigUpdated").build());
        }
    }
}
