package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.UserMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserPassRequest;
import com.pddon.framework.easyapi.dao.dto.request.UserListRequest;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.dto.request.AddUserRequest;
import com.pddon.framework.easyapi.dao.dto.request.UpdateUserRequest;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: UserMntController
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 17:24
 * @Addr: https://pddon.cn
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("user")
public class UserMntController {

    @Autowired
    private UserMntService userMntService;

    @GetMapping("info")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    public UserInfoDto info(@RequestParam(value = "id", required = false) String id){
        return userMntService.getUserInfo(id);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("user:query")
    @RequiredSession
    public PaginationResponse<BaseUser> list(@RequestBody UserListRequest req){
        return userMntService.list(req);
    }

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @RequiresPermissions("user:add")
    @OperateLog(type="新增用户账号", apiName = "user/add")
    public void add(@RequestBody AddUserRequest req){
        userMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @OperateLog(type="修改用户信息", apiName = "user/update")
    public void update(@RequestBody UpdateUserRequest req){
        userMntService.update(req);
    }

    @PostMapping("updatePass")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @OperateLog(type="修改密码", apiName = "user/updatePass")
    public void updatePass(@RequestBody UpdateUserPassRequest req){
        userMntService.updatePass(req);
    }

    @PostMapping("resetPass")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @RequiresPermissions("user:update")
    @OperateLog(type="重置密码", apiName = "user/resetPass")
    public void resetPass(@RequestParam(value = "id") Long id){
        userMntService.resetPass(id);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @RequiresPermissions("user:delete")
    @OperateLog(type="删除用户账号", apiName = "user/delete")
    public void delete(@RequestBody IdsRequest req){
        userMntService.delete(req);
    }

}
