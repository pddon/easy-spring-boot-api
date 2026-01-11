package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.FileItemDao;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dao.mapper.FileItemMapper;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: FileItemDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-24 18:05
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class FileItemDaoImpl extends ServiceImpl<FileItemMapper, FileItem> implements FileItemDao {

    @Override
    public boolean saveItem(FileItem fileItem) {
        return this.save(fileItem);
    }

    @Override
    public FileItem getByTypeKey(String fileType, String fileKey) {
        List<FileItem> items = this.lambdaQuery().eq(FileItem::getFileKey, fileKey).eq(StringUtils.isNotBlank(fileType), FileItem::getFileType, fileType).orderByDesc(FileItem::getCrtTime).list();
        if(items.isEmpty()){
            return null;
        }
        return items.get(0);
    }

    @Override
    public boolean removeByKey(String fileKey) {
        return this.remove(new LambdaQueryWrapper<FileItem>().eq(FileItem::getFileKey, fileKey));
    }
}
