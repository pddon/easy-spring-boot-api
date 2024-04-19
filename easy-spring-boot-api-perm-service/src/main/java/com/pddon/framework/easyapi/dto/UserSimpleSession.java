package com.pddon.framework.easyapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.NoArgsConstructor;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.mgt.SimpleSession;

import java.util.Collection;
import java.util.Map;

/**
 * @ClassName: UserSimpleSession
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-17 17:51
 * @Addr: https://pddon.cn
 */
@NoArgsConstructor
public class UserSimpleSession extends SimpleSession {

    @JsonIgnore
    @Override
    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
        return super.getAttributeKeys();
    }

    @JsonIgnore
    @Override
    public Object getAttribute(Object key) {
        return super.getAttribute(key);
    }

    @JsonIgnore
    @Override
    public void setAttribute(Object key, Object value) {
        super.setAttribute(key, value);
    }
}
