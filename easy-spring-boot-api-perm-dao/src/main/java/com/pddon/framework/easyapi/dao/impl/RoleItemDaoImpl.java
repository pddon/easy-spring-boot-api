package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.RoleItemDao;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dao.mapper.RoleItemMapper;
import com.pddon.framework.easyapi.dto.req.RoleListRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * @ClassName: RoleItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class RoleItemDaoImpl extends ServiceImpl<RoleItemMapper, RoleItem> implements RoleItemDao {

    @Override
    public boolean exists(String roleId) {
        return this.count(new LambdaQueryWrapper<RoleItem>().eq(RoleItem::getRoleId, roleId)) > 0;
    }

    @Override
    public boolean saveRole(RoleItem role) {
        return this.save(role);
    }

    @Override
    public RoleItem getByItemId(Long id) {
        return this.getByItemId(id);
    }

    @Override
    public boolean updateRole(RoleItem role) {
        return this.updateById(role);
    }

    @Override
    public boolean removeByIds(String[] ids) {
        return this.remove(new LambdaQueryWrapper<RoleItem>().in(RoleItem::getRoleId, Arrays.asList(ids)));
    }

    @Override
    public RoleItem getByRoleId(String roleId) {
        return this.getOne(new LambdaQueryWrapper<RoleItem>().eq(RoleItem::getRoleId, roleId));
    }

    @Override
    public IPage<RoleItem> pageQuery(RoleListRequest req) {
        Page<RoleItem> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<RoleItem> wrapper = new LambdaQueryWrapper<RoleItem>()
                .eq(!StringUtils.isEmpty(req.getRoleId()), RoleItem::getRoleId, req.getRoleId())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.likeLeft(RoleItem::getRoleId, req.getKeyword()).or()
                            .likeLeft(RoleItem::getIntro, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? RoleItem::getCrtTime : RoleItem::getChgTime);
        return this.page(page, wrapper);
    }
}
