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

}
