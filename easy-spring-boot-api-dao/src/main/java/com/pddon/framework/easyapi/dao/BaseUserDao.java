package com.pddon.framework.easyapi.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pddon.framework.easyapi.dao.dto.request.UserListRequest;
import com.pddon.framework.easyapi.dao.entity.BaseUser;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: BaseUserDao
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-10 18:11
 * @Addr: https://pddon.cn
 */
public interface BaseUserDao<K extends BaseUser> {
    BaseUser getBySessionId(String sessionId);

    BaseUser getByUserId(String userId);

    boolean updateUserSession(String sessionId, Date loginTime, String userId);

    boolean existUserId(String userId);

    boolean saveUser(BaseUser user);

    IPage<K> pageQuery(UserListRequest req);

    boolean existUsername(String username);

    BaseUser getByItemId(Long id);

    boolean updateUser(K user);

    boolean delete(List<Long> ids);

    boolean updateUserPass(Long id, String newPassword);
}
