package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.DepartmentMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.Department;
import com.pddon.framework.easyapi.dto.req.AddDepartmentMemberRequest;
import com.pddon.framework.easyapi.dto.req.AddDepartmentRequest;
import com.pddon.framework.easyapi.dto.req.DepartmentListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateDepartmentRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: DepartmentController
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 00:26
 * @Addr: https://pddon.cn
 */
@Api(tags = "部门相关管理接口")
@RestController
@RequestMapping("department")
public class DepartmentController {
    
    @Autowired
    private DepartmentMntService departmentMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增部门", apiName = "department/add")
    @RequiresPermissions("department:add")
    @RequiredSession
    public IdResponse add(@RequestBody AddDepartmentRequest req){
        return departmentMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改部门", apiName = "department/update")
    @RequiresPermissions("department:update")
    @RequiredSession
    public void update(@RequestBody UpdateDepartmentRequest req){
        departmentMntService.update(req);
    }

    @PostMapping("addMember")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="添加部门成员", apiName = "department/addMember")
    @RequiresPermissions("department:update")
    @RequiredSession
    public void addMember(@RequestBody AddDepartmentMemberRequest req){
        departmentMntService.addMember(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除部门", apiName = "department/delete")
    @RequiresPermissions("department:delete")
    public void remove(@RequestBody IdsRequest req){
        departmentMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    //@RequiredSession
    @RequiredSession
    public PaginationResponse<Department> listDepartment(@RequestBody DepartmentListRequest req){
        return departmentMntService.list(req);
    }

}
