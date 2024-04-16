package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.PermItem;

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
}
