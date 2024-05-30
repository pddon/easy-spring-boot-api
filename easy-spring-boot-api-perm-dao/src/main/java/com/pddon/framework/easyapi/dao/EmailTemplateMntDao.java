package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dto.req.EmailTemplateListRequest;

import java.util.List;

/**
 * @ClassName: EmailTemplateMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 23:36
 * @Addr: https://pddon.cn
 */
public interface EmailTemplateMntDao {
    boolean exists(String sceneId, String resourceId);

    boolean saveItem(EmailTemplate item);

    EmailTemplate getByItemId(Long id);

    boolean updateByItemId(EmailTemplate item);

    boolean removeByIds(List<String> asList);

    IPage<EmailTemplate> pageQuery(EmailTemplateListRequest req);
}
