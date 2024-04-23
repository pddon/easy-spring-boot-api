package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.RoleItem;
import com.pddon.framework.easyapi.dto.req.RoleListRequest;

/**
 * @ClassName: RoleItemDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface RoleItemDao {
    boolean exists(String roleId);

    boolean saveRole(RoleItem role);

    RoleItem getByItemId(Long id);

    boolean updateRole(RoleItem role);

    boolean removeByIds(String[] ids);

    RoleItem getByRoleId(String roleId);

    IPage<RoleItem> pageQuery(RoleListRequest req);
}
