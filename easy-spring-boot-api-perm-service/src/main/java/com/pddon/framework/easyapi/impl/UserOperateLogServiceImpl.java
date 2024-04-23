package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.UserOperateLogService;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.UserOperateRecordDao;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName: UserOperateLogServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-23 15:16
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class UserOperateLogServiceImpl implements UserOperateLogService {

    @Autowired
    private UserOperateRecordDao userOperateRecordDao;

    @Override
    public boolean addLog(String operateType, String apiName, Date startTime, Boolean completed, String errorMsg) {
        UserOperateRecord record = new UserOperateRecord();
        record.setUserId(RequestContext.getContext().getUserId());
        record.setSessionId(RequestContext.getContext().getSessionId());
        if(RequestContext.getContext().getSession() != null){
            record.setUsername(RequestContext.getContext().getSession().getUsername());
        }
        record.setOperateType(operateType);
        record.setApiName(apiName);
        record.setOperateTime(startTime);
        record.setCompleted(completed);
        record.setErrorMsg(errorMsg);
        record.setEndTime(new Date());

        return userOperateRecordDao.saveLog(record);
    }
}
