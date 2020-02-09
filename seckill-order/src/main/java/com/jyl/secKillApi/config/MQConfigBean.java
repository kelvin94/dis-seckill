package com.jyl.secKillApi.config;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MQConfigBean {
    private boolean publisherConfirms;
    private String queue;

}
