package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;

/**
 * @ClassName: EmailSendRecordDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:26
 * @Addr: https://pddon.cn
 */
public interface EmailSendRecordDao {
    void saveItem(EmailSendRecord record);
}
