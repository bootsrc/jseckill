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
     *
     * @param paramMap
     * @return
     */
    @Transactional
    public SecKillEnum handleByPessLockInMySQL(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer) paramMap.get("productId");
        User user = new User(userId);
        Product product = secKillMapper.getProductById(productId);
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(product.getProductName());

        //判断是否重复购买
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy) {
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }
        boolean secKillSuccess = secKillMapper.updatePessLockInMySQL(product);
        if (!secKillSuccess) {
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        //秒杀成功
        record = new Record(null, user, product, SecKillEnum.SUCCESS.getCode(), SecKillEnum.SUCCESS.getMessage(), new Date());
        log.info(record.toString());
        boolean insertFlag = secKillMapper.insertRecord(record);
        //插入record成功
        if (insertFlag) {
            long addResult = jedis.sadd(hasBoughtSetKey, user.getId().toString());
            if (addResult > 0) {
                log.info("---------秒杀成功");
                return SecKillEnum.SUCCESS;
            } else {
                throw new SecKillException(SecKillEnum.REPEAT);
            }
        } else {
            throw new SecKillException(SecKillEnum.SYSTEM_EXCEPTION);
        }
    }

    /**
     * MySQL加字段version实现乐观锁
     *  先updatePosiLockInMySQL,再insertRecord，行锁，会经过两个数据库操作，行锁时间太长，性能低,
     *  优化性能的见方法 {@link #handleByPosiLockInMySQL(Map<String, Object>) }
     * @param paramMap
     * @return
     */
    @Deprecated
    @Transactional
    public SecKillEnum handleByPosiLockInMySQLDeprecated(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer) paramMap.get("productId");
        User user = new User(userId);
        Product product = secKillMapper.getProductById(productId);
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(product.getProductName());

        //判断是否重复购买
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy) {
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }
        //库存减一
        int lastStock = product.getStock() - 1;
        if (lastStock >= 0) {
            product.setStock(lastStock);
            boolean secKillSuccess = secKillMapper.updatePosiLockInMySQL(product);
            if (!secKillSuccess) {
                //秒杀失败,version被修改
                throw new SecKillException(SecKillEnum.FAIL);
            }
        } else {
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        record = new Record(null, user, product, SecKillEnum.SUCCESS.getCode(), SecKillEnum.SUCCESS.getMessage(), new Date());
        log.info(record.toString());
        boolean insertFlag = secKillMapper.insertRecord(record);
        //插入record成功
        if (insertFlag) {
            long addResult = jedis.sadd(hasBoughtSetKey, user.getId().toString());
            if (addResult > 0) {
                //秒杀成功
                return SecKillEnum.SUCCESS;
            } else {
                //重复秒杀
                log.info("---------重复秒杀");
                throw new SecKillException(SecKillEnum.REPEAT);
            }
        } else {
            //系统错误
            throw new SecKillException(SecKillEnum.SYSTEM_EXCEPTION);
        }
    }

    /**
     * 高并发优化 - 对调“插入记录”和“更新库存”的步骤
     *
     * 先插入秒杀记录，然后更新库存，行锁只作用于更新库存前后，能相对减小行锁的作用时间，提高并发性能
     * @param paramMap
     * @return
     */
    @Transactional
    public SecKillEnum handleByPosiLockInMySQL(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer) paramMap.get("productId");
        User user = new User(userId);
        Product product = secKillMapper.getProductById(productId);
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(product.getProductName());

        //判断是否重复购买
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy) {
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }

        record = new Record(null, user, product, SecKillEnum.SUCCESS.getCode(), SecKillEnum.SUCCESS.getMessage(), new Date());
        log.info(record.toString());
        //  插入秒杀记录
        boolean insertFlag = secKillMapper.insertRecord(record);
        // 这处， seckill_record的id等于这个特定id的行被启用了行锁,   但是其他的事务可以insert另外一行， 不会影响其他事务里对这个表的insert操作
        //插入record成功
        if (insertFlag) {
            long addResult = jedis.sadd(hasBoughtSetKey, user.getId().toString());
            if (addResult > 0) {
                //秒杀成功

                //库存减一
                int lastStock = product.getStock() - 1;
                if (lastStock >= 0) {
                    product.setStock(lastStock);

                    // update操作开始，seckill_product的id等于这个特定id的行被启用了行锁,   其他的事务无法update这一行， 可以update其他行
                    boolean secKillSuccess = secKillMapper.updatePosiLockInMySQL(product);

                    if (secKillSuccess) {
                        return SecKillEnum.SUCCESS;     //conn.commit()
                    } else {
                        //秒杀失败,version被修改
                        throw new SecKillException(SecKillEnum.FAIL); //conn.rollback()
                    }
                    // update结束，行锁被取消  。只有updatePosiLockInMySQL()被执行前后数据行被锁定, 其他的事务无法写这一行。
                } else {
                    //库存不足
                    throw new SecKillException(SecKillEnum.LOW_STOCKS);
                }



            } else {
                //重复秒杀
                log.info("---------重复秒杀");
                // 抛出异常SecKillException，属于RuntimeExcepiton会导致mysql事务回滚。
                throw new SecKillException(SecKillEnum.REPEAT);
            }
        } else {
            //系统错误
            throw new SecKillException(SecKillEnum.SYSTEM_EXCEPTION);
        }
    }

    /**
     * redis的watch监控
     *
     * @param paramMap
     * @return
     */
    public SecKillEnum handleByRedisWatch(Map<String, Object> paramMap) {
        Jedis jedis = redisCacheHandle.getJedis();
        Record record = null;
        Integer userId = (Integer) paramMap.get("userId");
        Integer productId = (Integer) paramMap.get("productId");
        User user = new User(userId);
        String productName = jedis.get("product_" + productId);
        String productStockCacheKey = productName + "_stock";
        String hasBoughtSetKey = SecKillUtils.getRedisHasBoughtSetKey(productName);

        //watch开启监控
        jedis.watch(productStockCacheKey);

        //判断是否重复购买，注意这里高并发情形下并不安全
        boolean isBuy = jedis.sismember(hasBoughtSetKey, user.getId().toString());
        if (isBuy) {
            //重复秒杀
            throw new SecKillException(SecKillEnum.REPEAT);
        }

        String stock = jedis.get(productStockCacheKey);
        if (Integer.parseInt(stock) <= 0) {
            //库存不足
            throw new SecKillException(SecKillEnum.LOW_STOCKS);
        }

        //开启Redis事务
        Transaction tx = jedis.multi();
        //库存减一
        tx.decrBy(productStockCacheKey, 1);
        //执行事务
        List<Object> resultList = tx.exec();

        if (resultList == null || resultList.isEmpty()) {
            jedis.unwatch();
            //watch监控被更改过----物品抢购失败;
            throw new SecKillException(SecKillEnum.FAIL);
        }

        //添加到已买队列
        long addResult = jedis.sadd(hasBoughtSetKey, user.getId().toString());
        if (addResult > 0) {
            Product product = new Product(productId);
            //秒杀成功
            record = new Record(null, user, product, SecKillEnum.SUCCESS.getCode(), SecKillEnum.SUCCESS.getMessage(), new Date());
            //添加record到rabbitMQ消息队列
//            rabbitMQSender.send(JSON.toJSONString(record));
            mqSenderService.sendMessage(JSON.toJSONString(record));
            //返回秒杀成功
            return SecKillEnum.SUCCESS;
        } else {
            //重复秒杀
            //这里抛出RuntimeException异常，redis的decr操作并不会回滚，所以需要手动incr回去
            jedis.incrBy(productStockCacheKey, 1);
            throw new SecKillException(SecKillEnum.REPEAT);
        }
    }
}
