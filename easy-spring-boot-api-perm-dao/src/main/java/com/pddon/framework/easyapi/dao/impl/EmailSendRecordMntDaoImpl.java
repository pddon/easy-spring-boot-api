package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.EmailSendRecordMntDao;
import com.pddon.framework.easyapi.dao.dto.req.EmailRecordListRequest;
import com.pddon.framework.easyapi.dao.entity.EmailSendRecord;
import com.pddon.framework.easyapi.dao.mapper.EmailSendRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: EmailSendRecordMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-07-01 10:28
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class EmailSendRecordMntDaoImpl extends ServiceImpl<EmailSendRecordMapper, EmailSendRecord> implements EmailSendRecordMntDao {
    @Override
    public EmailSendRecord getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<EmailSendRecord>().in(EmailSendRecord::getId, ids));
    }

    @Override
    public IPage<EmailSendRecord> pageQuery(EmailRecordListRequest req) {
        Page<EmailSendRecord> page = new Page<>(req.getCurrent(), req.getSize());
        Wrapper<EmailSendRecord> wrapper = new LambdaQueryWrapper<EmailSendRecord>()
                .eq(req.getToUserId() != null, EmailSendRecord::getUserId, req.getToUserId())
                .eq(req.getTenantId() != null, EmailSendRecord::getTenantId, req.getTenantId())
                .eq(req.getEmail() != null, EmailSendRecord::getEmail, req.getEmail())
                .eq(req.getSceneId() != null, EmailSendRecord::getSceneId, req.getSceneId())
                .eq(req.getResourceId() != null, EmailSendRecord::getResourceId, req.getResourceId())
                .eq(req.getSuccess() != null, EmailSendRecord::getSuccess, req.getSuccess())
                .gt(req.getStartTime() != null, EmailSendRecord::getCrtTime, req.getStartTime())
                .lt(req.getEndTime() != null, EmailSendRecord::getCrtTime, req.getEndTime())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.likeRight(EmailSendRecord::getTitle, req.getKeyword()).or()
                            .likeRight(EmailSendRecord::getEmail, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? EmailSendRecord::getCrtTime : EmailSendRecord::getChgTime);
        return this.page(page, wrapper);
    }
}
