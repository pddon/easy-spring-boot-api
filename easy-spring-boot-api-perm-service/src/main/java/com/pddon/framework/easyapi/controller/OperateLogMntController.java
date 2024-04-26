package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.UserMntService;
import com.pddon.framework.easyapi.UserOperateLogService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import com.pddon.framework.easyapi.dto.req.OperateLogListRequest;
import com.pddon.framework.easyapi.dto.resp.UserInfoDto;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: OperateLogMntController
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-25 17:24
 * @Addr: https://pddon.cn
 */
@Api(tags = "操作日志")
@RestController
@RequestMapping("log")
public class OperateLogMntController {

    @Autowired
    private UserOperateLogService userOperateLogService;

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresAuthentication
    public PaginationResponse<UserOperateRecord> list(@RequestBody OperateLogListRequest req){
        return userOperateLogService.list(req);
    }
}
