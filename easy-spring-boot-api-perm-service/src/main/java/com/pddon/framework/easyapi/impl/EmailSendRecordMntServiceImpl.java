package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.EmailSendRecordMntService;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.EmailSendRecordMntDao;
import com.pddon.framework.easyapi.dao.dto.req.EmailRecordListRequest;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @ClassName: EmailSendRecordMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 11:06
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class EmailSendRecordMntServiceImpl implements EmailSendRecordMntService {

    @Autowired
    private EmailSendRecordMntDao emailSendRecordMntDao;


    @Override
    public PaginationResponse<EmailSendRecord> list(EmailRecordListRequest req) {
        IPage<EmailSendRecord> itemPage = emailSendRecordMntDao.pageQuery(req);
        PaginationResponse<EmailSendRecord> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public void remove(IdsRequest req) {
        emailSendRecordMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public void resend(Long id) {
        EmailSendRecord record = emailSendRecordMntDao.getByItemId(id);
        if(record == null){
            throw new BusinessException("邮件发送记录未找到!");
        }
        EmailUtil.sendHtmlEmail(record.getUserId(), record.getEmail(), record.getTitle(), record.getContent());
    }
}
