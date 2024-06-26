package com.pddon.framework.easyapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Getter
public class UserAuthenticationToken implements AuthenticationToken {
    private String sessionId;
    private String userId;
    private String password;
    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public Object getCredentials() {
        return password;
    }
}
