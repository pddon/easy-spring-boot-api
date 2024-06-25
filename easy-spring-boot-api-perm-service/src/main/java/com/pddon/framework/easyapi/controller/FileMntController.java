package com.pddon.framework.easyapi.controller;

import com.pddon.framework.easyapi.FileMntService;
import com.pddon.framework.easyapi.annotation.RequiredSign;
import com.pddon.framework.easyapi.annotations.OperateLog;
import com.pddon.framework.easyapi.consts.SignScope;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;
import com.pddon.framework.easyapi.exception.BusinessException;
import io.swagger.annotations.Api;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @ClassName: FileMntController
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:43
 * @Addr: https://pddon.cn
 */
@Api(tags = "文件相关管理接口")
@RestController
@RequestMapping("file")
public class FileMntController {
    private final static long MAX_SIZE = 12 * 1024 * 1024;
    @Autowired
    private FileMntService fileMntService;

    @PostMapping("add")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="新增文件", apiName = "file/add")
    @RequiresPermissions("file:add")
    public IdResponse add(@RequestBody AddFileRequest req) throws IOException {
        return fileMntService.add(req);
    }

    @PostMapping("update")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="修改文件", apiName = "file/update")
    @RequiresPermissions("file:update")
    public void update(@RequestBody UpdateFileRequest req){
        fileMntService.update(req);
    }


    @PostMapping("delete")
    @RequiredSign(scope = SignScope.REQUEST)
    @OperateLog(type="删除文件", apiName = "file/delete")
    @RequiresPermissions("file:delete")
    public void remove(@RequestBody IdsRequest req){
        fileMntService.remove(req);
    }

    @PostMapping("list")
    @RequiredSign(scope = SignScope.REQUEST)
    @RequiresPermissions("file:query")
    public PaginationResponse<FileItem> listDict(@RequestBody FileListRequest req){
        return fileMntService.list(req);
    }

}
