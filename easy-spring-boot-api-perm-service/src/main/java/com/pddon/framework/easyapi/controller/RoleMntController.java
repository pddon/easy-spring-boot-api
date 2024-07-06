package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.RoleMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.dto.resp.PermTreeDataDto;
import com.pddon.framework.easyapi.dto.resp.RoleDetailDto;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName: RoleMntController
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 21:26
 * @Addr: https://pddon.cn
 */
@Api(tags = "角色权限管理")
@RestController
@RequestMapping("role")
public class RoleMntController {

    @Autowired
    private RoleMntService roleMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增角色", apiName = "role/add")
    @RequiresPermissions("role:add")
    public IdResponse addRole(@RequestBody AddRoleRequest req){
        return roleMntService.addRole(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="更新角色", apiName = "role/update")
    @RequiresPermissions("role:update")
    public void updateRole(@RequestBody UpdateRoleRequest req){
        roleMntService.updateRole(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除角色", apiName = "role/delete")
    @RequiresPermissions("role:delete")
    public void removeRoles(@RequestBody IdsRequest req){
        roleMntService.removeRoleByIds(req.getIds());
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    public PaginationResponse<RoleItem> listRole(@RequestBody RoleListRequest req){
        return roleMntService.listRole(req);
    }

    @GetMapping("getUserPerms")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    public List<PermTreeDataDto> getUserPerms(@RequestParam(value = "userId", required = false) String userId){
        return roleMntService.getUserPerms(userId);
    }

    @GetMapping("get")
    @RequiredSign(scope = SignScope.REQUEST)
    //@OperateLog(type="查询角色详情", apiName = "role/get")
    @RequiresPermissions("role:query")
    public RoleDetailDto roleDetail(@RequestParam("id")String id){
        return roleMntService.roleDetail(id);
    }

    @PostMapping("listPerm")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("perm:query")
    public PaginationResponse<PermItem> listPerm(@RequestBody PermListRequest req){
        return roleMntService.listPerm(req);
    }

    @PostMapping("addPerm")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增权限", apiName = "role/addPerm")
    @RequiresPermissions("perm:add")
    public IdResponse addPerm(@RequestBody AddPermRequest req){
        return roleMntService.addPerm(req);
    }

    @PostMapping("updatePerm")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="更新权限", apiName = "role/updatePerm")
    @RequiresPermissions("perm:update")
    public void updatePerm(@RequestBody UpdatePermRequest req){
        roleMntService.updatePerm(req);
    }

    @PostMapping("deletePerm")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除权限", apiName = "role/deletePerm")
    @RequiresPermissions("perm:delete")
    public void removePermByIds(@RequestBody IdsRequest req){
        roleMntService.removePermByIds(req.getIds());
    }

}
