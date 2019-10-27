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

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SeckillServiceImpl implements SeckillService {
    private static Logger logger = LogManager.getLogger(SeckillServiceImpl.class.getSimpleName());
    private final Gson gson;
    private final SwagRepository swagRepository;
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
            jedis = jedisPool.getResource();
            // check if the key(swag id) is in redis, if exists, decompose the json convert it back to POJO
            if(jedis.exists(GeneralUtil.getUrlRedisKey(seckillSwagId))) {
                logger.info("Seckill product exists in Redis. Returning obj in redis.");
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
                str_order = jedis.get(GeneralUtil.getSeckillOrderRedisKey(userPhone, seckillSwagId));
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
}
