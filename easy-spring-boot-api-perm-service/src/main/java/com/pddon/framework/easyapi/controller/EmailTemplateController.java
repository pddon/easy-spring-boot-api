package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.EmailTemplateMntService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
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
 * @ClassName: EmailTemplateController
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:44
 * @Addr: https://pddon.cn
 */
@Api(tags = "邮件模板相关管理接口")
@RestController
@RequestMapping("emailTemplate")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateMntService emailTemplateMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增邮件模板", apiName = "emailTemplate/add")
    @RequiresPermissions("emailTemplate:add")
    public IdResponse add(@RequestBody AddEmailTemplateRequest req){
        return emailTemplateMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改邮件模板", apiName = "emailTemplate/update")
    @RequiresPermissions("emailTemplate:update")
    public void update(@RequestBody UpdateEmailTemplateRequest req){
        emailTemplateMntService.update(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除邮件模板", apiName = "emailTemplate/delete")
    @RequiresPermissions("emailTemplate:delete")
    public void remove(@RequestBody IdsRequest req){
        emailTemplateMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("emailTemplate:query")
    public PaginationResponse<EmailTemplate> listDict(@RequestBody EmailTemplateListRequest req){
        return emailTemplateMntService.list(req);
    }

    @PostMapping("sendEmail")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="发送邮件", apiName = "emailTemplate/sendEmail")
    @RequiresPermissions("emailTemplate:query")
    public void sendEmail(@RequestBody SendEmailRequest req){
        emailTemplateMntService.sendEmail(req);
    }
}
