package com.pddon.framework.easyapi.dao.utils;

import redis.clients.jedis.Jedis;

public interface RedisCallback {

    Object doWithRedis(Jedis jedis);
}
