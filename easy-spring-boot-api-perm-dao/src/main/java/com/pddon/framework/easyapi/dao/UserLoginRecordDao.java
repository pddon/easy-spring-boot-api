package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.UserLoginRecord;

/**
 * @ClassName: UserLoginRecordDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface UserLoginRecordDao {
    void addLoginRecord(UserLoginRecord record);
}
