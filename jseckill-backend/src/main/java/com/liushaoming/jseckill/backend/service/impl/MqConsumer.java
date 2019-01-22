package com.liushaoming.jseckill.backend.service.impl;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.liushaoming.jseckill.backend.constant.MiscConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MqConsumer {

    private final static Logger logger = LoggerFactory
            .getLogger(MqConsumer.class);

    @JmsListener(destination = MiscConstant.MQ_QUEUE_SECKILL, containerFactory = "jmsQueueListener")
    public void receiveQueue(final TextMessage text, Session session)
            throws JMSException {
        try {
            logger.info("MqConsumer收到报文:" + text.getText());
            text.acknowledge();// 使用手动签收模式，需要手动的调用，如果不在catch中调用session.recover()消息只会在重启服务后重发
        } catch (Exception e) {
            session.recover();// 此不可省略 重发信息使用
        }
    }
}
