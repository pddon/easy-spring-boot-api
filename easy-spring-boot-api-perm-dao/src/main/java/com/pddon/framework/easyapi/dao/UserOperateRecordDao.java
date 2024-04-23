package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;

/**
 * @ClassName: UserLoginRecordDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface UserOperateRecordDao {
    boolean saveLog(UserOperateRecord record);
}
