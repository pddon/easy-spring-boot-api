package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DataPermissionMntDao;
import com.pddon.framework.easyapi.dao.entity.DataPermission;
import com.pddon.framework.easyapi.dao.mapper.DataPermissionMapper;
import com.pddon.framework.easyapi.dto.req.DataPermissionListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DataPermissionMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class DataPermissionMntDaoImpl extends ServiceImpl<DataPermissionMapper, DataPermission> implements DataPermissionMntDao {

    @Autowired
    private DataPermissionMapper dataPermissionMapper;

    @Override
    public boolean saveItem(DataPermission dataPermission) {
        return this.save(dataPermission);
    }

    @Override
    public DataPermission getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(DataPermission dataPermission) {
        return this.updateById(dataPermission);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<DataPermission>().in(DataPermission::getId, ids));
    }

    @Override
    public IPage<DataPermission> pageQuery(DataPermissionListRequest req) {
        Page<DataPermission> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认升序排列
            req.setIsAsc(true);
        }
        Wrapper<DataPermission> wrapper = new LambdaQueryWrapper<DataPermission>()
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(DataPermission::getPermId, req.getKeyword()).or()
                            .like(DataPermission::getPermName, req.getKeyword()).or()
                            .like(DataPermission::getComment, req.getKeyword());
                })
                .eq(StringUtils.isNotEmpty(req.getQueryType()), DataPermission::getQueryType, req.getQueryType())
                .eq(StringUtils.isNotEmpty(req.getTenantId()), DataPermission::getTenantId, req.getTenantId())
                .eq(req.getDisabled() != null, DataPermission::getDisabled, req.getDisabled())
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? DataPermission::getCrtTime : DataPermission::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean exists(String permId) {
        return this.lambdaQuery().eq(DataPermission::getPermId, permId).count() > 0;
    }

    @Override
    public List<DataPermission> getByPermIds(List<String> permIds, String queryType) {
        return this.lambdaQuery().in(DataPermission::getPermId, permIds)
                .eq(queryType != null, DataPermission::getQueryType, queryType)
                .list();
    }

    @Override
    public DataPermission getByPermId(String permId) {
        return this.lambdaQuery().eq(DataPermission::getPermId, permId)
                .one();
    }
}
