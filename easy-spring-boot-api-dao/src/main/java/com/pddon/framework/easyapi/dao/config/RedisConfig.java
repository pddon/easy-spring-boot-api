/*
 * 项目名称:cdb-api
 * 类名称:RedisConfig.java
 * 包名称:com.gzzbjkj.config
 *
 * 修改履历:
 *      日期                修正者      主要内容
 *      2018/11/21 16:04    gzzbjkj      初版完成
 *
 * Copyright (c) 2019-2019 八借科技
 */
package com.pddon.framework.easyapi.dao.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pddon.framework.easyapi.dao.cache.RedisOrLocalCache;
import com.pddon.framework.easyapi.utils.IOUtils;
import com.pddon.framework.easyapi.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * Redis配置
 *
 * @author gzzbjkj
 */
@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.timeout}")
    private String strTimeout;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.database}")
    private Integer database;

    @Value("${spring.redis.lettuce.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.lettuce.pool.max-wait}")
    private String strMaxWaitMillis;

    @Value("${spring.redis.lettuce.pool.max-idle}")
    private Integer maxIdle;

    @Value("${spring.redis.lettuce.pool.min-idle}")
    private int minIdle;

    @Bean
    public JedisPool jedisPool() {
        long maxWaitMillis = Long.parseLong(strMaxWaitMillis.replace("ms", ""));
        Integer timeout = Integer.parseInt(strTimeout.replace("ms", ""));

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnBorrow(true);
        if (StringUtils.isBlank(password)) {
            password = null;
        }
        JedisPool jedisPool = null;
        try{
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password, database);
            log.info("------------------------------------------------JedisPool注入成功！------------------------------------------------");
            log.info("redis地址：" + host + ":" + port);
            RedisOrLocalCache.setJedisPool(jedisPool);
        }catch (Exception e){
            log.info("------------------------------------------------JedisPool注入失败，改用本地缓存！------------------------------------------------");
            log.warn(IOUtils.getThrowableInfo(e));
        }
        return jedisPool;
    }

    @SuppressWarnings("all")
    @Bean //该方法的返回对象交于spring容器管理
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setConnectionFactory(factory);
        //key序列化方式
        template.setKeySerializer(redisSerializer);
        //value序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //value hashmap序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        //field序列化  key field  value
        template.setHashKeySerializer(redisSerializer);
        return template;
    }
}
