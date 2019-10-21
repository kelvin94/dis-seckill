package com.jyl.secKillApi.service;

import com.google.gson.Gson;
import com.jyl.secKillApi.dto.SeckillExecution;
import com.jyl.secKillApi.dto.UrlExposer;
import com.jyl.secKillApi.entity.SeckillOrder;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.execptions.RepeatkillException;
import com.jyl.secKillApi.execptions.SeckillCloseException;
import com.jyl.secKillApi.execptions.SeckillException;
import com.jyl.secKillApi.repository.OrderRepository;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillParameter;
import com.jyl.secKillApi.stateenum.SeckillStateEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SeckillServiceImpl implements SeckillService {
    private static Logger logger = LogManager.getLogger(SeckillServiceImpl.class.getSimpleName());
    private final Gson gson;
    private final SwagRepository swagRepository;
    private final OrderRepository orderRepository;
    private final JedisPool jedisPool;
    private final String salt = "randomsalt555";
    public SeckillServiceImpl(
            SwagRepository swagRepository,
            OrderRepository orderRepository,
            JedisPool jedisPool,
            Gson gson
    ) {
        this.swagRepository = swagRepository;
        this.orderRepository = orderRepository;
        this.jedisPool = jedisPool;
        this.gson = gson;
    }

    @Override
    public List<SeckillSwag> findAll() {
        return swagRepository.findAll();
    }

    @Override
    public SeckillSwag findBySeckillSwagId(Long seckillSwag_Id) {
        Optional<SeckillSwag> result = swagRepository.findBySeckillSwagId(seckillSwag_Id);
        return result.orElse(null);
    }

    @Override
    public UrlExposer exportSeckillUrl(Long seckillSwagId) {
        logger.info("Begin exportSeckillUrl...");
        /*
            Redis key for swag id: "url:" + seckillSwagId
         */

        // get jedis connection from connection pool
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // check if the key(swag id) is in redis, if exists, decompose the json convert it back to POJO
            if(jedis.exists(getUrlRedisKey(seckillSwagId))) {
                logger.info("Seckill product exists in Redis. Returning obj in redis.");
                String redis_value = jedis.get(getUrlRedisKey(seckillSwagId));
                return gson.fromJson(redis_value, UrlExposer.class);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        } finally {
            // return the jedis resource back to the resource pool
            logger.info("closeing redis connection and return resource back to the resource pool");
            if(jedis != null)
                jedis.close();
        }



        // not in the cache then continue with the normal postgres call and store the url in redis as a json
        logger.info("#.....generating url.. seckillSwagId " + seckillSwagId);

        Optional<SeckillSwag> swag = swagRepository.findBySeckillSwagId(seckillSwagId);
        if(swag.isPresent()) {
            logger.info("Found swag from postgres.");
            //generate md5Url
           String md5Url = getMd5(swag.get().getSeckillSwagId());
            logger.info("md5 hashed url " + md5Url);

            Date startTs = swag.get().getStartTime();
            Date endTs =  swag.get().getEndTime();
            Date now = new Date();
            int currentStockCount = swag.get().getStockCount();
            if(currentStockCount > 0 && now.getTime() > startTs.getTime() && now.getTime() < endTs.getTime()) {
                logger.info("Sales is happening.. current stock count="+currentStockCount + " swagID="+seckillSwagId);
                UrlExposer returnValue = new UrlExposer(
                        true, md5Url, swag.get().getSeckillSwagId(),
                        currentStockCount, swag.get().getStartTime().getTime(), swag.get().getEndTime().getTime(), swag.get().getSeckill_price());

                String str_returnValue = gson.toJson(returnValue);
                try{
                    jedis = jedisPool.getResource();
                    jedis.set(getUrlRedisKey(seckillSwagId), str_returnValue);
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                } finally {
                    // return the jedis resource back to the resource pool
                    if(jedis != null)
                        jedis.close();
                }
                return returnValue;
            }
            return new UrlExposer(false, swag.get().getSeckillSwagId());
        }
        return null;
    }

    private String getUrlRedisKey(Long seckillSwagId) {
        return "swagUrl:"+seckillSwagId;
    }



    private String getSeckillOrderRedisKey(Long userPhone, Long seckillSwagId) {
        return "swagOrder:"+seckillSwagId+":"+userPhone;
    }

    private String getMd5(long seckillSwagId) {
        String base = seckillSwagId + "/" + seckillSwagId;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // roll back if any exception is thrown
    public SeckillExecution executeSeckill(SeckillParameter requestParam) throws Exception {
        Long seckillSwagId = requestParam.getSeckillSwagId();
        Long userPhone = requestParam.getUserPhone();
        String md5Url = requestParam.getMd5Url();

        if (md5Url == null || !md5Url.equalsIgnoreCase(getMd5(seckillSwagId))) {
            throw new SeckillException("seckill data is tampered. hashed result is different.");
        }
        Jedis jedis = null;
        try {
            // check if a SeckillOrder that contains the same phoneNumber and seckillSwagId existing in redis
            // 如果已经存在就避免重复击杀
            // 不存在就存入redis
            String str_order = null;
            try {
                jedis = jedisPool.getResource();
                str_order = jedis.get(getSeckillOrderRedisKey(userPhone, seckillSwagId));
                logger.info("完成从redis读取SeckillOrder: "+str_order);
            } finally {
                if(jedis!=null) {
                    logger.info("Redis conn close");
                    jedis.close();
                }
            }
            long remainingStockCount = 0;
            BigDecimal seckill_price = null;
            long dealStartTs = 0;
            long dealEndTs = 0;

            if(str_order != null) {
                SeckillOrder order = gson.fromJson(str_order, SeckillOrder.class);
                logger.info("Seckill order exists in redis. 重复购买。");
                throw new RepeatkillException("Your order already placed.");
            } else {
                try {
                    // Non-repeated purchase - Update Redis Url with new stockCount
                    jedis = jedisPool.getResource();
                    String seckill_url = jedis.get(getUrlRedisKey(seckillSwagId));
                    UrlExposer url = gson.fromJson(seckill_url, UrlExposer.class);
                    if(url != null && url.getStockCount() > 0) {
                        url.setStockCount(url.getStockCount() - 1 );
                        remainingStockCount = url.getStockCount();
                        dealStartTs = url.getDealStart();
                        dealEndTs = url.getDealEnd();
                        seckill_price = url.getSeckill_price();
                    }
                } finally {
                    if(jedis!=null)
                        jedis.close();
                }
                logger.info("剩余库存 " + remainingStockCount);
                logger.info("(Redis) 完成更新库存");
                if (remainingStockCount <= 0) {
                    throw new SeckillCloseException("Sold out. 卖完啦洗洗睡吧.");
                }
                Date currentSysTime = new Date();
                if(currentSysTime.getTime() > dealStartTs && currentSysTime.getTime() < dealEndTs ) {

                    // 减库存
                    int updatedRows = swagRepository.updateStockCount(remainingStockCount, seckillSwagId);
                    logger.info("(Postgres) 完成更新库存");
                    if (updatedRows != 1) {
                        throw new Exception("Somethings wrong...Updated more than 1 swag's stock_count.");
                    }
                    int state = 1;
                    try {
                        jedis = jedisPool.getResource();
                        SeckillOrder order = new SeckillOrder(seckillSwagId, seckill_price, userPhone, state);
                        jedis.set(getSeckillOrderRedisKey(userPhone, seckillSwagId), gson.toJson(order));
                        logger.info("(Redis) 更新order完笔");
                        orderRepository.insertOder(seckillSwagId, seckill_price, userPhone, state);
                        logger.info("(Postgres) 更新Order完毕");
                    }  catch( DataIntegrityViolationException ex) {
                        logger.error(ex.getMessage());
                        throw new RepeatkillException(Objects.requireNonNull(SeckillStateEnum.stateOf(-1)).getStateInfo());
                    } finally {
                        if(jedis!=null)
                            jedis.close();
                    }

                    return new SeckillExecution(seckillSwagId, 1, Objects.requireNonNull(SeckillStateEnum.stateOf(1)).getStateInfo());
                }

            }

        } catch (RepeatkillException ex) {
            logger.error("userPhone " + userPhone + " try to buy twice this swag id: " + seckillSwagId);
            throw new RepeatkillException("userPhone " + userPhone + " try to buy twice this swag id: " + seckillSwagId);
        } catch (SeckillCloseException ex) {
            throw new SeckillCloseException("user phone " + userPhone);
        } catch (SeckillException ex) {
            // all other exceptions...
            logger.error("##userPhone: " + userPhone + " " + ex.getMessage());
            throw new SeckillException(ex.getMessage());
        }
        return null;
    }
}
