package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.BaseUserDao;
import com.pddon.framework.easyapi.dao.UserLoginRecordDao;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import com.pddon.framework.easyapi.dao.entity.UserLoginRecord;
import com.pddon.framework.easyapi.dao.mapper.BaseUserMapper;
import com.pddon.framework.easyapi.dao.mapper.UserLoginRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: BaseUserDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-09 21:54
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class UserLoginRecordDaoImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordDao {

}
