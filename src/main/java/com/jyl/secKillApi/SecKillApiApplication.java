package com.jyl.secKillApi;

import com.jyl.secKillApi.rabbitmq.MQConsumer;
import com.jyl.secKillApi.service.SeckillService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@SpringBootApplication
public class SecKillApiApplication {
	private static Logger logger = LogManager.getLogger(SecKillApiApplication.class.getSimpleName());

	public static void main(String[] args) {

		SpringApplication.run(SecKillApiApplication.class, args);
	}

    @Value("${cache.redis.host}")
    private String redisHost;
	@Autowired
	private MQConsumer mqConsumer;
	@Autowired
    private SeckillService seckillService;


	@EventListener(ApplicationReadyEvent.class)
	public void initTask() throws Exception {
		logger.info("Consumer startToConsumeMsg--->");
		mqConsumer.receiveMsgFromJiankuQueue();
		// Pre-load one product to redis
        JedisPool jedisPool = new JedisPool(redisHost);
        try(Jedis jedis = jedisPool.getResource();) {
//            jedis.flushAll();
            jedis.flushDB();
        }
        seckillService.exportSeckillUrl(1L);

	}
}
