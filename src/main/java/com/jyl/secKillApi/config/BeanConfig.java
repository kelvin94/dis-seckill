package com.jyl.secKillApi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jyl.secKillApi.requestInterceptor.CheckAuthInterceptor;
import com.jyl.secKillApi.service.CheckUserAuthorizationService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeanConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ObjectMapper jsonObjectMapper() {
        return new ObjectMapper();
    }

//    @Bean
//    public CheckUserAuthorizationService checkUserAuthorizationService(RestTemplate restTemplate, ObjectMapper
//    jsonObjectMapper) {
//        return new CheckUserAuthorizationService(restTemplate, jsonObjectMapper);
//    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public CheckAuthInterceptor checkAuthInterceptor(CheckUserAuthorizationService checkUserAuthorizationService) {
        return new CheckAuthInterceptor(checkUserAuthorizationService);
    }
}
