package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dto.req.AddDepartmentMemberRequest;
import com.pddon.framework.easyapi.dto.req.AddDepartmentRequest;
import com.pddon.framework.easyapi.dto.req.DepartmentListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateDepartmentRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: DepartmentMntService
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-27 23:10
 * @Addr: https://pddon.cn
 */
public interface DepartmentMntService {
    IdResponse add(AddDepartmentRequest req);

    void update(UpdateDepartmentRequest req);

    void remove(IdsRequest req);

    PaginationResponse<Department> list(DepartmentListRequest req);

    void addMember(AddDepartmentMemberRequest req);
}
