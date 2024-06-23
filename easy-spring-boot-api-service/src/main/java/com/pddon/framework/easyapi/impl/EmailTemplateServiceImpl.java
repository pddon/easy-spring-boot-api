package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.EmailTemplateService;
import com.pddon.framework.easyapi.dao.EmailTemplateDao;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName: EmailTemplateServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:40
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    @Autowired
    private EmailTemplateDao emailTemplateDao;

    @Override
    public EmailTemplate getByScene(String sceneId, String resourceId){
        return emailTemplateDao.getByScene(sceneId, resourceId);
    }
}
