package com.pddon.framework.easyapi.dao.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pddon.framework.easyapi.dao.FileItemMntDao;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dao.entity.HtmlPage;
import com.pddon.framework.easyapi.dao.mapper.FileItemMapper;
import com.pddon.framework.easyapi.dto.req.FileListRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @ClassName: FileItemMntDaoImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:42
 * @Addr: https://pddon.cn
 */
@Repository
@Slf4j
public class FileItemMntDaoImpl extends ServiceImpl<FileItemMapper, FileItem> implements FileItemMntDao {
    @Override
    public boolean removeByIds(List<String> ids) {
        return this.remove(new LambdaQueryWrapper<FileItem>().in(FileItem::getId, ids));
    }

    @Override
    public IPage<FileItem> pageQuery(FileListRequest req) {
        Page<FileItem> page = new Page<>(req.getCurrent(), req.getSize());
        Wrapper<FileItem> wrapper = new LambdaQueryWrapper<FileItem>()
                .select(FileItem.class, tableFieldInfo -> {
                    if(tableFieldInfo.getColumn().equalsIgnoreCase("file_data")){
                        return false;
                    }
                    return true;
                })
                .eq(req.getType() != null, FileItem::getFileType, req.getType())
                .gt(req.getStartTime() != null, FileItem::getCrtTime, req.getStartTime())
                .lt(req.getEndTime() != null, FileItem::getCrtTime, req.getEndTime())
                .and(!StringUtils.isEmpty(req.getKeyword()), query -> {
                    return query.likeLeft(FileItem::getFilename, req.getKeyword()).or()
                            .likeLeft(FileItem::getComments, req.getKeyword());
                })
                .orderBy(!StringUtils.isEmpty(req.getOrderBy()), req.getIsAsc(), "crtTime".equals(req.getOrderBy()) ? FileItem::getCrtTime : FileItem::getChgTime);
        return this.page(page, wrapper);
    }

    @Override
    public void saveItem(FileItem item) {
        this.save(item);
    }

    @Override
    public FileItem getByItemId(Long id) {
        return this.getById(id);
    }

    @Override
    public void updateByItemId(FileItem item) {
        this.updateById(item);
    }

    @Override
    public FileItem getByKey(String fileKey) {
        List<FileItem> items = this.lambdaQuery().eq(FileItem::getFileKey, fileKey).orderByDesc(FileItem::getCrtTime).list();
        if(items.isEmpty()){
            return null;
        }
        return items.get(0);
    }
}
