package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.dto.request.DictListRequest;
import com.pddon.framework.easyapi.dao.entity.DictItem;

import java.util.List;

/**
 * @ClassName: DictItemDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-05-10 00:55
 * @Addr: https://pddon.cn
 */
public interface DictItemDao {
    DictItem getDefaultByDictId(String dictId);

    DictItem getTenantDict(String tenantId, String appId, String dictId);

    DictItem getTenantDefaultDict(String tenantId, String dictId);

    DictItem getUserDict(String userId, String dictId);

    List<DictItem> getDefaultDictsByGroupId(String groupId);

    List<DictItem> getTenantDictsByGroupId(String tenantId, String appId, String groupId);

    List<DictItem> getDefaultTenantDictsByGroupId(String appId, String groupId);

    List<DictItem> getTenantDictsByUserId(String userId, String groupId);

    IPage<DictItem> pageQuery(DictListRequest req);

}
