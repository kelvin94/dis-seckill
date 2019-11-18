package com.jyl.secKillApi.rabbitmq;

import com.google.gson.Gson;
import com.jyl.secKillApi.config.MQConfigBean;
import com.jyl.secKillApi.execptions.SeckillException;
import com.jyl.secKillApi.repository.OrderRepository;
import com.jyl.secKillApi.repository.SwagRepository;
import com.jyl.secKillApi.resource.SeckillMsgBody;
import com.jyl.secKillApi.service.SeckillService;
import com.jyl.secKillApi.stateenum.AckAction;
import com.jyl.secKillApi.stateenum.SeckillStateEnum;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

@Component
public class MQConsumer {
    private static Logger logger = LogManager.getLogger(MQConsumer.class.getSimpleName());
    private final Gson gson;
    private final SwagRepository swagRepository;
    private final OrderRepository orderRepository;
    private final Connection mqConnectionReceive;
    private final MQConfigBean mqConfigBean;
    private final JedisPool jedisPool;
    private final SeckillService seckillService;
    public MQConsumer(
            Gson gson,
            SwagRepository swagRepository,
            OrderRepository orderRepository,
            Connection mqConnectionReceive,
            MQConfigBean mqConfigBean,
            JedisPool jedisPool,
            SeckillService seckillService
    ) {
        this.gson = gson;
        this.swagRepository = swagRepository;
        this.orderRepository = orderRepository;
        this.mqConnectionReceive = mqConnectionReceive;
        this.mqConfigBean = mqConfigBean;
        this.jedisPool = jedisPool;
        this.seckillService = seckillService;
    }

    public void receiveMsgFromJiankuQueue() throws Exception {

        Channel channel = null;
        try {
            channel = mqConnectionReceive.createChannel();
            channel.queueDeclare(mqConfigBean.getQueue(),true,false, false,null);
            // false = this channel setting should be applied to each consumer; true = setting applied to the whole channel
            // 2 = prefetchCount, tells broker(RabbitMQ) not to give more than 2 msgs to a worker at a time.
            channel.basicQos(0,2,false);

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        // call MyDefaultConsumer
        MyDefaultConsumer consumer = new MyDefaultConsumer(channel);
        if (channel != null) {
            channel.basicConsume(mqConfigBean.getQueue(), false, consumer);
        }
    }

    private class MyDefaultConsumer extends DefaultConsumer {
        private Channel channel;
        MyDefaultConsumer(Channel channel) {
            super(channel);
            this.channel = channel;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
            // "handleDelivery" = method Called when a delivery appears for this consumer.
            logger.info("In receive_threadId_"+Thread.currentThread().getId());
            String msg = new String(body, "UTF-8");
            SeckillMsgBody msgBody = gson.fromJson(msg, SeckillMsgBody.class);
            AckAction ackAction = AckAction.ACCEPT;

            try {
                logger.info("跳转到handleInRedis - 减库中...");
                seckillService.handleInRedis(msg);
            } catch (SeckillException e) {
                if (e.getSeckillStateEnum() == SeckillStateEnum.SOLD_OUT
                        || e.getSeckillStateEnum() == SeckillStateEnum.REPEAT_KILL) {
                    // 已售罄，或者此人之前已经秒杀过的
                    ackAction = AckAction.THROW;
                } else {
                    // Unknown issues, requeue the msg
                    logger.error(e.getMessage(), e);
                    logger.info("---->NACK--error_requeue!!!");
                    ackAction = AckAction.RETRY;
                }
            } finally {
                switch (ackAction) {
                    case ACCEPT:
                        // response ack to broker
                        logger.info("---->ACK");
                        this.channel.basicAck(envelope.getDeliveryTag(), false);

                        break;
                    case THROW:
                        // response ack to broker
                        logger.info("--LET_MQ_ACK REASON:SeckillStateEnum.SOLD_OUT,SeckillStateEnum.REPEAT_KILL");

                        this.channel.basicAck(envelope.getDeliveryTag(), false);

                        break;
                    case RETRY:
                        this.channel.basicNack(envelope.getDeliveryTag(), false,true);
                        break;
                }
            }
        }

    }
}
