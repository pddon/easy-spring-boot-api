package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.FileItem;

/**
 * @ClassName: FileItemDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 18:05
 * @Addr: https://pddon.cn
 */
public interface FileItemDao {
    boolean saveItem(FileItem fileItem);

    FileItem getByTypeKey(String fileType, String fileKey);

    /** 仅按 fileKey 查询（删除时用于属主校验，可能有多条，取最新一条） */
    FileItem getByKey(String fileKey);

    boolean removeByKey(String fileKey);
}
