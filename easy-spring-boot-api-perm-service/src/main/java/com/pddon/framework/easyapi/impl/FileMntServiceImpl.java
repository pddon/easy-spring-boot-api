package com.pddon.framework.easyapi.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.FileMntService;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.FileItemMntDao;
import com.pddon.framework.easyapi.dao.consts.FileStoreType;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dto.req.AddFileRequest;
import com.pddon.framework.easyapi.dto.req.FileListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateFileRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import com.pddon.framework.easyapi.utils.UUIDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @ClassName: FileMntServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:43
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class FileMntServiceImpl implements FileMntService {
    @Autowired
    private FileItemMntDao fileItemMntDao;

    @Override
    public void remove(IdsRequest req) {
        fileItemMntDao.removeByIds(Arrays.asList(req.getIds()));
    }

    @Override
    public PaginationResponse<FileItem> list(FileListRequest req) {
        IPage<FileItem> itemPage = fileItemMntDao.pageQuery(req);
        PaginationResponse<FileItem> page = new PaginationResponse<>();
        page.setSize(itemPage.getSize())
                .setCurrent(itemPage.getCurrent())
                .setTotal(itemPage.getTotal())
                .setPages(itemPage.getPages())
                .setRecords(itemPage.getRecords());
        return page;
    }

    @Override
    public IdResponse add(AddFileRequest req) {
        FileItem item = fileItemMntDao.getByKey(req.getFileKey());
        if(item == null){
            throw new BusinessException("文件未找到!");
        }
        item.setFilename(req.getFilename())
                .setDisabled(req.getDisabled())
                .setComments(req.getComments());
        fileItemMntDao.updateByItemId(item);
        return new IdResponse(item.getId());
    }

    @Override
    public void update(UpdateFileRequest req) {
        FileItem item = fileItemMntDao.getByItemId(req.getId());
        if(item == null){
            throw new BusinessException("文件未找到!");
        }
        item.setFilename(req.getFilename())
                .setDisabled(req.getDisabled())
                .setComments(req.getComments());
        fileItemMntDao.updateByItemId(item);
    }
}
