package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DataPermissionResourceMntDao;
import com.pddon.framework.easyapi.dao.entity.DataPermissionResource;
import com.pddon.framework.easyapi.dao.entity.DataPermissionResource;
import com.pddon.framework.easyapi.dao.mapper.DataPermissionResourceMapper;
import com.pddon.framework.easyapi.dto.req.DataPermissionResourceListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DataPermissionResourceMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class DataPermissionResourceMntDaoImpl extends ServiceImpl<DataPermissionResourceMapper, DataPermissionResource> implements DataPermissionResourceMntDao {

    @Autowired
    private DataPermissionResourceMapper dataPermissionResourceMapper;

    @Override
    public boolean saveItem(DataPermissionResource resource) {
        return this.save(resource);
    }

    @Override
    public DataPermissionResource getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(DataPermissionResource resource) {
        return this.updateById(resource);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<DataPermissionResource>().in(DataPermissionResource::getId, ids));
    }

    @Override
    public List<DataPermissionResource> getByPermIds(List<String> permIds) {
        return this.lambdaQuery().in(DataPermissionResource::getPermId, permIds).list();
    }

    @Override
    public boolean exists(String resType, String resName, String resField, String permId) {
        return this.lambdaQuery()
                .eq(DataPermissionResource::getResName, resName)
                .eq(DataPermissionResource::getResField, resField)
                .eq(DataPermissionResource::getPermId, permId)
                .eq(DataPermissionResource::getResType, resType)
                .count() > 0;
    }

    @Override
    public boolean existsExcludeSelf(String resType, String resName, String resField, String permId, Long id) {
        return this.lambdaQuery()
                .eq(DataPermissionResource::getResName, resName)
                .eq(DataPermissionResource::getResField, resField)
                .eq(DataPermissionResource::getPermId, permId)
                .eq(DataPermissionResource::getResType, resType)
                .ne(DataPermissionResource::getId, id)
                .count() > 0;
    }

    @Override
    public IPage<DataPermissionResource> pageQuery(DataPermissionResourceListRequest req) {
        Page<DataPermissionResource> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认升序排列
            req.setIsAsc(true);
        }
        Wrapper<DataPermissionResource> wrapper = new LambdaQueryWrapper<DataPermissionResource>()
                .eq(StringUtils.isNotEmpty(req.getPermId()), DataPermissionResource::getPermId, req.getPermId())
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(DataPermissionResource::getPermId, req.getKeyword()).or()
                            .like(DataPermissionResource::getResName, req.getKeyword()).or()
                            .like(DataPermissionResource::getComment, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? DataPermissionResource::getCrtTime : DataPermissionResource::getChgTime);
        return this.page(page, wrapper);
    }
}
