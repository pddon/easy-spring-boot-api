package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.DataPermission;
import com.pddon.framework.easyapi.dto.req.DataPermissionListRequest;

import java.util.List;

/**
 * @ClassName: DataPermissionMntDao
 * @Description:
 * @Author: Allen
 * @Date: 2025-04-28 11:43
 * @Addr: https://pddon.cn
 */
public interface DataPermissionMntDao {
    boolean saveItem(DataPermission dataPermission);

    DataPermission getByItemId(Long id);

    boolean updateByItemId(DataPermission dataPermission);

    boolean removeByIds(List<String> ids);

    IPage<DataPermission> pageQuery(DataPermissionListRequest req);

    boolean exists(String permId);

    List<DataPermission> getByPermIds(List<String> permIds, String queryType);

    DataPermission getByPermId(String permId);
}
