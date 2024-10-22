package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DictGroupDao;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.mapper.DictGroupMapper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DictGroupDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class DictGroupDaoImpl extends ServiceImpl<DictGroupMapper, DictGroup> implements DictGroupDao {
    @Override
    public List<DictGroup> getByParentDictGroupId(String parentId) {
        return this.lambdaQuery().eq(DictGroup::getParentGroupId, parentId).list();
    }

    @Override
    public boolean existsGroupId(String groupId) {
        return this.lambdaQuery().eq(DictGroup::getGroupId, groupId).count() > 0;
    }

    @Override
    public DictGroup getByGroupId(String groupId) {
        return this.lambdaQuery().eq(DictGroup::getGroupId, groupId).one();
    }

    @Override
    public IPage<DictGroup> pageQuery(DictGroupListRequest req) {
        Page<DictGroup> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<DictGroup> wrapper = new LambdaQueryWrapper<DictGroup>()
                .eq(StringUtils.isNotEmpty(req.getGroupId()), DictGroup::getGroupId, req.getGroupId())
                .eq(StringUtils.isNotEmpty(req.getParentGroupId()), DictGroup::getParentGroupId, req.getParentGroupId())
                .and(StringUtils.isNotEmpty(req.getTenantId()), query -> {
                    return query.eq(DictGroup::getTenantId, req.getTenantId()).or().isNull(DictGroup::getTenantId);
                })
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(DictGroup::getGroupId, req.getKeyword()).or()
                            .like(DictGroup::getParentGroupId, req.getKeyword()).or()
                            .like(DictGroup::getDescription, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? DictGroup::getCrtTime : DictGroup::getChgTime);
        return this.page(page, wrapper);
    }
}
