package com.pddon.framework.easyapi.dao;

import com.pddon.framework.easyapi.dao.entity.PermItem;
import com.pddon.framework.easyapi.dao.entity.UserPerm;

import java.util.List;

/**
 * @ClassName: UserPermDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 22:24
 * @Addr: https://pddon.cn
 */
public interface UserPermDao {
    List<UserPerm> getPermsByUserId(String userId);
}
