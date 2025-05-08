package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.DepartmentMntService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.ListResponse;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.DepartmentMntDao;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.AddDepartmentMemberRequest;
import com.pddon.framework.easyapi.dto.req.AddDepartmentRequest;
import com.pddon.framework.easyapi.dto.req.DepartmentListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateDepartmentRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: DepartmentMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:23
 * @Addr: https://pddon.cn
 */
@Service
@Primary
@Slf4j
public class DepartmentMntServiceImpl implements DepartmentMntService {

    @Autowired
    private DepartmentMntDao departmentMntDao;

    @Autowired
    private BaseUserDao baseUserDao;


    //@Transactional
    @Override
    public IdResponse add(AddDepartmentRequest req) {
        Department department = new Department();
        BeanUtils.copyProperties(req, department);
        department.setOrderValue(System.currentTimeMillis());
        department.setTenantId(RequestContext.getContext().getSession().getChannelId());
        departmentMntDao.saveItem(department);
        /*if(StringUtils.isNotEmpty(department.getLeaderId())){
            baseUserDao.updateDepartmentId(department.getId(), Arrays.asList(department.getLeaderId()));
        }*/
        return new IdResponse(department.getId());
    }

    //@Transactional
    @Override
    public void update(UpdateDepartmentRequest req) {
        Department department = departmentMntDao.getByItemId(req.getId());
        if(department == null){
            throw new BusinessException("记录未找到!");
        }
        BeanUtils.copyProperties(req, department);
        departmentMntDao.updateByItemId(department);
        /*if(StringUtils.isNotEmpty(department.getLeaderId())){
            baseUserDao.updateDepartmentId(department.getId(), Arrays.asList(department.getLeaderId()));
        }*/
    }

    @Override
    public void remove(IdsRequest req) {
        departmentMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<Department> list(DepartmentListRequest req) {
        IPage<Department> itemPage = departmentMntDao.pageQuery(req);
        PaginationResponse<Department> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public void top(Integer id) {
        departmentMntDao.top(id);
    }

    @Override
    public void addMember(AddDepartmentMemberRequest req) {
        baseUserDao.updateDepartmentId(req.getDepartmentId(), req.getUserIds());
    }

    @Override
    public ListResponse<Department> listItems(String tenantId, Long parentId) {
        List<Department> items = departmentMntDao.listItems(tenantId, parentId);
        return new ListResponse<>(items);
    }
}
