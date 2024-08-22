package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.DictGroupMntDao;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
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

}
