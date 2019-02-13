package com.liushaoming.jseckill.backend.mq;

import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class MQProducer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MQChannelManager mqChannelManager;

    public void send(String msg) {
        //获取当前线程使用的Rabbitmq通道
        Channel channel = mqChannelManager.getSendChannel();
        try {
            logger.info(" [mqSend] '" + msg + "'");
            channel.confirmSelect();

            channel.basicPublish("",
                    MQConstant.QUEUE_NAME_SECKILL,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes());


        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean sendAcked = false;
        try {
            sendAcked = channel.waitForConfirms(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        logger.info("sendAcked={}", sendAcked);
        if (!sendAcked) {
            logger.info("!!!mqSend_NACKED,NOW_RETRY>>>");
            try {
                channel.basicPublish("",
                        MQConstant.QUEUE_NAME_SECKILL,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        msg.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
