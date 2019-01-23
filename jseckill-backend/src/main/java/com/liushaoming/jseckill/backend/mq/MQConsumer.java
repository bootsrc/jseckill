package com.liushaoming.jseckill.backend.mq;


import com.liushaoming.jseckill.backend.boot.AppContextHolder;
import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    public void receive() {
        Channel channel =  AppContextHolder.getBean(MQChannelManager.class).getReceiveChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                try {
                    String msg = new String(body, "UTF-8");
                    System.out.println("[x] Received '" + msg + "'");
                    channel.basicAck(envelope.getDeliveryTag(), false);
                } catch(IOException ioE) {
                    logger.error(ioE.getMessage(), ioE);
                    channel.basicNack(envelope.getDeliveryTag(), false, true);
                    throw ioE;
                }
            }
        };

        try {
            channel.basicConsume(MQConstant.QUEUE_NAME_SECKILL, false, consumer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
