package com.jyl.secKillApi;

import com.jyl.secKillApi.rabbitmq.MQConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SecKillApiApplication {
	private static Logger logger = LogManager.getLogger(SecKillApiApplication.class.getSimpleName());

	public static void main(String[] args) {

		SpringApplication.run(SecKillApiApplication.class, args);
	}

	@Autowired
	private MQConsumer mqConsumer;

	// TODO: write an "InitTask" method, to start the MQConsumer
	@EventListener(ApplicationReadyEvent.class)
	public void initTask() throws Exception {
		logger.info("StartToConsumeMsg--->");
		mqConsumer.receiveMsgFromJiankuQueue();
	}
}
