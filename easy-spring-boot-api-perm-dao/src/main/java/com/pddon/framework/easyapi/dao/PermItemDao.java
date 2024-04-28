package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dto.req.PermListRequest;

import java.util.List;

/**
 * @ClassName: PermItemDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface PermItemDao {
    List<PermItem> getAllPerms();

    List<PermItem> getByPermIds(List<String> permIdList);

    boolean saveItem(PermItem item);

    PermItem geByItemId(String id);

    boolean updateByItemId(PermItem permItem);

    boolean removeByPermIds(String[] ids);

    IPage<PermItem> pageQuery(PermListRequest req);

    boolean existsPermId(String item);

    void saveItems(List<PermItem> perms);
}
