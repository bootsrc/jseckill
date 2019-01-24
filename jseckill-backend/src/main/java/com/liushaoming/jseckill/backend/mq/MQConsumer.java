package com.liushaoming.jseckill.backend.mq;


import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.boot.AppContextHolder;
import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.liushaoming.jseckill.backend.dto.SeckillMsgBody;
import com.liushaoming.jseckill.backend.enums.SeckillStateEnum;
import com.liushaoming.jseckill.backend.exception.SeckillException;
import com.liushaoming.jseckill.backend.service.SeckillService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class MQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    @Resource
    private SeckillService seckillService;
    @Resource
    private MQChannelManager mqChannelManager;

    public void receive() {
        Channel channel = mqChannelManager.getReceiveChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {

                String msg = new String(body, "UTF-8");
                logger.info("[mqReceive]  '" + msg + "'");
                SeckillMsgBody msgBody = JSON.parseObject(msg, SeckillMsgBody.class);
                try {
                    seckillService.doUpdateStock(msgBody.getSeckillId(), msgBody.getUserPhone());
                    logger.info("---->ACK");
                    channel.basicAck(envelope.getDeliveryTag(), false);

                } catch (SeckillException seckillE) {
                    if (seckillE.getSeckillStateEnum() == SeckillStateEnum.REPEAT_KILL
                            || seckillE.getSeckillStateEnum() == SeckillStateEnum.END) {
                        // SeckillStateEnum.REPEAT_KILL说明数据库中已经保存了记录，这样的多余消息，应该消费掉并应答队列
                        // 秒杀活动时间已经结束的，也不需要更新数据库
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    } else {
                        logger.error(seckillE.getMessage(), seckillE);
                        logger.info("---->NACK--error_requeue!!!");
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                    }
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
