package com.liushaoming.jseckill.backend.mq;


import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.boot.AppContextHolder;
import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.liushaoming.jseckill.backend.dto.SeckillExecution;
import com.liushaoming.jseckill.backend.dto.SeckillMsgBody;
import com.liushaoming.jseckill.backend.enums.AckAction;
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

    @Resource(name = "mqConnectionReceive")
    private Connection mqConnectionReceive;

    public void receive() {
        Channel channel = null;
        try {
            channel = mqConnectionReceive.createChannel();
            channel.queueDeclare(MQConstant.QUEUE_NAME_SECKILL, true, false, false, null);
            channel.basicQos(0, 1, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyDefaultConsumer myDefaultConsumer = new MyDefaultConsumer(channel);

        try {
            channel.basicConsume(MQConstant.QUEUE_NAME_SECKILL, false, myDefaultConsumer);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class MyDefaultConsumer extends DefaultConsumer {
        private Channel channel;

        /**
         * Constructs a new instance and records its association to the passed-in channel.
         *
         * @param channel the channel to which this consumer is attached
         */
        public MyDefaultConsumer(Channel channel) {
            super(channel);
            this.channel = channel;
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                                   AMQP.BasicProperties properties, byte[] body)
                throws IOException {

            long threadId1 = Thread.currentThread().getId();
            logger.info("---receive_threadId_1={}", threadId1);

            String msg = new String(body, "UTF-8");
            logger.info("[mqReceive]  '" + msg + "'");
            SeckillMsgBody msgBody = JSON.parseObject(msg, SeckillMsgBody.class);

            SeckillExecution exeResult = null;
            AckAction ackAction = AckAction.ACCEPT;
            try {
                exeResult = seckillService.doUpdateStock(msgBody.getSeckillId(), msgBody.getUserPhone());
                ackAction = AckAction.ACCEPT;

            } catch (SeckillException seckillE) {
                if (seckillE.getSeckillStateEnum() == SeckillStateEnum.REPEAT_KILL
                        || seckillE.getSeckillStateEnum() == SeckillStateEnum.END) {
                    // SeckillStateEnum.REPEAT_KILL说明数据库中已经保存了记录，这样的多余消息，应该消费掉并应答队列
                    // 秒杀活动时间已经结束的，也不需要更新数据库
//                            logger.info("--LET_MQ_ACK REASON:SeckillStateEnum.REPEAT_KILL,SeckillStateEnum.END");
//                            channel.basicAck(envelope.getDeliveryTag(), false);

                    ackAction = AckAction.THROW;
                } else {
                    logger.error(seckillE.getMessage(), seckillE);
                    logger.info("---->NACK--error_requeue!!!");
//                            channel.basicNack(envelope.getDeliveryTag(), false, true);
                    ackAction = AckAction.RETRY;
                }
            } finally {
                logger.info("------processIt----");
                switch (ackAction) {
                    case ACCEPT:
                        try {
                            logger.info("---->ACK");
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (IOException ioE) {
                            logger.info("---------basicAck_throws_IOException----------");
                            logger.error(ioE.getMessage(), ioE);
                            throw ioE;
                        }
                        break;

                    case THROW:
                        logger.info("--LET_MQ_ACK REASON:SeckillStateEnum.REPEAT_KILL,SeckillStateEnum.END");
                        channel.basicAck(envelope.getDeliveryTag(), false);

                        break;

                    case RETRY:
                        logger.info("---->NACK--error_requeue!!!");
                        channel.basicNack(envelope.getDeliveryTag(), false, true);
                        break;

                }
            }
        }

    }
}
