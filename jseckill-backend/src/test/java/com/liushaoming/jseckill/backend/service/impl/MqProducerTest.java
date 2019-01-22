package com.liushaoming.jseckill.backend.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MqProducerTest {
    @Autowired
    private MqProducer mqProducer;

    @Test
    public void sendMessage() {
        for (int i=0;i<5;i++){
            mqProducer.sendMessage("这是秒杀消息"+i);
        }
    }
}