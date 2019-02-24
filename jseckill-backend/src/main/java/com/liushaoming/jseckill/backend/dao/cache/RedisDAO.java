package com.liushaoming.jseckill.backend.dao.cache;

import com.liushaoming.jseckill.backend.constant.RedisKey;
import com.liushaoming.jseckill.backend.constant.RedisKeyPrefix;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.singleton.MyRuntimeSchema;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
public class RedisDAO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = MyRuntimeSchema.getInstance().getGoodsRuntimeSchema();

    public Seckill getSeckill(long seckillId) {
        //redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = RedisKeyPrefix.SECKILL_GOODS + seckillId;
                byte[] bytes = jedis.get(key.getBytes());
                //缓存中获取到bytes
                if (bytes != null) {
                    //空对象
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    //seckill 被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public String putSeckill(Seckill seckill) {
        // set Object(Seckill) -> 序列化 -> byte[]
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = RedisKeyPrefix.SECKILL_GOODS + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                String result = jedis.set(key.getBytes(), bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 从缓存中获取所有的实时商品数据(包括实时库存量)
     * @return
     */
    public List<Seckill> getAllGoods() {
        List<Seckill> result = new ArrayList<>();
        Jedis jedis = jedisPool.getResource();
        Set<String> idSet = jedis.smembers(RedisKey.SECKILL_ID_SET);
        if (idSet != null || idSet.size() > 0) {
            for (String seckillId : idSet) {
                String goodsKey = RedisKeyPrefix.SECKILL_GOODS + seckillId;
                byte[] bytes = jedis.get(goodsKey.getBytes());
                if (bytes != null) {
                    Seckill seckill = schema.newMessage();
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);

                    try {
                        // goodsKey获取到的库存量是初始值，并不是当前值，所有需要从RedisKeyPrefix.SECKILL_INVENTORY+seckillID
                        // 获取到的库存，再设置到结果中去
                        String inventoryStr = jedis.get(RedisKeyPrefix.SECKILL_INVENTORY + seckillId);
                        if (!StringUtils.isEmpty(inventoryStr)) {
                            seckill.setInventory(Integer.valueOf(inventoryStr));
                        }
                    } catch (NumberFormatException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                    result.add(seckill);
                }
            }
        }
        jedis.close();
        return result;
    }

    public void setAllGoods(List<Seckill> list) {
        Jedis jedis = jedisPool.getResource();
        if (list == null || list.size()< 1) {
            logger.info("--FatalError!!! seckill_list_data is empty");
            return;
        }

        jedis.del(RedisKey.SECKILL_ID_SET);

        for (Seckill seckill : list) {
            jedis.sadd(RedisKey.SECKILL_ID_SET, seckill.getSeckillId() + "");

            String seckillGoodsKey = RedisKeyPrefix.SECKILL_GOODS + seckill.getSeckillId();
            byte[] goodsBytes = ProtostuffIOUtil.toByteArray(seckill, MyRuntimeSchema.getInstance().getGoodsRuntimeSchema(),
                    LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
            jedis.set(seckillGoodsKey.getBytes(), goodsBytes);
        }
        jedis.close();
        logger.info("数据库Goods数据同步到Redis完毕！");
    }

}
