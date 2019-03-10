package com.liushaoming.jseckill.backend.mq;

import com.liushaoming.jseckill.backend.bean.MQConfigBean;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 管理当前线程使用的Rabbitmq通道.
 * 使用了ThreadLocal
 */
@Component
public class MQChannelManager {
    private static final Logger logger = LoggerFactory.getLogger(MQChannelManager.class);

    @Resource(name = "mqConnectionSeckill")
    private Connection connection;

    @Autowired
    private MQConfigBean mqConfigBean;

    private ThreadLocal<Channel> localSendChannel = new ThreadLocal<Channel>() {
        public Channel initialValue() {
            try {
                Channel channelInst = connection.createChannel();
                channelInst.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
                return channelInst;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

    };

    /**
     * 获取当前线程使用的Rabbitmq通道
     *
     * @return
     */
    public Channel getSendChannel() {
        logger.info("Send_CurThread.id={}--->", Thread.currentThread().getId());
        Channel channel = localSendChannel.get();
        if (channel == null) {
            //申明队列
            try {
                channel = connection.createChannel();
                channel.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
                localSendChannel.set(channel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return channel;
    }
}
