package com.jyl.secKillApi.service;

import com.google.gson.Gson;
import com.jyl.secKillApi.dto.SeckillExecution;
import com.jyl.secKillApi.dto.UrlExposer;
import com.jyl.secKillApi.entity.SeckillSwag;
import com.jyl.secKillApi.execptions.RepeatkillException;
import com.jyl.secKillApi.execptions.SeckillCloseException;
import com.jyl.secKillApi.execptions.SeckillException;
import com.jyl.secKillApi.rabbitmq.MQProducer;
import com.jyl.secKillApi.repository.OrderRepository;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillMsgBody;
import com.jyl.secKillApi.resource.SeckillParameter;
import com.jyl.secKillApi.stateenum.SeckillStateEnum;
import com.jyl.secKillApi.util.GeneralUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final MQProducer mqProducer;
    private final JedisPool jedisPool;
    private final String salt = "randomsalt555";
    public SeckillServiceImpl(
            SwagRepository swagRepository,
            OrderRepository orderRepository,
            JedisPool jedisPool,
            Gson gson,
            MQProducer mqProducer
    ) {
        this.swagRepository = swagRepository;
        this.orderRepository = orderRepository;
        this.jedisPool = jedisPool;
        this.gson = gson;
        this.mqProducer = mqProducer;
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
            logger.info("Export swag url from Redis--->");
            jedis = jedisPool.getResource();
            // check if the key(swag id) is in redis, if exists, decompose the json convert it back to POJO
            if(jedis.exists(GeneralUtil.getUrlRedisKey(seckillSwagId))) {
                logger.info("Seckill product exists in Redis. Returning obj in redis-->");
                String redis_value = jedis.get(GeneralUtil.getUrlRedisKey(seckillSwagId));
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
        logger.info("Generating url from DB.. seckillSwagId " + seckillSwagId);

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
                logger.info("特价中.. current stock count="+currentStockCount + " swagID="+seckillSwagId);
                UrlExposer returnValue = new UrlExposer(
                        true, md5Url, swag.get().getSeckillSwagId(),
                        currentStockCount, swag.get().getStartTime().getTime(), swag.get().getEndTime().getTime(), swag.get().getSeckill_price());

                String str_returnValue = gson.toJson(returnValue);
                try{
                    jedis = jedisPool.getResource();
                    jedis.set(GeneralUtil.getUrlRedisKey(seckillSwagId), str_returnValue);
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


    private String getMd5(long seckillSwagId) {
        String base = seckillSwagId + "/" + seckillSwagId;
        return DigestUtils.md5DigestAsHex(base.getBytes());
    }

    @Override
    public SeckillExecution executeSeckill(SeckillParameter requestParam) throws Exception {
        Long seckillSwagId = requestParam.getSeckillSwagId();
        Long userPhone = requestParam.getUserPhone();
        String md5Url = requestParam.getMd5Url();

        if (md5Url == null || !md5Url.equalsIgnoreCase(getMd5(seckillSwagId))) {
            throw new SeckillException("seckill data is tampered. hashed url result is different.");
        }
        Jedis jedis = null;
        try {
            // check if a SeckillOrder that contains the same phoneNumber and seckillSwagId existing in redis
            // 如果已经存在就避免重复击杀
            // 不存在就存入redis
            String str_order = null;
            try {
                jedis = jedisPool.getResource();
                logger.debug("key: "+GeneralUtil.getSeckillOrderRedisKey(userPhone, seckillSwagId));
                str_order = jedis.get(GeneralUtil.getSeckillOrderRedisKey(userPhone, seckillSwagId));
            } catch(Exception e) {
                logger.error("Exception thrown..." + e.getMessage());
                e.printStackTrace();
                throw e;
            } finally {
                if(jedis!=null) {
                    logger.info("Redis conn close");
                    jedis.close();
                }
            }

            if(str_order != null) {
                logger.info("Seckill order exists in redis. 重复购买。");
                throw new RepeatkillException("Your order already placed.");
            } else {

                    long threadId = Thread.currentThread().getId();
                /*
                    2019-Oct-26 Update: encapsulate swagID + userPhone as a msg, send the msg to the jianku_exchange
                 */
                SeckillMsgBody msg = new SeckillMsgBody(seckillSwagId, userPhone);
                    // // 进入待秒杀队列，进行后续串行操作
                    mqProducer.jianku_send(msg);
                    // 立即返回给客户端，说明秒杀成功了
                    return new SeckillExecution(seckillSwagId, 1, Objects.requireNonNull(SeckillStateEnum.stateOf(1)).getStateInfo());
//                }

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
    }

    // Purpose: decrement stockcount in redis
    @Transactional
    public void handleInRedis(String mqMessage) {

        SeckillMsgBody body = gson.fromJson(mqMessage, SeckillMsgBody.class);
        Long seckillSwagId = body.getSeckillSwagId();
        Long userPhone = body.getUserPhone();
        logger.info("decrementRedisPGStockCountAndSaveOrder seckillSwagID: "+seckillSwagId + " userPhone: "+ userPhone);

        int remainingStockCount = 0;
        BigDecimal seckill_price = null;
        long dealStartTs = 0;
        long dealEndTs = 0;
        try (Jedis jedis = jedisPool.getResource()) {
            // Non-repeated purchase - Update Redis Url with new stockCount
            String seckill_url = jedis.get(GeneralUtil.getUrlRedisKey(seckillSwagId));
            UrlExposer url = gson.fromJson(seckill_url, UrlExposer.class);
            if (url != null && url.getStockCount() > 0) {
                remainingStockCount = url.getStockCount();
                remainingStockCount--;
                url.setStockCount(remainingStockCount);
                if (url.getStockCount() <= 0) {
                    throw new SeckillCloseException("Sold out. 卖完啦洗洗睡吧.");
                }
                dealStartTs = url.getDealStart();
                dealEndTs = url.getDealEnd();
                seckill_price = url.getSeckill_price();
                Date currentSysTime = new Date();
                if(currentSysTime.getTime() > dealStartTs && currentSysTime.getTime() < dealEndTs ) {
                    // Redis: update stockCount...
                    logger.info("updating redis stockCount...");
                    jedis.set(GeneralUtil.getUrlRedisKey(seckillSwagId), gson.toJson(url));
                    jedis.set(GeneralUtil.getSeckillOrderRedisKey(userPhone, seckillSwagId), "sfasfdsa");// "1" as value to indicate this person has placed an order of this product
                    // If sales is still on-going(here is only double check incase FE exposes the api endpoint for seckill_execute method)
                    //  create a new msg and send to a service to update Postgres stockCount and persist the seckillOrder record.
                    logger.info("Redis part done... now call postgres to insert order");
                }
            }
        }

        // save the order to Postgres
        updateInventory(seckillSwagId, userPhone, remainingStockCount, seckill_price);
    }

    // updateInventory only interacts to postgres
    private void updateInventory(long seckillid, long userPhone, int remainStockCount, BigDecimal seckill_price) {
        logger.info("Updating inventory...");
        swagRepository.updateStockCount(remainStockCount, seckillid);
        logger.info("Inserting order...");
        orderRepository.insertOder(seckillid, seckill_price,userPhone, 1); // state: 1 = 秒杀成功
    }
}
