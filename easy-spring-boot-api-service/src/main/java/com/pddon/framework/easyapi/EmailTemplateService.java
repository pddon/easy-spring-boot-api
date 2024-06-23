package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dao.entity.EmailTemplate;

/**
 * @ClassName: EmailTemplateService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:40
 * @Addr: https://pddon.cn
 */
public interface EmailTemplateService {
    EmailTemplate getByScene(String sceneId, String resourceId);
}
