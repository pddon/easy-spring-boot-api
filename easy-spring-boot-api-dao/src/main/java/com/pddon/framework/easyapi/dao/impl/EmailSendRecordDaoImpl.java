package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.EmailSendRecordDao;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;
import com.pddon.framework.easyapi.dao.mapper.EmailSendRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: EmailSendRecordDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:26
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class EmailSendRecordDaoImpl extends ServiceImpl<EmailSendRecordMapper, EmailSendRecord> implements EmailSendRecordDao {
    @Override
    public void saveItem(EmailSendRecord record) {
        this.save(record);
    }
}
