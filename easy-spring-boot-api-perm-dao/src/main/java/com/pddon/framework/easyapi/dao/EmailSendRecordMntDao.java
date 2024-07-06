package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.dto.req.EmailRecordListRequest;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;

import java.util.List;

/**
 * @ClassName: EmailSendRecordMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:28
 * @Addr: https://pddon.cn
 */
public interface EmailSendRecordMntDao {
    EmailSendRecord getByItemId(Long id);

    IPage<EmailSendRecord> pageQuery(EmailRecordListRequest req);

    boolean removeByIds(List<String> ids);
}
