package com.pddon.framework.easyapi.utils;

import com.pddon.framework.easyapi.LockDistributedManager;

public class DistributedLockDaemonThreadUtil {
    public static class DaemonRefreshThread extends Thread{
        private final long startTimeSeconds;
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
                    //
                }
                long currentTimeSeconds = System.currentTimeMillis() / 1000;
                long spendTimeSeconds = currentTimeSeconds - startTimeSeconds;
                long maxLastTime = timeoutSeconds / 3;
                if(timeoutSeconds - spendTimeSeconds < maxLastTime){
                    //刷新锁超时时间
                    lockDistributedManager.refreshLock(lockName, identifier, timeoutSeconds);
                }
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
