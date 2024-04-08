package com.pddon.framework.easyapi;

public interface LockDistributedManager {
    /**
     * 加锁,执行操作前先调用加锁
     * @param lockName 锁的名字
     * @param acquireTimeoutSeconds 获取锁的超时时间
     * @param timeoutSeconds 锁本身的超时时间
     * @return identifier 锁唯一标识，解锁时需要使用
     * @author danyuan
     */
    String lock(String lockName, long acquireTimeoutSeconds, long timeoutSeconds);

    /**
     * 释放锁,执行完成后释放锁
     * @param lockName 锁的名字
     * @param identifier 加锁时返回的标识，解铃还须系铃人，不能释放别人的锁
     * @return
     * @author danyuan
     */
    boolean unlock(String lockName, String identifier);

    /**
     * 刷新锁，检测到业务未执行完，且锁快自动释放时，自动续约
     * @param lockName
     * @param identifier
     * @param timeoutSeconds
     * @return
     */
    boolean refreshLock(String lockName, String identifier, long timeoutSeconds);

    /**
     * 是否支持分布式锁
     * @return
     */
    default boolean support() {
        return true;
    }
}
