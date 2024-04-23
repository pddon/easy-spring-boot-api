package com.pddon.framework.easyapi.dto;

import lombok.AllArgsConstructor;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * @ClassName: AdaptorSession
 * @Description:
 * @Author: Allen
 * @Date: 2024-04-22 01:33
 * @Addr: https://pddon.cn
 */
@AllArgsConstructor
public class AdaptorSession implements Session {
    private com.pddon.framework.easyapi.dto.Session session;
    @Override
    public Serializable getId() {
        return session.getSessionId();
    }

    @Override
    public Date getStartTimestamp() {
        return session.getCreateTime();
    }

    @Override
    public Date getLastAccessTime() {
        return session.getLastTime();
    }

    @Override
    public long getTimeout() throws InvalidSessionException {
        return session.getMaxInactiveInterval();
    }

    @Override
    public void setTimeout(long l) throws InvalidSessionException {
        session.setMaxInactiveInterval(Long.valueOf(l).intValue());
    }

    @Override
    public String getHost() {
        return session.getSessionId();
    }

    @Override
    public void touch() throws InvalidSessionException {

    }

    @Override
    public void stop() throws InvalidSessionException {
        session.invalidate();
    }

    @Override
    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
        return Collections.list(session.getAttributeNames()).stream().map(key -> (Object)key).collect(Collectors.toList());
    }

    @Override
    public Object getAttribute(Object o) throws InvalidSessionException {
        return session.getAttribute(o.toString());
    }

    @Override
    public void setAttribute(Object o, Object o1) throws InvalidSessionException {
        session.setAttribute(o.toString(), o1);
    }

    @Override
    public Object removeAttribute(Object o) throws InvalidSessionException {
        Object val = session.getAttribute(o.toString());
        session.removeAttribute(o.toString());
        return val;
    }
}
