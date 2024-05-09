package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.DictGroupMntDao;
import com.pddon.framework.easyapi.dao.DictItemMntDao;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dto.req.DictListRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

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
                .and(query -> {
                    return query.eq(StringUtils.isNotEmpty(groupId), DictItem::getGroupId, groupId).or()
                            .isNull(DictItem::getGroupId);
                })
                .and(query -> {
                    return query.eq(StringUtils.isNotEmpty(userId), DictItem::getUserId, userId).or()
                            .isNull(DictItem::getUserId);
                })
                .and(query -> {
                    return query.eq(StringUtils.isNotEmpty(appId), DictItem::getAppId, appId).or()
                            .isNull(DictItem::getAppId);
                })
                .and(query -> {
                    return query.eq(StringUtils.isNotEmpty(tenantId), DictItem::getTenantId, tenantId).or()
                            .isNull(DictItem::getTenantId);
                })
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
                .eq(StringUtils.isNotEmpty(req.getAppId()), DictItem::getAppId, req.getAppId())
                .eq(StringUtils.isNotEmpty(req.getUserId()), DictItem::getUserId, req.getUserId())
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
