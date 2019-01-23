package com.liushaoming.jseckill.backend.boot;

import com.liushaoming.jseckill.backend.mq.MQConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitTask implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitTask.class);
    @Override
    public void run(String... args) throws Exception {
        logger.info("StartToConsumeMsg--->");
        new MQConsumer().receive();
    }
}
