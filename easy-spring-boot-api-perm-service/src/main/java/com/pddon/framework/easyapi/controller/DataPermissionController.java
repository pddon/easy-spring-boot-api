package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.DataPermissionMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.DataPermission;
import com.pddon.framework.easyapi.dao.entity.DataPermissionResource;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: DataPermissionController
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 12:12
 * @Addr: https://pddon.cn
 */
@Api(tags = "数据权限相关管理接口")
@RestController
@RequestMapping("dataPerm")
public class DataPermissionController {

    @Autowired
    private DataPermissionMntService dataPermissionMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增数据权限", apiName = "dataPermission/add")
    @RequiresPermissions("dataPermission:add")
    @RequiredSession
    public IdResponse add(@RequestBody AddDataPermissionRequest req){
        return dataPermissionMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改数据权限", apiName = "dataPermission/update")
    @RequiresPermissions("dataPermission:update")
    @RequiredSession
    public void update(@RequestBody UpdateDataPermissionRequest req){
        dataPermissionMntService.update(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除数据权限", apiName = "dataPermission/delete")
    @RequiresPermissions("dataPermission:delete")
    public void remove(@RequestBody IdsRequest req){
        dataPermissionMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("dataPermission:query")
    public PaginationResponse<DataPermission> listDataPermission(@RequestBody DataPermissionListRequest req){
        return dataPermissionMntService.list(req);
    }

    @PostMapping("addResource")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="添加数据权限资源", apiName = "dataPermission/addResource")
    @RequiresPermissions("dataPermission:add")
    @RequiredSession
    public IdResponse addResource(@RequestBody AddDataPermissionResoruceRequest req){
        return dataPermissionMntService.addResource(req);
    }

    @PostMapping("updateResource")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改数据权限资源", apiName = "dataPermission/updateResource")
    @RequiresPermissions("dataPermission:update")
    @RequiredSession
    public void updateResource(@RequestBody UpdateDataPermissionResourceRequest req){
        dataPermissionMntService.updateResource(req);
    }

    @PostMapping("deleteResource")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除数据权限资源", apiName = "dataPermission/deleteResource")
    @RequiresPermissions("dataPermission:delete")
    public void removeResource(@RequestBody IdsRequest req){
        dataPermissionMntService.removeResource(req);
    }

    @PostMapping("authToRole")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="授权或取消数据权限给角色", apiName = "dataPermission/authToRole")
    @RequiresPermissions("dataPermission:update")
    public void authToRole(@RequestBody AuthToRoleRequest req){
        dataPermissionMntService.authToRole(req);
    }

    @PostMapping("authToUser")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="授权或取消数据权限给用户", apiName = "dataPermission/authToUser")
    @RequiresPermissions("dataPermission:update")
    public void authToUser(@RequestBody AuthToUserRequest req){
        dataPermissionMntService.authToUser(req);
    }

    @PostMapping("listResource")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("dataPermission:query")
    public PaginationResponse<DataPermissionResource> listResource(@RequestBody DataPermissionResourceListRequest req){
        return dataPermissionMntService.listResource(req);
    }
}
