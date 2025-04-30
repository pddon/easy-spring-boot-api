package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.DataPermissionResource;
import com.pddon.framework.easyapi.dto.req.DataPermissionResourceListRequest;

import java.util.List;

/**
 * @ClassName: DataPermissionResourceMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
public interface DataPermissionResourceMntDao {
    boolean saveItem(DataPermissionResource resource);

    DataPermissionResource getByItemId(Long id);

    boolean updateByItemId(DataPermissionResource resource);

    boolean removeByIds(List<String> ids);

    IPage<DataPermissionResource> pageQuery(DataPermissionResourceListRequest req);

    List<DataPermissionResource> getByPermIds(List<String> permIds);
}
