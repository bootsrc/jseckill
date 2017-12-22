package io.github.flylib.seckill.service;

import com.alibaba.fastjson.JSON;
import io.github.flylib.seckill.cache.RedisCacheHandle;
import io.github.flylib.seckill.common.SecKillEnum;
import io.github.flylib.seckill.entity.Product;
import io.github.flylib.seckill.entity.Record;
import io.github.flylib.seckill.entity.User;
import io.github.flylib.seckill.exception.SecKillException;
import io.github.flylib.seckill.mapper.SecKillMapper;
import io.github.flylib.seckill.mq.MQSenderService;
import io.github.flylib.seckill.utils.SecKillUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SecKillService {
    private Logger log = LoggerFactory.getLogger(SecKillService.class);

    @Autowired
    private RedisCacheHandle redisCacheHandle;

    @Autowired
    private SecKillMapper secKillMapper;

//    @Autowired
//    private RabbitMQSender rabbitMQSender;

    @Autowired
    private MQSenderService mqSenderService;

    /**
     * 利用MySQL的update行锁实现悲观锁
     * @param paramMap
     * @return
     */
    @Transactional
    public SecKillEnum handleByPessLockInMySQL(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer)paramMap.get("productId");
        User user = new User(userId);
        Product product = secKillMapper.getProductById(productId);
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(product.getProductName());

        //判断是否重复购买
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy){
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }
        boolean secKillSuccess = secKillMapper.updatePessLockInMySQL(product);
        if (!secKillSuccess){
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        //秒杀成功
        record = new Record(null,user,product,SecKillEnum.SUCCESS.getCode(),SecKillEnum.SUCCESS.getMessage(),new Date());
        log.info(record.toString());
        boolean insertFlag =  secKillMapper.insertRecord(record);
        //插入record成功
        if (insertFlag){
            long addResult = jedis.sadd(hasBoughtSetKey,user.getId().toString());
            if (addResult>0){
                log.info("---------秒杀成功");
                return SecKillEnum.SUCCESS;
            }else {
                throw new SecKillException(SecKillEnum.REPEAT);
            }
        }else {
            throw new SecKillException(SecKillEnum.SYSTEM_EXCEPTION);
        }
    }

    /**
     * MySQL加字段version实现乐观锁
     * @param paramMap
     * @return
     */
    @Transactional
    public SecKillEnum handleByPosiLockInMySQL(Map<String, Object> paramMap){
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer)paramMap.get("productId");
        User user = new User(userId);
        Product product = secKillMapper.getProductById(productId);
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(product.getProductName());

        //判断是否重复购买
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy){
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }
        //库存减一
        int lastStock = product.getStock()-1;
        if (lastStock>=0){
            product.setStock(lastStock);
            boolean secKillSuccess = secKillMapper.updatePosiLockInMySQL(product);
            if (!secKillSuccess){
                //秒杀失败,version被修改
                throw new SecKillException(SecKillEnum.FAIL);
            }
        }else {
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        record = new Record(null,user,product,SecKillEnum.SUCCESS.getCode(),SecKillEnum.SUCCESS.getMessage(),new Date());
        log.info(record.toString());
        boolean insertFlag = secKillMapper.insertRecord(record);
        //插入record成功
        if (insertFlag){
            long addResult = jedis.sadd(hasBoughtSetKey,user.getId().toString());
            if (addResult>0){
                //秒杀成功
                return SecKillEnum.SUCCESS;
            }else {
                //重复秒杀
                log.info("---------重复秒杀");
                throw new SecKillException(SecKillEnum.REPEAT);
            }
        }else {
            //系统错误
            throw new SecKillException(SecKillEnum.SYSTEM_EXCEPTION);
        }
    }

    /**
     * redis的watch监控
     * @param paramMap
     * @return
     */
    public SecKillEnum handleByRedisWatch(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer)paramMap.get("productId");
        User user = new User(userId);
        String productName = jedis.get("product_"+productId);
        String productStockCacheKey = productName+"_stock";
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(productName);

        //watch开启监控
        jedis.watch(productStockCacheKey);

        //判断是否重复购买，注意这里高并发情形下并不安全
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy){
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }

        String stock = jedis.get(productStockCacheKey);
        if (Integer.parseInt(stock)<=0) {
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        //开启Redis事务
        Transaction tx = jedis.multi();
        //库存减一
        tx.decrBy(productStockCacheKey,1);
        //执行事务
        List<Object> resultList = tx.exec();

        if (resultList == null || resultList.isEmpty()) {
            jedis.unwatch();
            //watch监控被更改过----物品抢购失败;
            throw new SecKillException(SecKillEnum.FAIL);
        }

        //添加到已买队列
        long addResult = jedis.sadd(hasBoughtSetKey,user.getId().toString());
        if (addResult>0){
            Product product = new Product(productId);
            //秒杀成功
            record =  new Record(null,user,product,SecKillEnum.SUCCESS.getCode(),SecKillEnum.SUCCESS.getMessage(),new Date());
            //添加record到rabbitMQ消息队列
//            rabbitMQSender.send(JSON.toJSONString(record));
            mqSenderService.sendMessage(JSON.toJSONString(record));
            //返回秒杀成功
            return SecKillEnum.SUCCESS;
        }else {
            //重复秒杀
            //这里抛出RuntimeException异常，redis的decr操作并不会回滚，所以需要手动incr回去
            jedis.incrBy(productStockCacheKey,1);
            throw new SecKillException(SecKillEnum.REPEAT);
        }
    }
}
