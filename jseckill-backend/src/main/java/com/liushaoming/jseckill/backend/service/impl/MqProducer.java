package com.liushaoming.jseckill.backend.service.impl;

import javax.jms.Destination;

import com.liushaoming.jseckill.backend.constant.MiscConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MqProducer {

    @Autowired
    private JmsTemplate jmsTemplate;

    /**
     * 发送消息，estination是发送到的队列，message是待发送的消息
     *
     * @param destination
     * @param message
     */
    public void sendMessage(Destination destination, final String message) {
        System.out.println(jmsTemplate.getDeliveryMode());
        jmsTemplate.convertAndSend(destination, message);
    }

    /**
     * 发送消息，message是待发送的消息
     *
     * @param message
     */
    public void sendMessage(final String message) {
        System.out.println(jmsTemplate.getDeliveryMode());
        jmsTemplate.convertAndSend(MiscConstant.MQ_QUEUE_SECKILL, message);
    }
}
