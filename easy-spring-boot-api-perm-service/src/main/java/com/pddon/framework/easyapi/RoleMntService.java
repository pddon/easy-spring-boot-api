package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.dto.resp.RoleDetailDto;

/**
 * @ClassName: RoleMntService
 * @Description: 角色权限管理
 * @Author: Allen
 * @Date: 2024-04-23 21:28
 * @Addr: https://pddon.cn
 */
public interface RoleMntService {
    IdResponse addRole(AddRoleRequest req);

    boolean updateRole(UpdateRoleRequest req);

    boolean removeRoleByIds(String[] ids);

    PaginationResponse<RoleItem> listRole(RoleListRequest req);

    RoleDetailDto roleDetail(String roleId);

    PaginationResponse<PermItem> listPerm(PermListRequest req);

    IdResponse addPerm(AddPermRequest req);

    boolean updatePerm(UpdatePermRequest req);

    boolean removePermByIds(String[] ids);
}
