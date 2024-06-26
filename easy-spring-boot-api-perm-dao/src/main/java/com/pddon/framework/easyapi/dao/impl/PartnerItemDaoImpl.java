package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.PartnerItemDao;
import com.pddon.framework.easyapi.dao.entity.PartnerItem;
import com.pddon.framework.easyapi.dao.mapper.PartnerItemMapper;
import com.pddon.framework.easyapi.dto.req.PartnerListRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: PartnerItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class PartnerItemDaoImpl extends ServiceImpl<PartnerItemMapper, PartnerItem> implements PartnerItemDao {

    @Override
    public boolean saveItem(PartnerItem item) {
        return this.save(item);
    }

    @Override
    public PartnerItem getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(PartnerItem item) {
        return this.updateById(item);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<PartnerItem>().in(PartnerItem::getId, ids));
    }

    @Override
    public IPage<PartnerItem> pageQuery(PartnerListRequest req) {
        Page<PartnerItem> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<PartnerItem> wrapper = new LambdaQueryWrapper<PartnerItem>()
                .eq(!StringUtils.isEmpty(req.getTenantId()), PartnerItem::getTenantId, req.getTenantId())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.likeRight(PartnerItem::getPartnerName, req.getKeyword()).or()
                            .likeRight(PartnerItem::getCompanyName, req.getKeyword()).or()
                            .likeRight(PartnerItem::getDescription, req.getKeyword()).or()
                            .likeRight(PartnerItem::getEmail, req.getKeyword()).or()
                            .likeRight(PartnerItem::getPhone, req.getKeyword()).or()
                            .likeRight(PartnerItem::getUsername, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? PartnerItem::getCrtTime : PartnerItem::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean existsTenantId(String tenantId) {
        return this.count(new LambdaQueryWrapper<PartnerItem>().eq(PartnerItem::getTenantId, tenantId)) > 0;
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return this.update(new LambdaUpdateWrapper<PartnerItem>().eq(PartnerItem::getId, id).set(PartnerItem::getPartnerStatus, status));
    }
}
