package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.DictGroupMntDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.DictGroupListRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DictGroupMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:59
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class DictGroupMntDaoImpl extends DictGroupDaoImpl implements DictGroupMntDao {
    @Override
    public boolean exists(String groupId) {
        return this.lambdaQuery().eq(DictGroup::getGroupId, groupId).count() > 0;
    }

    @Override
    public boolean exists(Long id) {
        return this.getById(id) != null;
    }

    @Override
    public boolean exists(Long id, String groupId) {
        return this.lambdaQuery().eq(DictGroup::getGroupId, groupId).ne(DictGroup::getId, id).count() > 0;
    }

    @Override
    public boolean saveItem(DictGroup item) {
        return this.save(item);
    }

    @Override
    public DictGroup getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(DictGroup item) {
        return this.updateById(item);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<DictGroup>().in(DictGroup::getId, ids));
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
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(DictGroup::getGroupId, req.getKeyword()).or()
                            .like(DictGroup::getParentGroupId, req.getKeyword()).or()
                            .like(DictGroup::getDescription, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? DictGroup::getCrtTime : DictGroup::getChgTime);
        return this.page(page, wrapper);
    }
}
