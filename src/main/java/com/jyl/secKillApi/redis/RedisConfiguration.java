package com.jyl.secKillApi.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
public class RedisConfiguration {
    // CachingConfigurerSupport is an impl class, that impl CachingConfigurer interface, which provides methods
    //      to specify explicitly how caches are resolved and how keys are generated for annotation-driven cache management.

    private static Logger logger = LogManager.getLogger(RedisConfiguration.class.getSimpleName());

    @Value("${cache.redis.host}")
    private String REDIS_HOSTNAME;
    @Value("${cache.redis.port}")
    private int REDIS_PORT;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(REDIS_HOSTNAME, REDIS_PORT);
        redisStandaloneConfiguration.setPassword(RedisPassword.none());
        return new JedisConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    public JedisPool jedisPool() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        logger.info("JedisPoolConfig Injection begins...注入开始: ");

        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return new JedisPool(poolConfig, "localhost");
    }

}
