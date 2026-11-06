package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.CaptchaService;
import com.pddon.framework.easyapi.FileItemService;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.annotation.RequiredParam;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.context.RequestContext;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dao.FileItemDao;
import com.pddon.framework.easyapi.dto.FileUploadResult;
import com.pddon.framework.easyapi.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName: FileController
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 18:08
 * @Addr: https://pddon.cn
 */
@Controller
@RequestMapping("res/file")
@Slf4j
public class FileController {

    private final static long MAX_SIZE = 12 * 1024 * 1024;

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private FileItemDao fileItemDao;

    @Autowired
    private CaptchaService captchaService;

    @PostMapping("upload")
    @IgnoreSign
    @RequiredSession
    @ResponseBody
    public FileUploadResult upload(@RequestParam(value = "tenantId", required = false) String tenantId, @RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空，请选择一个文件上传!");
        }
        if(file.getSize() > MAX_SIZE){
            throw new BusinessException("文件必须小于12MB，请压缩后上传!");
        }
        // 获取文件名
        String filename = file.getOriginalFilename();
        // 获取文件的字节
        byte[] data = file.getBytes();
        String contentType = file.getContentType();

        FileItem item = fileItemService.saveFileData(tenantId, filename, contentType, data);
        FileUploadResult result = new FileUploadResult();
        result.setFileKey(item.getFileKey())
                .setFilename(item.getFilename())
                .setFileUrl(item.getFileUrl());
        return result;
    }

    @PostMapping("delete")
    @RequiredSign
    @RequiredSession
    @ResponseBody
    public void delete(@RequestParam("fileKey") String fileKey) throws IOException {
        // 先查记录，校验属主（仅文件上传者本人或超管可删）
        FileItem fileItem = fileItemDao.getByKey(fileKey);
        if (fileItem == null) {
            // 与下载接口保持一致：key 不存在时直接抛"文件不存在"
            throw new BusinessException("文件不存在!");
        }
        String currentUserId = RequestContext.getContext().getUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(fileItem.getCrtUserId());
        boolean isSuper = RequestContext.getContext().isSuperManager();
        if (!isOwner && !isSuper) {
            log.warn("无权删除文件: fileKey={}, crtUserId={}, currentUserId={}, isSuper={}",
                    fileKey, fileItem.getCrtUserId(), currentUserId, isSuper);
            throw new BusinessException("无权删除该文件!");
        }
        log.info("删除文件: fileKey={}, filename={}, operator={}, isSuper={}",
                fileKey, fileItem.getFilename(), currentUserId, isSuper);
        fileItemService.removeByKey(fileKey);
    }

    @GetMapping("captcha.jpg")
    public void getCaptcha(@RequestParam("id") String id, HttpServletResponse response){
        captchaService.generateCaptcha(id, response);
    }
}
