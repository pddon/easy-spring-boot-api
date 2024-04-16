package com.pddon.framework.easyapi.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.shiro.authc.AuthenticationToken;

/**
 * @ClassName: UserAuthenticationToken
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-15 23:56
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthenticationToken implements AuthenticationToken {
    private String sessionId;
    private Session session;
    @Override
    public Object getPrincipal() {
        return sessionId;
    }

    @Override
    public Object getCredentials() {
        return sessionId;
    }
}
