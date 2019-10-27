package com.jyl.secKillApi.rabbitmq;

import com.jyl.secKillApi.config.MQConfigBean;
import com.jyl.secKillApi.util.GeneralUtil;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class MQConfig {
    @Value("${app.mqhost}")
    private String MQHost;
    private final String routingKey_jiankuExchangeToQueue = "jianku_routingKey";

    private final String jianKuExchangename = "jianku_exchange";
//    private final String jianKuQueuename = "jianku_queue";

    @Bean
    public MQConfigBean mqConfigBean() {
        MQConfigBean mqConfigBean = new MQConfigBean();
        mqConfigBean.setQueue(GeneralUtil.jianKuQueuename);
        return mqConfigBean;
    }


    @Bean("mqConnectionSeckill")
    public Connection mqConnectionSeckill() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(MQHost);
        return connectionFactory.newConnection();
    }

    @Bean("mqConnectionReceive")
    public Connection mqConnectionReceive() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(MQHost);
        return connectionFactory.newConnection();
    }




//
//    @Bean
//    public DirectExchange jianKuExchange() {
//        return new DirectExchange(jianKuExchangename);
//    }
//
//    @Bean
//    public Queue jianKuQueue() {
//        return new Queue(jianKuQueuename);
//    }
//
//    @Bean
//    public Binding declareBindingJianKu() {
//        return BindingBuilder.bind(jianKuQueue()).to(jianKuExchange()).with(routingKey_jiankuExchangeToQueue);
//    }
//
//    @Bean
//    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
//        return rabbitTemplate;
//    }


//    @Bean
//    public Declarables topicBindings() {
////        Queue topicQueue1 = new Queue(seckillQueueName, true);
////        TopicExchange topicExchange = new TopicExchange(exchangeName);
////        return new Declarables(topicQueue1, topicExchange,
////                BindingBuilder.bind(topicQueue1).to(topicExchange).with(GeneralUtil.SECKILL_QUEUE_ROUTING_KEY));
//    }

}
