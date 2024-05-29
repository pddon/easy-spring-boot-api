package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.EmailTemplateDao;
import com.pddon.framework.easyapi.dao.HtmlSegmentDao;
import com.pddon.framework.easyapi.dao.entity.EmailTemplate;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.mapper.EmailTemplateMapper;
import com.pddon.framework.easyapi.dao.mapper.HtmlPageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

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

}
