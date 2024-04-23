package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.PermItemDao;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dao.mapper.PermItemMapper;
import com.pddon.framework.easyapi.dto.req.PermListRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: PermItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class PermItemDaoImpl extends ServiceImpl<PermItemMapper, PermItem> implements PermItemDao {

    @Override
    public List<PermItem> getAllPerms() {
        return this.list();
    }

    @Override
    public List<PermItem> getByPermIds(List<String> permIdList) {
        return this.list(new LambdaQueryWrapper<PermItem>().in(PermItem::getPermId, permIdList));
    }

    @Override
    public boolean saveItem(PermItem item) {
        return this.save(item);
    }

    @Override
    public PermItem geByItemId(String id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(PermItem permItem) {
        return this.updateById(permItem);
    }

    @Override
    public boolean removeByPermIds(String[] ids) {
        return this.remove(new LambdaQueryWrapper<PermItem>().in(PermItem::getPermId, Arrays.asList(ids)));
    }

    @Override
    public IPage<PermItem> pageQuery(PermListRequest req) {
        Page<PermItem> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<PermItem> wrapper = new LambdaQueryWrapper<PermItem>()
                .eq(!StringUtils.isEmpty(req.getPermId()), PermItem::getPermId, req.getPermId())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.likeLeft(PermItem::getPermId, req.getKeyword()).or()
                            .likeLeft(PermItem::getIntro, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? PermItem::getCrtTime : PermItem::getChgTime);
        return this.page(page, wrapper);
    }
}
