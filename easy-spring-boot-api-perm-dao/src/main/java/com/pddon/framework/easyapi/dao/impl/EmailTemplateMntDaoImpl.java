package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pddon.framework.easyapi.dao.EmailTemplateMntDao;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dto.req.EmailTemplateListRequest;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: EmailTemplateMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:37
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class EmailTemplateMntDaoImpl extends EmailTemplateDaoImpl implements EmailTemplateMntDao {
    @Override
    public boolean exists(String sceneId, String resourceId) {
        return this.lambdaQuery().eq(EmailTemplate::getSceneId, sceneId)
                .eq(resourceId != null, EmailTemplate::getResourceId, resourceId)
                .isNull(resourceId == null, EmailTemplate::getResourceId).count() > 0;
    }

    @Override
    public boolean saveItem(EmailTemplate item) {
        return this.save(item);
    }

    @Override
    public EmailTemplate getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public boolean updateByItemId(EmailTemplate item) {
        return this.updateById(item);
    }

    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<EmailTemplate>().in(EmailTemplate::getId, ids));
    }

    @Override
    public IPage<EmailTemplate> pageQuery(EmailTemplateListRequest req) {
        Page<EmailTemplate> page = new Page<>(req.getCurrent(), req.getSize());
        if(StringUtils.isEmpty(req.getOrderBy())){
            //默认按创建时间排序
            req.setOrderBy("crtTime");
        }
        if(req.getIsAsc() == null){
            //默认降序排列
            req.setIsAsc(false);
        }
        Wrapper<EmailTemplate> wrapper = new LambdaQueryWrapper<EmailTemplate>()
                .eq(StringUtils.isNotEmpty(req.getSceneId()), EmailTemplate::getSceneId, req.getSceneId())
                .eq(StringUtils.isNotEmpty(req.getResourceId()), EmailTemplate::getResourceId, req.getResourceId())
                .eq(StringUtils.isNotEmpty(req.getItemAppId()), EmailTemplate::getAppId, req.getItemAppId())
                .eq(StringUtils.isNotEmpty(req.getTenantId()), EmailTemplate::getTenantId, req.getTenantId())
                .and(StringUtils.isNotEmpty(req.getKeyword()), query -> {
                    return query.like(EmailTemplate::getSceneId, req.getKeyword()).or()
                            .like(EmailTemplate::getResourceId, req.getKeyword()).or()
                            .like(EmailTemplate::getTenantId, req.getKeyword()).or()
                            .like(EmailTemplate::getAppId, req.getKeyword()).or()
                            .like(EmailTemplate::getTitle, req.getKeyword()).or()
                            .like(EmailTemplate::getComments, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? EmailTemplate::getCrtTime : EmailTemplate::getChgTime);
        return this.page(page, wrapper);
    }
}
