package com.liushaoming.jseckill.backend.mq;

import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class MQProducer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MQChannelManager mqChannelManager;

    public void send(String msg) {
        //获取当前线程使用的Rabbitmq通道
        Channel channel = mqChannelManager.getSendChannel();
        try {
            channel.basicPublish("",
                    MQConstant.QUEUE_NAME_SECKILL,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes());
            System.out.println(" [mqSend] '" + msg + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
