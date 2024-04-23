package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.UserOperateRecordDao;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import com.pddon.framework.easyapi.dao.mapper.UserOperateRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @ClassName: UserOperateRecordDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class UserOperateRecordDaoImpl extends ServiceImpl<UserOperateRecordMapper, UserOperateRecord> implements UserOperateRecordDao {

    @Override
    public boolean saveLog(UserOperateRecord record) {
        return this.save(record);
    }
}