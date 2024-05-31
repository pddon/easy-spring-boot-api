package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.EmailService;
import com.pddon.framework.easyapi.EmailTemplateMntService;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.EmailTemplateMntDao;
import com.pddon.framework.easyapi.dao.consts.PageContentType;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dto.req.AddEmailTemplateRequest;
import com.pddon.framework.easyapi.dto.req.EmailTemplateListRequest;
import com.pddon.framework.easyapi.dto.req.SendEmailRequest;
import com.pddon.framework.easyapi.dto.req.UpdateEmailTemplateRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @ClassName: EmailTemplateMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:43
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class EmailTemplateMntServiceImpl implements EmailTemplateMntService {

    @Autowired
    private EmailTemplateMntDao emailTemplateMntDao;

    @Autowired
    private EmailService emailService;

    @Override
    public IdResponse add(AddEmailTemplateRequest req) {
        if(emailTemplateMntDao.exists(req.getSceneId(), req.getResourceId())){
            throw new BusinessException("该场景下的邮件模板已经配置，无需再次新增!");
        }
        EmailTemplate item = new EmailTemplate();
        BeanUtils.copyProperties(req, item);
        item.setAppId(req.getItemAppId());
        emailTemplateMntDao.saveItem(item);
        return new IdResponse(item.getId());
    }

    @Override
    public void update(UpdateEmailTemplateRequest req) {
        EmailTemplate item = emailTemplateMntDao.getByItemId(req.getId());
        if(item == null){
            throw new BusinessException("未找到邮件模板ID!");
        }
        BeanUtils.copyProperties(req, item);
        item.setAppId(req.getItemAppId());
        emailTemplateMntDao.updateByItemId(item);
    }

    @Override
    public void remove(IdsRequest req) {
        emailTemplateMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<EmailTemplate> list(EmailTemplateListRequest req) {
        IPage<EmailTemplate> itemPage = emailTemplateMntDao.pageQuery(req);
        PaginationResponse<EmailTemplate> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public void sendEmail(SendEmailRequest req) {
        EmailUtil.sendEmail(req.getEmail(), req.getTitle(), req.getContent(), PageContentType.HTML.equals(req.getType()));
    }
}
