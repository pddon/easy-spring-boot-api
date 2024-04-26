package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.UserOperateRecordDao;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dao.entity.UserOperateRecord;
import com.pddon.framework.easyapi.dao.mapper.UserOperateRecordMapper;
import com.pddon.framework.easyapi.dto.req.OperateLogListRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    @Override
    public IPage<UserOperateRecord> pageQuery(OperateLogListRequest req) {
        Page<UserOperateRecord> page = new Page<>(req.getCurrent(), req.getSize());
        Wrapper<UserOperateRecord> wrapper = new LambdaQueryWrapper<UserOperateRecord>()
                .gt(req.getStartTime() != null, UserOperateRecord::getOperateTime, req.getStartTime())
                .lt(req.getEndTime() != null, UserOperateRecord::getEndTime, req.getEndTime())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.eq(UserOperateRecord::getUserId, req.getKeyword()).or()
                            .eq(UserOperateRecord::getUsername, req.getKeyword()).or()
                            .eq(UserOperateRecord::getOperateType, req.getKeyword()).or()
                            .eq(UserOperateRecord::getApiName, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? UserOperateRecord::getOperateTime : UserOperateRecord::getEndTime);
        return this.page(page, wrapper);
    }
}
