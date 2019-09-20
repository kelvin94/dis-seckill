package com.jyl.secKillApi.config;

import com.jyl.secKillApi.service.CheckUserAuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public CheckUserAuthorizationService checkUserAuthorizationService() {
        return new CheckUserAuthorizationService();
    }


}
