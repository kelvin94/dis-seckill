package com.jyl.secKillApi.rabbitmq;

import com.google.gson.Gson;
import com.jyl.secKillApi.config.MQConfigBean;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillMsgBody;
import com.jyl.secKillApi.util.GeneralUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class MQProducer {
    private static Logger logger = LogManager.getLogger(MQProducer.class.getSimpleName());


    private final MQConfigBean mqConfigBean;
    private final Gson gson;
    private final MQChannelManager mqChannelManager;
    private final JedisPool jedisPool;

    public MQProducer(
            SwagRepository repo,
            Gson gson,
            MQConfigBean mqConfigBean,
            MQChannelManager mqChannelManager,
            JedisPool jedisPool
    ) {
        this.mqConfigBean = mqConfigBean;
        this.gson = gson;
        this.mqChannelManager = mqChannelManager;
        this.jedisPool = jedisPool;
    }

    public void jianku_send(SeckillMsgBody body) throws IOException {
        logger.info("...[MQProducer]Sending message...msg id: " + body.getMsgId());
        String msg = gson.toJson(body);
        // get current thread's connection
        Channel channel = mqChannelManager.getSendChannel();
        channel.basicPublish("", mqConfigBean.getQueue(), MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
        // MessageProperties.PERSISTENT_TEXT_PLAIN = set messages to be persistent
        logger.info("...[MQProducer] sent msg '" + msg + "'");

        boolean isSentAcked = false;
        try {
            logger.info("...[MQProducer]Waiting broker replies ACK message...");
            isSentAcked = channel.waitForConfirms(100); // listen for confirms, if broker somehow cannot take care of
            // the msg and returns a NAck, will throw an exception
        } catch (InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }

        if (isSentAcked) {
            // Broker successfully get the msg
            // then call redis 做减库
            try (Jedis jedis = jedisPool.getResource()) {
                logger.info("...[MQProducer]Consumer receive msg，put order into Redis...");
                jedis.set(GeneralUtil.getSeckillOrderRedisKey(body.getUserPhone(), body.getSeckillSwagId()),
                        body.getSeckillSwagId() + "@" + body.getUserPhone());
            }
            logger.info("...[MQProducer]Redis - 减库结束...");

        } else {
            // Broker somehow cannot get the msg
            // retry to publish the msg
            logger.info("...[MQProducer]Resending msg...");
            channel.basicPublish("", mqConfigBean.getQueue(), MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());
            logger.info("...[MQProducer] re-sent msg '" + msg + "'");

        }
    }


}
