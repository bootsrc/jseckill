package com.liushaoming.jseckill.backend.boot;

import com.liushaoming.jseckill.backend.constant.RedisKeyPrefix;
import com.liushaoming.jseckill.backend.dao.SeckillDAO;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.mq.MQConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.List;

@Component
public class InitTask implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(InitTask.class);

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;
    @Resource
    private SeckillDAO seckillDAO;
    @Resource
    private MQConsumer mqConsumer;

    @Override
    public void run(String... args) throws Exception {
        initRedis();
        logger.info("StartToConsumeMsg--->");
        mqConsumer.receive();
    }

    /**
     * 预热秒杀数据到Redis
     */
    private void initRedis() {
        Jedis jedis = jedisPool.getResource();
        //清空Redis缓存
        jedis.flushDB();

        List<Seckill> seckillList = seckillDAO.queryAll(0, 10);
        for (Seckill seckill : seckillList) {
            String nameKey = RedisKeyPrefix.SECKILL_NAME + seckill.getSeckillId();
            String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + seckill.getSeckillId();
            jedis.set(nameKey, seckill.getName());
            jedis.set(inventoryKey, String.valueOf(seckill.getInventory()));
        }
        jedis.close();
        logger.info("Redis缓存数据初始化完毕！");
    }
}
