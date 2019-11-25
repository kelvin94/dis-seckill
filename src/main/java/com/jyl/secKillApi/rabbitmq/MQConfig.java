package com.jyl.secKillApi.rabbitmq;

import com.jyl.secKillApi.config.MQConfigBean;
import com.jyl.secKillApi.redis.RedisConfiguration;
import com.jyl.secKillApi.util.GeneralUtil;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class MQConfig {
    private static Logger logger = LogManager.getLogger(MQConfig.class.getSimpleName());

    @Value("${app.mqhost}")
    private String MQHost;
    @Value("${app.mqusername}")
    private String MQUsername;
    @Value("${app.mquserpwd}")
    private String MQUserpwd;
    @Value("${app.mqvhost}")
    private String mqVhost;

    @Bean
    public MQConfigBean mqConfigBean() {

        MQConfigBean mqConfigBean = new MQConfigBean();
        mqConfigBean.setQueue(GeneralUtil.jianKuQueuename);
        return mqConfigBean;
    }


    @Bean("mqConnectionSeckill")
    public Connection mqConnectionSeckill() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost(mqVhost);
        connectionFactory.setHost(MQHost);
        connectionFactory.setUsername(MQUsername);
        connectionFactory.setPassword(MQUserpwd);
        return connectionFactory.newConnection();
    }

    @Bean("mqConnectionReceive")
    public Connection mqConnectionReceive() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setVirtualHost(mqVhost);
        connectionFactory.setHost(MQHost);
        connectionFactory.setUsername(MQUsername);
        connectionFactory.setPassword(MQUserpwd);
        return connectionFactory.newConnection();
    }

}
