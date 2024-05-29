package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.EmailTemplateMntService;
import com.pddon.framework.easyapi.HtmlPageMntService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: HtmlPageController
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-29 19:44
 * @Addr: https://pddon.cn
 */
@Api(tags = "html页面相关管理接口")
@RestController
@RequestMapping("page")
public class HtmlPageController {

    @Autowired
    private HtmlPageMntService htmlPageMntService;

    
}
