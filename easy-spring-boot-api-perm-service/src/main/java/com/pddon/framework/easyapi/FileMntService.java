package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.FileItem;
import com.pddon.framework.easyapi.dto.req.AddFileRequest;
import com.pddon.framework.easyapi.dto.req.FileListRequest;
import com.pddon.framework.easyapi.dto.req.UpdateFileRequest;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: FileMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-06-25 21:43
 * @Addr: https://pddon.cn
 */
public interface FileMntService {
    void remove(IdsRequest req);

    PaginationResponse<FileItem> list(FileListRequest req);

    void update(UpdateFileRequest req);

    IdResponse add(AddFileRequest req);
}
