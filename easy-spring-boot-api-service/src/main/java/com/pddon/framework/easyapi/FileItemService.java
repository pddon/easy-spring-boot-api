package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.dao.entity.FileItem;

/**
 * @ClassName: FileItemService
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 18:08
 * @Addr: https://pddon.cn
 */
public interface FileItemService {
    FileItem saveFileData(String tenantId, String filename, String contentType, byte[] data);

    FileItem getByTypeKey(String fileType, String fileKey);

    void removeByKey(String fileKey);
}
