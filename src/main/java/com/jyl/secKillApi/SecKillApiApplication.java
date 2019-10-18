package com.jyl.secKillApi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SecKillApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecKillApiApplication.class, args);
	}

}
