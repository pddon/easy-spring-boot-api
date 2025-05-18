package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DictItemDao;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictItem;
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
                .and(StringUtils.isNotEmpty(tenantId), query -> {
                    return query.eq(DictItem::getTenantId, tenantId).or().isNull(DictItem::getTenantId);
                })
                .isNull(StringUtils.isEmpty(tenantId), DictItem::getTenantId)
                .eq(StringUtils.isNotEmpty(appId), DictItem::getAppId, appId)
                .isNull(StringUtils.isEmpty(appId), DictItem::getAppId)
                .isNull(DictItem::getUserId)
                .one();
    }

    @Override
    public DictItem getTenantDefaultDict(String tenantId, String dictId) {
        return this.lambdaQuery().eq(DictItem::getDictId, dictId)
                .and(StringUtils.isNotEmpty(tenantId), query -> {
                    return query.eq(DictItem::getTenantId, tenantId).or().isNull(DictItem::getTenantId);
                })
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
                .eq(StringUtils.isNotEmpty(appId), DictItem::getAppId, appId)
                .and(StringUtils.isNotEmpty(tenantId), query -> {
                    return query.eq(DictItem::getTenantId, tenantId).or().isNull(DictItem::getTenantId);
                })
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

    @Override
    public IPage<DictItem> pageQuery(DictListRequest req) {
        Page<DictItem> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<DictItem> wrapper = new LambdaQueryWrapper<DictItem>()
                .eq(StringUtils.isNotEmpty(req.getDictId()), DictItem::getDictId, req.getDictId())
                .eq(StringUtils.isNotEmpty(req.getGroupId()), DictItem::getGroupId, req.getGroupId())
                .eq(StringUtils.isNotEmpty(req.getDictAppId()), DictItem::getAppId, req.getDictAppId())
                .and(StringUtils.isNotEmpty(req.getTenantId()), query -> {
                    return query.eq(DictItem::getTenantId, req.getTenantId()).or().isNull(DictItem::getTenantId);
                })
                .eq(StringUtils.isNotEmpty(req.getDictUserId()), DictItem::getUserId, req.getDictUserId())
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(DictItem::getDictId, req.getKeyword()).or()
                            .like(DictItem::getTenantId, req.getKeyword()).or()
                            .like(DictItem::getUserId, req.getKeyword()).or()
                            .like(DictItem::getAppId, req.getKeyword()).or()
                            .like(DictItem::getGroupId, req.getKeyword()).or()
                            .like(DictItem::getDescription, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? DictItem::getCrtTime : DictItem::getChgTime);
        return this.page(page, wrapper);
    }
}
