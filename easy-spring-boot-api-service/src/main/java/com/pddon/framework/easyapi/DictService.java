package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.request.DictGroupListRequest;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictGroup;
import com.pddon.framework.easyapi.dao.entity.DictItem;

import java.util.List;

/**
 * @ClassName: DictService
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 20:33
 * @Addr: https://pddon.cn
 */
public interface DictService {
    DictItem getDefaultByDictId(String dictId);

    DictItem getTenantDict(String tenantId, String appId, String dictId);

    DictItem getTenantDefaultDict(String tenantId, String dictId);

    DictItem getUserDict(String userId, String dictId);

    List<DictItem> getDefaultDictsByGroupId(String groupId);

    List<DictItem> getTenantDictsByGroupId(String tenantId, String appId, String groupId);

    List<DictItem> getDefaultTenantDictsByGroupId(String appId, String groupId);

    List<DictItem> getTenantDictsByUserId(String userId, String groupId);

    List<DictGroup> getByParentDictGroupId(String parentId);

    boolean existsGroupId(String groupId);

    DictGroup getByGroupId(String groupId);

    PaginationResponse<DictItem> list(DictListRequest req);

    PaginationResponse<DictGroup> listGroup(DictGroupListRequest req);
}
