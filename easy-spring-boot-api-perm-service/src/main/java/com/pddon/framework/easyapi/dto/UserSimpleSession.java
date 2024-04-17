package com.pddon.framework.easyapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.mgt.SimpleSession;

import java.util.Collection;

/**
 * @ClassName: UserSimpleSession
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-17 17:51
 * @Addr: https://pddon.cn
 */
public class UserSimpleSession extends SimpleSession {

    @JsonIgnore
    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
        return super.getAttributeKeys();
    }
}
