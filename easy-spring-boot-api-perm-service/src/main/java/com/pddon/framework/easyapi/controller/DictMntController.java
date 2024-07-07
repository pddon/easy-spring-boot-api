package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.DictMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName: DictMntController
 * @Description: 字典信息管理
 * @Author: Allen
 * @Date: 2024-05-10 01:03
 * @Addr: https://pddon.cn
 */
@Api(tags = "字典相关管理接口")
@RestController
@RequestMapping("dict")
public class DictMntController {

    @Autowired
    private DictMntService dictMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增字典", apiName = "dict/add")
    //@RequiresPermissions("dict:add")
    @RequiredSession
    public IdResponse add(@RequestBody AddDictRequest req){
        return dictMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改字典", apiName = "dict/update")
    //@RequiresPermissions("dict:update")
    @RequiredSession
    public void update(@RequestBody UpdateDictRequest req){
        dictMntService.update(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除字典", apiName = "dict/delete")
    @RequiresPermissions("dict:delete")
    public void remove(@RequestBody IdsRequest req){
        dictMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    //@RequiredSession
    @RequiredSession
    public PaginationResponse<DictItem> listDict(@RequestBody DictListRequest req){
        return dictMntService.list(req);
    }

    @GetMapping("get")
    @RequiredSign(scope = SignScope.REQUEST)
    //@RequiresPermissions("dict:query")
    @RequiredSession
    public DictItem get(@RequestParam("dictId") String dictId){
        return dictMntService.get(dictId);
    }

    @GetMapping("getByGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    //@RequiresPermissions("dict:query")
    @RequiredSession
    public List<DictItem> getByGroup(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestParam("groupId") String groupId){
        return dictMntService.getByGroup(tenantId, groupId);
    }

    @PostMapping("updatesByGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="批量修改分组下字典", apiName = "dict/updatesByGroup")
    //@RequiresPermissions("dict:update")
    @RequiredSession
    public void updatesByGroup(@RequestBody UpdatesByGroupRequest req){
        dictMntService.updatesByGroup(req);
    }

    @PostMapping("addGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增字典分组", apiName = "dict/addGroup")
    //@RequiresPermissions("dict:add")
    @RequiredSession
    public IdResponse addGroup(@RequestBody AddDictGroupRequest req){
        return dictMntService.addGroup(req);
    }

    @PostMapping("updateGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改字典分组", apiName = "dict/updateGroup")
    //@RequiresPermissions("dict:update")
    @RequiredSession
    public void updateGroup(@RequestBody UpdateDictGroupRequest req){
        dictMntService.updateGroup(req);
    }

    @PostMapping("deleteGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除字典分组", apiName = "dict/deleteGroup")
    @RequiresPermissions("dict:delete")
    public void removeGroup(@RequestBody IdsRequest req){
        dictMntService.removeGroup(req);
    }

    @PostMapping("listGroup")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    public PaginationResponse<DictGroup> listGroup(@RequestBody DictGroupListRequest req){
        return dictMntService.listGroup(req);
    }
}
