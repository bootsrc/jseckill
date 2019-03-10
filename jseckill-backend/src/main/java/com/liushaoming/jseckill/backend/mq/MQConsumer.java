package com.liushaoming.jseckill.backend.mq;


import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.bean.MQConfigBean;
import com.liushaoming.jseckill.backend.constant.RedisKey;
import com.liushaoming.jseckill.backend.dto.SeckillMsgBody;
import com.liushaoming.jseckill.backend.enums.AckAction;
import com.liushaoming.jseckill.backend.enums.SeckillStateEnum;
import com.liushaoming.jseckill.backend.exception.SeckillException;
import com.liushaoming.jseckill.backend.service.SeckillService;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class MQConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MQConsumer.class);

    @Resource
    private SeckillService seckillService;

    @Resource(name = "mqConnectionReceive")
    private Connection mqConnectionReceive;

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    @Autowired
    private MQConfigBean mqConfigBean;

    public void receive() {
        Channel channel = null;
        try {
            channel = mqConnectionReceive.createChannel();
            channel.queueDeclare(mqConfigBean.getQueue(), true, false, false, null);
            channel.basicQos(0, 1, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MyDefaultConsumer myDefaultConsumer = new MyDefaultConsumer(channel);

        try {
            channel.basicConsume(mqConfigBean.getQueue(), false, myDefaultConsumer);
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

            AckAction ackAction = AckAction.ACCEPT;
            try {
                // 这里演延时2秒，模式秒杀的耗时操作, 上线的时候需要注释掉
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    logger.error(e.getMessage(), e);
//                }
                seckillService.handleInRedis(msgBody.getSeckillId(), msgBody.getUserPhone());
                ackAction = AckAction.ACCEPT;
            } catch (SeckillException seckillE) {
                if (seckillE.getSeckillStateEnum() == SeckillStateEnum.SOLD_OUT
                        || seckillE.getSeckillStateEnum() == SeckillStateEnum.REPEAT_KILL) {
                    // 已售罄，或者此人之前已经秒杀过的
                    ackAction = AckAction.THROW;
                } else {
                    logger.error(seckillE.getMessage(), seckillE);
                    logger.info("---->NACK--error_requeue!!!");
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

                        Jedis jedis = jedisPool.getResource();
                        jedis.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSeckillId() + "@" + msgBody.getUserPhone());
                        jedis.close();
                        break;

                    case THROW:
                        logger.info("--LET_MQ_ACK REASON:SeckillStateEnum.SOLD_OUT,SeckillStateEnum.REPEAT_KILL");
                        channel.basicAck(envelope.getDeliveryTag(), false);

                        Jedis jedis1 = jedisPool.getResource();
                        jedis1.srem(RedisKey.QUEUE_PRE_SECKILL, msgBody.getSeckillId() + "@" + msgBody.getUserPhone());
                        jedis1.close();

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
