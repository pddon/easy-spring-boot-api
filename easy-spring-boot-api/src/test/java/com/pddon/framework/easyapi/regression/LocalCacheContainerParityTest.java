package com.pddon.framework.easyapi.regression;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.pddon.framework.easyapi.cache.LocalCacheContainer;
import com.pddon.framework.easyapi.consts.CacheExpireMode;

/**
 * 优化点5的对照测试：LocalCacheContainer 旧实现 vs 生产实现。
 * 锁定模式（lockFree=false）必须与旧实现完全一致；lockFree=true 验证与锁定模式同语义。
 */
public class LocalCacheContainerParityTest {

    @Test
    public void testLockedModeMatchesLegacyForExpireAfterWrite() {
        List<Object> legacy = runScript(new LegacyOps(new LegacyLocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE)));
        List<Object> produced = runScript(new ProOps(new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false)));
        Assert.assertEquals(legacy, produced);
    }

    @Test
    public void testLockedModeMatchesLegacyForExpireAfterRead() {
        List<Object> legacy = runScript(new LegacyOps(new LegacyLocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_REDA)));
        List<Object> produced = runScript(new ProOps(new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_REDA, false)));
        Assert.assertEquals(legacy, produced);
    }

    @Test
    public void testLockFreeModeHasSameSemanticsAsLockedMode() {
        List<Object> locked = runScript(new ProOps(new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false)));
        List<Object> lockFree = runScript(new ProOps(new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, true)));
        List<Object> legacy = runScript(new LegacyOps(new LegacyLocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE)));
        Assert.assertEquals(locked, lockFree);
        Assert.assertEquals(legacy, lockFree);
    }

    @Test
    public void testInvalidExpireThrowsSameException() {
        Throwable legacy = capture(new Runnable() {
            @Override
            public void run() {
                new LegacyLocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE).set("k", "v", -5L);
            }
        });
        Throwable producedLocked = capture(new Runnable() {
            @Override
            public void run() {
                new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false).set("k", "v", -5L);
            }
        });
        Throwable producedLockFree = capture(new Runnable() {
            @Override
            public void run() {
                new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, true).set("k", "v", -5L);
            }
        });

        Assert.assertNotNull(legacy);
        Assert.assertEquals(legacy.getClass(), producedLocked.getClass());
        Assert.assertEquals(legacy.getMessage(), producedLocked.getMessage());
        Assert.assertEquals(legacy.getClass(), producedLockFree.getClass());
        Assert.assertEquals(legacy.getMessage(), producedLockFree.getMessage());
    }

    @Test
    public void testExpireAfterWriteRemovesEntryOnRead() throws Exception {
        LegacyLocalCacheContainer legacy = new LegacyLocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE);
        LocalCacheContainer produced = new LocalCacheContainer(CacheExpireMode.EXPIRE_AFTER_WRITE, false);
        legacy.set("expire-key", "value", 1L);
        produced.set("expire-key", "value", 1L);
        Assert.assertEquals("value", legacy.get("expire-key", 1L));
        Assert.assertEquals("value", produced.get("expire-key", 1L));

        long deadline = System.currentTimeMillis() + 5000L;
        while (System.currentTimeMillis() < deadline
                && (legacy.get("expire-key", 1L) != null || produced.get("expire-key", 1L) != null)) {
            Thread.sleep(50L);
        }
        Assert.assertNull(legacy.get("expire-key", 1L));
        Assert.assertNull(produced.get("expire-key", 1L));
        Assert.assertNull(legacy.get("expire-key"));
        Assert.assertNull(produced.get("expire-key"));
    }

    private static Throwable capture(Runnable runnable) {
        try {
            runnable.run();
            return null;
        } catch (Throwable e) {
            return e;
        }
    }

    /**
     * 对缓存容器执行相同的操作脚本，输出每一步可观察的结果。
     */
    private static List<Object> runScript(CacheOps c) {
        List<Object> r = new ArrayList<>();
        r.add(c.get("a"));
        r.add(c.get("a", 180L));
        c.set("a", "A", 180L);
        r.add(c.get("a", 180L));
        r.add(c.get("a"));
        r.add(c.exists("a"));
        r.add(c.exists("a", 60L));

        c.set("a", "A2", 60L);
        r.add(c.get("a", 180L));
        r.add(c.get("a", 60L));
        r.add(c.get("a"));

        c.set("b", "B", 60L);
        c.set("b", "B2", 180L, 60L);
        r.add(c.get("b", 60L));
        r.add(c.get("b", 180L));
        r.add(c.get("b"));

        c.set("b", "B3", 180L, 60L);
        r.add(c.get("b", 180L));

        c.setExpire("a", 180L);
        r.add(c.get("a", 60L));
        r.add(c.get("a", 180L));
        r.add(c.get("a"));

        c.setExpire("b", 30L, 180L);
        r.add(c.get("b", 180L));
        r.add(c.get("b", 30L));
        r.add(c.get("b"));

        c.setExpire("missing", 30L);
        r.add(c.get("missing"));
        c.setExpire("missing", 30L, 180L);
        r.add(c.get("missing"));

        c.remove("a", 180L);
        r.add(c.get("a"));
        c.remove("absent-key");
        r.add(c.get("absent-key"));

        c.remove("b", 180L);
        r.add(c.get("b"));
        c.remove("b", 30L);
        r.add(c.get("b"));
        c.remove("b", null);
        r.add(c.get("b"));

        c.clear(180L);
        c.clear(60L);
        r.add(c.get("a"));
        r.add(c.get("b"));

        c.set("c1", "C1", 180L);
        c.set("c2", "C2", 60L);
        r.add(c.get("c1"));
        r.add(c.get("c2"));
        c.clear(null);
        r.add(c.get("c1"));
        r.add(c.get("c2"));
        r.add(c.exists("c1"));
        return r;
    }

    private interface CacheOps {
        void set(String key, Object content, Long seconds);

        void set(String key, Object content, Long expireSeconds, Long oldExpireSeconds);

        void setExpire(String key, Long seconds);

        void setExpire(String key, Long seconds, Long oldExpireSeconds);

        Object get(String key);

        Object get(String key, Long expireSeconds);

        void remove(String key);

        void remove(String key, Long expireSeconds);

        void clear(Long expireSeconds);

        boolean exists(String key);

        boolean exists(String key, Long expireSeconds);
    }

    private static final class LegacyOps implements CacheOps {
        private final LegacyLocalCacheContainer container;

        LegacyOps(LegacyLocalCacheContainer container) {
            this.container = container;
        }

        @Override
        public void set(String key, Object content, Long seconds) {
            container.set(key, content, seconds);
        }

        @Override
        public void set(String key, Object content, Long expireSeconds, Long oldExpireSeconds) {
            container.set(key, content, expireSeconds, oldExpireSeconds);
        }

        @Override
        public void setExpire(String key, Long seconds) {
            container.setExpire(key, seconds);
        }

        @Override
        public void setExpire(String key, Long seconds, Long oldExpireSeconds) {
            container.setExpire(key, seconds, oldExpireSeconds);
        }

        @Override
        public Object get(String key) {
            return container.get(key);
        }

        @Override
        public Object get(String key, Long expireSeconds) {
            return container.get(key, expireSeconds);
        }

        @Override
        public void remove(String key) {
            container.remove(key);
        }

        @Override
        public void remove(String key, Long expireSeconds) {
            container.remove(key, expireSeconds);
        }

        @Override
        public void clear(Long expireSeconds) {
            container.clear(expireSeconds);
        }

        @Override
        public boolean exists(String key) {
            return container.exists(key);
        }

        @Override
        public boolean exists(String key, Long expireSeconds) {
            return container.exists(key, expireSeconds);
        }
    }

    private static final class ProOps implements CacheOps {
        private final LocalCacheContainer container;

        ProOps(LocalCacheContainer container) {
            this.container = container;
        }

        @Override
        public void set(String key, Object content, Long seconds) {
            container.set(key, content, seconds);
        }

        @Override
        public void set(String key, Object content, Long expireSeconds, Long oldExpireSeconds) {
            container.set(key, content, expireSeconds, oldExpireSeconds);
        }

        @Override
        public void setExpire(String key, Long seconds) {
            container.setExpire(key, seconds);
        }

        @Override
        public void setExpire(String key, Long seconds, Long oldExpireSeconds) {
            container.setExpire(key, seconds, oldExpireSeconds);
        }

        @Override
        public Object get(String key) {
            return container.get(key);
        }

        @Override
        public Object get(String key, Long expireSeconds) {
            return container.get(key, expireSeconds);
        }

        @Override
        public void remove(String key) {
            container.remove(key);
        }

        @Override
        public void remove(String key, Long expireSeconds) {
            container.remove(key, expireSeconds);
        }

        @Override
        public void clear(Long expireSeconds) {
            container.clear(expireSeconds);
        }

        @Override
        public boolean exists(String key) {
            return container.exists(key);
        }

        @Override
        public boolean exists(String key, Long expireSeconds) {
            return container.exists(key, expireSeconds);
        }
    }
}
