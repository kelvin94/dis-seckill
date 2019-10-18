package com.jyl.secKillApi.repository;

import com.jyl.secKillApi.dto.UrlExposer;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RedisRepository {
    private HashOperations hashOperations;

    private final RedisTemplate redisTemplate;

    public RedisRepository(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = this.redisTemplate.opsForHash();
    }

    // ### REDIS PUT OPERATION
    // void put (H key, HK hashKey, HV value): "key" is a type of partition in the Redis cache
    // Use PUT OPERATION to save a seckill url
    public void save(UrlExposer url) {
        hashOperations.put("url", url.getSeckillSwagId(), url);
    }

    // ### REDIS GET OPERATION
    // GET UrlExposer object by seckill_swag_id
    public UrlExposer findBySeckillSwagId(Long seckill_swag_id) {
        // "hashOperation.get()" takes a hash key and return a Java object, so we need to cast the object to UrlExposer.
        return (UrlExposer) hashOperations.get("url", seckill_swag_id);
    }

    // ### REDIS DELETE OPERATION
    // Remove the cached object with unique hashkey under the key "url"
    public void delete(Long seckill_swag_id) {
        hashOperations.delete("url", seckill_swag_id);
    }

    // ### REDIS VALUES OPERATION
    // "values()" retrieves all the values from the key, below implementation returns a list object but there're
    // operations to return key value pairs.
    public List findAll() {
        return hashOperations.values("url");
    }

    // ### REDIS MULTIGET OPERATION
    // "multiGet()", take in multiple keys at once and get the cached result
    // below implementation returns List of UrlExposer objects by the given seckill_swag_id
    public Map<Long, List<UrlExposer>> multiGetUrls(List<Long> seckill_swag_ids) {
        Map<Long, List<UrlExposer>> map = new HashMap<>();
        List<Object> urls = hashOperations.multiGet("url", seckill_swag_ids);
        int index = 0;
        for(Long seckill_swag_id : seckill_swag_ids) {
            map.put(seckill_swag_id, (List<UrlExposer>) urls.get(index));
            index++;
        }
        return map;
    }
}
