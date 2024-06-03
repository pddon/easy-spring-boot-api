package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.HtmlPageService;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: StaticResourceController
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-03 11:25
 * @Addr: https://pddon.cn
 */
@Controller
@RequestMapping("res")
@Slf4j
public class StaticResourceController {

    @Autowired
    private HtmlPageService htmlPageService;

    //@GetMapping("{pagePath:[a-zA-Z0-9_/]+}.html")
    @GetMapping("**/*.html")
    @IgnoreTenant
    @IgnoreSign
    public ResponseEntity<Resource> htmlPage(HttpServletRequest request, HttpServletResponse response){
        String url = request.getRequestURI();
        String path = url.substring(url.indexOf("/res/") + 5);
        String pagePath = path.substring(0, path.lastIndexOf("."));
        HtmlPage htmlPage = htmlPageService.getByPagePath(pagePath);
        String etag = htmlPage.getChgTime() != null ? String.valueOf(htmlPage.getChgTime().getTime()) : String.valueOf(htmlPage.getCrtTime().getTime());
        // 设置ETag到响应头
        response.setHeader("ETag", etag);
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        // 创建ByteArrayResource对象
        ByteArrayResource resource = null;
        try {
            resource = new ByteArrayResource(htmlPage.getContent().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.warn("非UTF-8字符集页面内容!");
            resource = new ByteArrayResource(htmlPage.getContent().getBytes());
        }
        HttpHeaders headers = new HttpHeaders();
        //headers.add("Content-Disposition", String.format("attachment; filename=\"%s.html\"", htmlPage.getTitle()));
        headers.add("ETag", etag);
        //未命中缓存，直接返回页面
        return ResponseEntity.ok()
                .headers(headers)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .eTag(etag)
                .contentType(MediaType.parseMediaType("text/html"))
                .body(resource);
    }
}
