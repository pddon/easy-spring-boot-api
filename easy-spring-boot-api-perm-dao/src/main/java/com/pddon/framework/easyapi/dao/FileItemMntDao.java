package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.annotation.RequireDataPermission;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dto.req.FileListRequest;

import java.util.List;

/**
 * @ClassName: FileItemMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:42
 * @Addr: https://pddon.cn
 */
public interface FileItemMntDao {
    boolean removeByIds(List<String> ids);

    @RequireDataPermission(tableFields = {"file_item.crt_user_id"}, tableFieldAlias = {"crt_user_id"})
    IPage<FileItem> pageQuery(FileListRequest req);

    void saveItem(FileItem item);

    FileItem getByItemId(Long id);

    void updateByItemId(FileItem item);

    FileItem getByKey(String fileKey);
}
