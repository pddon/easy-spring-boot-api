package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.UserMntService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
    @RequiresAuthentication
    public UserInfoDto info(){
        return userMntService.getCurrentUserInfo();
    }
}
