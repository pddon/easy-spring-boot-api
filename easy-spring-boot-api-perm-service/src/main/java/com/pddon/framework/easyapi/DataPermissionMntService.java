package com.pddon.framework.easyapi;

import com.pddon.framework.easyapi.annotation.CacheMethodResult;
import com.pddon.framework.easyapi.consts.CacheKeyMode;
import com.pddon.framework.easyapi.controller.request.IdsRequest;
import com.pddon.framework.easyapi.controller.response.ListResponse;
import com.pddon.framework.easyapi.controller.response.PaginationResponse;
import com.pddon.framework.easyapi.dao.dto.DataPermDto;
import com.pddon.framework.easyapi.dao.entity.DataPermission;
import com.pddon.framework.easyapi.dao.entity.DataPermissionResource;
import com.pddon.framework.easyapi.dto.TableInfoDto;
import com.pddon.framework.easyapi.dto.req.*;
import com.pddon.framework.easyapi.dto.resp.IdResponse;

import java.util.List;

/**
 * @ClassName: DataPermissionMntService
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 12:12
 * @Addr: https://pddon.cn
 */
public interface DataPermissionMntService {
    IdResponse add(AddDataPermissionRequest req);

    void update(UpdateDataPermissionRequest req);

    void remove(IdsRequest req);

    PaginationResponse<DataPermission> list(DataPermissionListRequest req);

    IdResponse addResource(AddDataPermissionResoruceRequest req);

    void updateResource(UpdateDataPermissionResourceRequest req);

    void removeResource(IdsRequest req);

    PaginationResponse<DataPermissionResource> listResource(DataPermissionResourceListRequest req);

    void authToRole(AuthToRoleRequest req);

    void authToUser(AuthToUserRequest req);


    @CacheMethodResult(prefix = "User:DataPerms", id = "currentUserId", keyMode = CacheKeyMode.CUSTOM_ID, needCacheField = "cacheable", expireSeconds = 3600)
    List<DataPermDto> getDataPermsByUserId(String currentUserId, boolean cacheable);

    ListResponse<TableInfoDto> listTables();

    ListResponse<String> getOwnerDataPerms(String permId, String ownerId, Boolean roleOwner);
}
