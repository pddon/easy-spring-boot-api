package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.DictItemMntDao;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @ClassName: DictItemMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:59
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class DictItemMntDaoImpl extends DictItemDaoImpl implements DictItemMntDao {
    @Override
    public boolean exists(String tenantId, String appId, String userId, String groupId, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .eq(StringUtils.isNotEmpty(groupId), DictItem::getGroupId, groupId)
                .isNull(StringUtils.isEmpty(groupId), DictItem::getGroupId)
                .eq(StringUtils.isNotEmpty(userId), DictItem::getUserId, userId)
                .isNull(StringUtils.isEmpty(userId), DictItem::getUserId)
                .eq(StringUtils.isNotEmpty(appId), DictItem::getAppId, appId)
                .isNull(StringUtils.isEmpty(appId), DictItem::getAppId)
                .eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId)
                .isNull(StringUtils.isEmpty(tenantId), DictItem::getTenantId)
                .count() > 0;
    }

    @Override
    public boolean exists(Long id) {
        return this.getById(id) != null;
    }

    @Override
    public boolean exists(Long id, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId).ne(DictItem::getId, id).count() > 0;
    }

    @Override
    public boolean saveItem(DictItem item) {
        return this.save(item);
    }

    @Override
    public DictItem getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(DictItem item) {
        return this.updateById(item);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<DictItem>().in(DictItem::getId, ids));
    }

    @Override
    public boolean saveOrUpdateItem(DictItem dictItem) {
        return this.saveOrUpdate(dictItem);
    }

    @Override
    public List<DictItem> getByGroupId(String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId).isNull(DictItem::getTenantId).list();
    }

    @Override
    public List<DictItem> getByTenantGroupId(String tenantId, String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId)
                .eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId)
                .isNull(StringUtils.isEmpty(tenantId), DictItem::getTenantId)
                .list();
    }

    @Override
    public List<DictItem> getByItemIds(Set<Long> ids) {
        return this.lambdaQuery().in(DictItem::getId, ids).list();
    }

    @Override
    public void saveOrUpdateByItemIds(List<DictItem> dictItems) {
        this.saveOrUpdateBatch(dictItems);
    }

    @Override
    protected Class<DictItem> currentModelClass() {
        return DictItem.class;
    }
}
