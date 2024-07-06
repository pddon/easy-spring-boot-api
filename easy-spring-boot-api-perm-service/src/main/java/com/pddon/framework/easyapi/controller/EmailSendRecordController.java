package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.EmailSendRecordMntService;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.req.EmailRecordListRequest;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dto.req.EmailTemplateListRequest;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName: EmailSendRecordController
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 11:05
 * @Addr: https://pddon.cn
 */
@Api(tags = "邮件发送记录相关管理接口")
@RestController
@RequestMapping("emailRecord")
public class EmailSendRecordController {
    @Autowired
    private EmailSendRecordMntService emailSendRecordMntService;

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("emailRecord:query")
    public PaginationResponse<EmailSendRecord> list(@RequestBody EmailRecordListRequest req){
        return emailSendRecordMntService.list(req);
    }

    @PostMapping("userEmails")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiredSession
    @IgnoreTenant
    public PaginationResponse<EmailSendRecord> userEmails(@RequestBody EmailRecordListRequest req){
        req.setToUserId(RequestContext.getContext().getSession().getUserId());
        return emailSendRecordMntService.list(req);
    }

    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除邮件发送记录", apiName = "emailRecord/delete")
    @RequiresPermissions("emailRecord:delete")
    public void remove(@RequestBody IdsRequest req){
        emailSendRecordMntService.remove(req);
    }

    @PostMapping("resend")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="重新发送邮件", apiName = "emailRecord/resend")
    @RequiresPermissions("emailRecord:update")
    public void resend(@RequestParam("id") Long id){
        emailSendRecordMntService.resend(id);
    }
}
