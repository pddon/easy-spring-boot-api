package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.EmailTemplateMntService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: EmailTemplateController
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:44
 * @Addr: https://pddon.cn
 */
@Api(tags = "邮件模板相关管理接口")
@RestController
@RequestMapping("emailTemplate")
public class EmailTemplateController {

    @Autowired
    private EmailTemplateMntService emailTemplateMntService;


}
