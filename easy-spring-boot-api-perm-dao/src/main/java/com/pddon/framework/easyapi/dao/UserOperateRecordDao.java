package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import com.pddon.framework.easyapi.dto.req.OperateLogListRequest;

/**
 * @ClassName: UserLoginRecordDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface UserOperateRecordDao {
    boolean saveLog(UserOperateRecord record);

    IPage<UserOperateRecord> pageQuery(OperateLogListRequest req);
}
