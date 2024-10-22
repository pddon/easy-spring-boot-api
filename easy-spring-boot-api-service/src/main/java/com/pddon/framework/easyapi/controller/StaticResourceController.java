package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.FileItemService;
import com.pddon.framework.easyapi.HtmlPageService;
import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.consts.CacheExpireMode;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.dto.request.HtmlPageListRequest;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dto.HtmlPageContentDto;
import com.pddon.framework.easyapi.dto.HtmlPageDetailDto;
import com.pddon.framework.easyapi.dto.HtmlPageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
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

    @Autowired
    private FileItemService fileItemService;

    //@GetMapping("{pagePath:[a-zA-Z0-9_/]+}.html")
    @GetMapping("**/*.html")
    @IgnoreSign
    //@CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
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

    @GetMapping("download/{fileType}/{fileKey}.{suffix}")
    @IgnoreSign
    @IgnoreTenant
    //@CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public ResponseEntity<Resource> getFile(@PathVariable("fileType") String fileType,
                                            @PathVariable("fileKey") String fileKey,
                                            HttpServletRequest request,
                                            HttpServletResponse response) throws UnsupportedEncodingException {
        FileItem fileItem = fileItemService.getByTypeKey(fileType, fileKey);
        String etag = fileItem.getChgTime() != null ? String.valueOf(fileItem.getChgTime().getTime()) : String.valueOf(fileItem.getCrtTime().getTime());
        // 设置ETag到响应头
        response.setHeader("ETag", etag);
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        // 创建ByteArrayResource对象
        ByteArrayResource resource = new ByteArrayResource(fileItem.getFileData());
        HttpHeaders headers = new HttpHeaders();
        if(!fileItem.getContentType().contains("image")){
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", URLEncoder.encode(fileItem.getFilename(), "UTF-8")));
        }
        headers.add("ETag", etag);
        //未命中缓存，直接返回页面
        return ResponseEntity.ok()
                .headers(headers)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .eTag(etag)
                .contentType(MediaType.parseMediaType(fileItem.getContentType()))
                .body(resource);
    }

    @GetMapping("getPagesByScene")
    @RequiredSign(scope = SignScope.REQUEST)
    @ResponseBody
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public List<HtmlPageDto> getPagesByScene(@RequestParam(name = "sceneId") String sceneId,
                                             @RequestParam(name = "resourceId", required = false) String resourceId){
        return htmlPageService.getPagesByScene(sceneId, resourceId);
    }

    @PostMapping("getPagesByScenePage")
    @RequiredSign(scope = SignScope.REQUEST)
    @ResponseBody
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public PaginationResponse<HtmlPageDto> getPagesByScenePage(@RequestBody HtmlPageListRequest req){
        return htmlPageService.list(req);
    }

    @GetMapping("getPageByResId")
    @RequiredSign(scope = SignScope.REQUEST)
    @ResponseBody
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public HtmlPageContentDto getPageByResId(@RequestParam(name = "sceneId") String sceneId,
                                             @RequestParam(name = "resourceId", required = true) String resourceId){
        return htmlPageService.getPageByResId(sceneId, resourceId);
    }

    @GetMapping("searchPage")
    @RequiredSign(scope = SignScope.REQUEST)
    @ResponseBody
    public List<HtmlPageDto> searchPage(@RequestParam(name = "keyword", required = true) String keyword,
                                        @RequestParam(name = "sceneId", required = false) String sceneId){
        return htmlPageService.searchPage(sceneId, keyword);
    }
    @GetMapping("getPageById")
    @RequiredSign(scope = SignScope.REQUEST)
    @ResponseBody
    @CacheMethodResult(expireSeconds = 180, expireMode = CacheExpireMode.EXPIRE_AFTER_WRITE)
    public HtmlPageDetailDto getPageById(@RequestParam(name = "id", required = true) Long id){
        return htmlPageService.getPageById(id);
    }
}
