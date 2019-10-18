package com.jyl.secKillApi.redis;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;

//@Configuration
public class RedisUtil<T> {
    /*
    This class is generic because you might have a requirement to cache the table or API response.
    As it is a generic class, you have to just create an instance of this class with the
    required type you want to cache. It is good practice to have only a single class performing the operation on DB,
    as we are following in the microservice.
     */


    private RedisTemplate<String,T> redisTemplate;
    private HashOperations<String,Object,T> hashOperation;
    private ListOperations<String,T>  listOperation;
    private ValueOperations<String,T> valueOperations;

    public RedisUtil(RedisTemplate<String,T> redisTemplate){
        this.redisTemplate = redisTemplate;
        this.hashOperation = redisTemplate.opsForHash();
        this.listOperation = redisTemplate.opsForList();
        this.valueOperations = redisTemplate.opsForValue();
    }
    public void putMap(String redisKey,Object key,T data) {
        hashOperation.put(redisKey, key, data);
    }
    public T getMapAsSingleEntry(String redisKey,Object key) {
        return  hashOperation.get(redisKey,key);
    }
    public Map<Object, T> getMapAsAll(String redisKey) {
        return hashOperation.entries(redisKey);
    }
    public void putValue(String key,T value) {
        valueOperations.set(key, value);
    }
    public void putValueWithExpireTime(String key,T value,long timeout,TimeUnit unit) {
        valueOperations.set(key, value, timeout, unit);
    }
    public T getValue(String key) {
        return valueOperations.get(key);
    }
    public void setExpire(String key,long timeout,TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }


}
