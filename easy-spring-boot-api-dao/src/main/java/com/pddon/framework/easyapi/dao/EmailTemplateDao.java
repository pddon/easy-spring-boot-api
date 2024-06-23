package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.EmailTemplate;

/**
 * @ClassName: EmailTemplateDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 12:22
 * @Addr: https://pddon.cn
 */
public interface EmailTemplateDao {
    EmailTemplate getByScene(String sceneId, String resourceId);
}
