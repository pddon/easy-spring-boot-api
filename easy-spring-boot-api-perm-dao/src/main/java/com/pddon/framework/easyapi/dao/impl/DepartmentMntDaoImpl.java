package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.DepartmentMntDao;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dao.mapper.DepartmentMapper;
import com.pddon.framework.easyapi.dto.req.DepartmentListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: DepartmentMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:24
 * @Addr: https://pddon.cn
 */
@Repository
@Primary
@Slf4j
public class DepartmentMntDaoImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentMntDao {

    @Autowired
    private DepartmentMapper departmentMapper;


    @Override
    public boolean saveItem(Department department) {
        return this.save(department);
    }

    @Override
    public Department getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(Department department) {
        return this.updateById(department);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<Department>().in(Department::getId, ids));
    }

    @Override
    public IPage<Department> pageQuery(DepartmentListRequest req) {
        Page<Department> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认升序排列
            req.setIsAsc(true);
        }
        Wrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(StringUtils.isNotEmpty(req.getParentId()), Department::getParentId, req.getParentId())
                .eq(StringUtils.isNotEmpty(req.getLeaderId()), Department::getLeaderId, req.getLeaderId())
                .eq(StringUtils.isNotEmpty(req.getTypeId()), Department::getTypeId, req.getTypeId())
                .eq(StringUtils.isNotEmpty(req.getDepStatus()), Department::getDepStatus, req.getDepStatus())
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(Department::getDepName, req.getKeyword()).or()
                            .like(Department::getLeaderName, req.getKeyword()).or()
                            .like(Department::getTypeName, req.getKeyword()).or()
                            .like(Department::getComment, req.getKeyword());
                })
                .eq(StringUtils.isNotEmpty(req.getTenantId()), Department::getTenantId, req.getTenantId())
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? Department::getCrtTime : Department::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Department> listItems(String tenantId, Long parentId) {
        return this.lambdaQuery().isNull(parentId == null, Department::getParentId)
                .eq(parentId != null, Department::getParentId, parentId)
                .eq(StringUtils.isNotEmpty(tenantId), Department::getTenantId, tenantId)
                .orderByDesc(Department::getOrderValue).orderByAsc(Department::getCrtTime)
                .list();
    }

    @Override
    public boolean top(Integer id) {
        return this.lambdaUpdate().set(Department::getOrderValue, System.currentTimeMillis()).eq(Department::getId, id).update();
    }
}
