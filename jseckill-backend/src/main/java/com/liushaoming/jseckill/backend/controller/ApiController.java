package com.liushaoming.jseckill.backend.controller;

import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.bean.MQConfigBean;
import com.liushaoming.jseckill.backend.mq.MQChannelManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("api")
@RestController
public class ApiController {
    @Autowired
    private MQChannelManager mqChannelManager;

    @Autowired
    private MQConfigBean mqConfigBean;

    @RequestMapping("ping")
    public String ping() {
        return "pong";
    }

    @RequestMapping("rabbitmq")
    public String rabbitmq() {
//        AMQP.Queue.DeclareOk declareOk = channelInst.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
//        int msgCount = declareOk.getMessageCount();

        Channel channel = mqChannelManager.getSendChannel();
        long msgCount = 0;
        try {
            msgCount = channel.messageCount(mqConfigBean.getQueue());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String , Object> result = new HashMap<>();
        result.put("msgCount", msgCount);
        return JSON.toJSONString(result);
    }
}
