package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dto.req.DepartmentListRequest;

import java.util.List;

/**
 * @ClassName: DepartmentMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:24
 * @Addr: https://pddon.cn
 */
public interface DepartmentMntDao {
    boolean saveItem(Department department);

    Department getByItemId(Long id);

    boolean updateByItemId(Department department);

    boolean removeByIds(List<String> ids);

    IPage<Department> pageQuery(DepartmentListRequest req);
}
