package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

/**
 * @ClassName: DictMntService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 01:08
 * @Addr: https://pddon.cn
 */
public interface DictMntService {
    IdResponse add(AddDictRequest req);

    void update(UpdateDictRequest req);

    void remove(IdsRequest req);

    PaginationResponse<DictItem> list(DictListRequest req);

    IdResponse addGroup(AddDictGroupRequest req);

    void updateGroup(UpdateDictGroupRequest req);

    void removeGroup(IdsRequest req);

    PaginationResponse<DictGroup> listGroup(DictGroupListRequest req);
}
