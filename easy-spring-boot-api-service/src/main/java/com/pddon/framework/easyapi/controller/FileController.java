package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.FileItemService;
import com.pddon.framework.easyapi.annotation.IgnoreSign;
import com.pddon.framework.easyapi.annotation.RequiredParam;
import com.pddon.framework.easyapi.annotation.RequiredSession;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.dao.annotation.IgnoreTenant;
import com.pddon.framework.easyapi.dao.entity.FileItem;
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
       fileItemService.removeByKey(fileKey);
    }
}
