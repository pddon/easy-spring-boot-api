package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.EmailSendRecordService;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.EmailSendRecordDao;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName: EmailSendRecordServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:41
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class EmailSendRecordServiceImpl implements EmailSendRecordService {

    @Autowired
    private EmailSendRecordDao emailSendRecordDao;
    @Autowired
    private BaseUserDao userDao;

    @Override
    public void saveSuccessRecord(String userId, String sceneId, String resourceId, String email, String title, String content) {
        saveRecord(userId, sceneId, resourceId, email, title, content, true, null);
    }

    @Override
    public void saveSuccessRecord(String userId, String email, String title, String content) {
        saveRecord(userId, null, null, email, title, content, true, null);
    }

    @Override
    public void saveFailedRecord(String userId, String email, String title, String content, String errMsg) {
        saveRecord(userId, null, null, email, title, content, false, errMsg);
    }

    @Override
    public void saveFailedRecord(String userId, String sceneId, String resourceId, String email, String title, String content, String errMsg) {
        saveRecord(userId, sceneId, resourceId, email, title, content, false, errMsg);
    }

    @Override
    public void saveRecord(String userId, String sceneId, String resourceId, String email, String title, String content, boolean success, String errMsg) {
        EmailSendRecord record = new EmailSendRecord();
        record.setEmail(email)
                .setSuccess(success)
                .setContent(content)
                .setErrMsg(errMsg)
                .setResourceId(resourceId)
                .setSceneId(sceneId)
                .setUserId(userId)
                .setTitle(title);
        if(userId == null){
            //通过邮箱号查找用户ID
            BaseUser user = userDao.getByEmail(email);
            if(user != null){
                record.setUserId(user.getUserId());
            }
        }
        emailSendRecordDao.saveItem(record);
    }
}
