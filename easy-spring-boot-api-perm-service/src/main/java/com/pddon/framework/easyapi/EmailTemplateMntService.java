package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dto.req.AddEmailTemplateRequest;
import com.pddon.framework.easyapi.dto.req.EmailTemplateListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateEmailTemplateRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: EmailTemplateMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:43
 * @Addr: https://pddon.cn
 */
public interface EmailTemplateMntService {
    IdResponse add(AddEmailTemplateRequest req);

    void update(UpdateEmailTemplateRequest req);

    void remove(IdsRequest req);

    PaginationResponse<EmailTemplate> list(EmailTemplateListRequest req);
}
