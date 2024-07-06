package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.req.EmailRecordListRequest;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;

/**
 * @ClassName: EmailSendRecordMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 11:06
 * @Addr: https://pddon.cn
 */
public interface EmailSendRecordMntService {
    PaginationResponse<EmailSendRecord> list(EmailRecordListRequest req);

    void resend(Long id);

    void remove(IdsRequest req);
}
