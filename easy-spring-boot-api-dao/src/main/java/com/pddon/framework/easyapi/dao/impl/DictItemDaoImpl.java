package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.DictItemDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.mapper.DictGroupMapper;
import com.pddon.framework.easyapi.dao.mapper.DictItemMapper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DictItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class DictItemDaoImpl extends ServiceImpl<DictItemMapper, DictItem> implements DictItemDao {
    @Override
    public DictItem getDefaultByDictId(String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .isNull(DictItem::getTenantId)
                .isNull(DictItem::getAppId)
                .isNull(DictItem::getUserId)
                .one();
    }

    @Override
    public DictItem getTenantDict(String tenantId, String appId, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId)
                .eq(StringUtils.isNotEmpty(appId), DictItem::getAppId, appId)
                .isNull(DictItem::getUserId)
                .one();
    }

    @Override
    public DictItem getTenantDefaultDict(String tenantId, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId)
                .isNull(DictItem::getAppId)
                .isNull(DictItem::getUserId)
                .one();
    }

    @Override
    public DictItem getUserDict(String userId, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .eq(DictItem::getUserId, userId)
                .isNull(DictItem::getAppId)
                .one();
    }

    @Override
    public List<DictItem> getDefaultDictsByGroupId(String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId)
                .isNull(DictItem::getUserId)
                .isNull(DictItem::getAppId)
                .isNull(DictItem::getTenantId)
                .list();
    }

    @Override
    public List<DictItem> getTenantDictsByGroupId(String tenantId, String appId, String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId)
                .eq(DictItem::getAppId, appId)
                .eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId)
                .isNull(DictItem::getUserId)
                .list();
    }

    @Override
    public List<DictItem> getDefaultTenantDictsByGroupId(String appId, String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId)
                .eq(DictItem::getAppId, appId)
                .isNull(DictItem::getUserId)
                .list();
    }

    @Override
    public List<DictItem> getTenantDictsByUserId(String userId, String groupId) {
        return this.lambdaQuery().eq(DictItem::getGroupId, groupId)
                .eq(DictItem::getUserId, userId)
                .list();
    }
}
