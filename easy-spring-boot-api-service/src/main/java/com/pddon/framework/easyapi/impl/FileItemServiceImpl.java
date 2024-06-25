package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.FileItemService;
import com.pddon.framework.easyapi.dao.FileItemDao;
import com.pddon.framework.easyapi.dao.consts.FileStoreType;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName: FileItemServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 18:08
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class FileItemServiceImpl implements FileItemService {

    @Autowired
    private FileItemDao fileItemDao;

    @Override
    public FileItem saveFileData(String filename, String contentType, byte[] data) {
        FileItem fileItem = new FileItem();
        String fileKey = UUIDGenerator.getUUID();
        String suffix = filename.substring(filename.lastIndexOf("."));
        String type = contentType.substring(contentType.lastIndexOf("/") + 1);
        String fileUrl = "/res/download/" + type + "/" + fileKey + suffix;
        fileItem.setFileKey(fileKey)
                .setFilename(filename)
                .setFileData(data)
                .setContentType(contentType)
                .setFileType(type)
                .setDisabled(false)
                .setStoreType(FileStoreType.IN_DB.name())
                .setUseCount(0)
                .setFileUrl(fileUrl);
        fileItemDao.saveItem(fileItem);
        return fileItem;
    }

    @Override
    public FileItem getByTypeKey(String fileType, String fileKey) {
        FileItem fileItem = fileItemDao.getByTypeKey(fileType, fileKey);
        if(fileItem == null){
            throw new BusinessException("文件不存在!");
        }
        return fileItem;
    }

    @Override
    public void removeByKey(String fileKey) {
        fileItemDao.removeByKey(fileKey);
    }
}
