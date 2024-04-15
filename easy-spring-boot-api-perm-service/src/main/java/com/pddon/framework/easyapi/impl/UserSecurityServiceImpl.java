package com.pddon.framework.easyapi.impl;

import com.pddon.framework.easyapi.UserSecurityService;
import com.pddon.framework.easyapi.dao.entity.BaseUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @ClassName: UserSecurityServiceImpl
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 23:41
 * @Addr: https://pddon.cn
 */
@Service
@Slf4j
public class UserSecurityServiceImpl implements UserSecurityService {
    @Override
    public BaseUser queryBySessionId(String sessionId) {
        return null;
    }

    @Override
    public Set<String> getUserPermissions(String userId) {
        return null;
    }
}
