package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.EmailTemplateDao;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.mapper.EmailTemplateMapper;
import com.pddon.framework.easyapi.dao.mapper.HtmlPageMapper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: EmailTemplateDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:34
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class EmailTemplateDaoImpl extends ServiceImpl<EmailTemplateMapper, EmailTemplate> implements EmailTemplateDao {

    @Override
    public EmailTemplate getByScene(String sceneId, String resourceId) {
        List<EmailTemplate> list = this.lambdaQuery().eq(EmailTemplate::getSceneId, sceneId)
                .eq(StringUtils.isNotEmpty(resourceId), EmailTemplate::getResourceId, resourceId)
                .isNull(StringUtils.isEmpty(resourceId), EmailTemplate::getResourceId)
                .orderByDesc(EmailTemplate::getChgTime).list();
        if(list.isEmpty()){
            return null;
        }
        return list.get(0);
    }
}
