package com.pddon.framework.easyapi.utils;

import com.pddon.framework.easyapi.LockDistributedManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DistributedLockDaemonThreadUtil {
    public static class DaemonRefreshThread extends Thread{
        private long startTimeSeconds;
        private LockDistributedManager lockDistributedManager;
        private String lockName;
        private String identifier;
        private long timeoutSeconds;
        
        private boolean alive = true;

        public DaemonRefreshThread(LockDistributedManager lockDistributedManager, String lockName, String identifier, long timeoutSeconds){
            this.lockDistributedManager = lockDistributedManager;
            this.lockName = lockName;
            this.identifier = identifier;
            this.timeoutSeconds = timeoutSeconds;
            this.startTimeSeconds = System.currentTimeMillis() / 1000;
        }

        public void terminate(){
            this.alive = false;
        }
        
        @Override
        public void run() {
            while (alive){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                }
                long currentTimeSeconds = System.currentTimeMillis() / 1000;
                long spendTimeSeconds = currentTimeSeconds - startTimeSeconds;
                long maxLastTime = timeoutSeconds / 3;
                if(timeoutSeconds - spendTimeSeconds < maxLastTime){
                    //刷新锁超时时间
                    if(lockDistributedManager.refreshLock(lockName, identifier, timeoutSeconds)){
                        //更新锁开始时间
                        startTimeSeconds = System.currentTimeMillis() / 1000;
                    }else{
                        alive = false;
                    }
                }
            }
            if(log.isInfoEnabled()){
                log.info("分布式锁[{}]守护线程结束!", lockName);
            }
        }
    }

    public static DaemonRefreshThread startDaemon(LockDistributedManager lockDistributedManager, String lockName, String identifier, long timeoutSeconds){
        DaemonRefreshThread thread = new DaemonRefreshThread(lockDistributedManager, lockName, identifier, timeoutSeconds);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public static void stopDaemon(DaemonRefreshThread thread){
        thread.terminate();
    }
}
