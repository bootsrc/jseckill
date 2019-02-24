package com.liushaoming.jseckill.backend.service.impl;

import com.alibaba.fastjson.JSON;
import com.liushaoming.jseckill.backend.bean.ZKConfigBean;
import com.liushaoming.jseckill.backend.constant.RedisKeyPrefix;
import com.liushaoming.jseckill.backend.dao.SeckillDAO;
import com.liushaoming.jseckill.backend.dao.SuccessKilledDAO;
import com.liushaoming.jseckill.backend.dao.cache.RedisDAO;
import com.liushaoming.jseckill.backend.distlock.CuratorClientManager;
import com.liushaoming.jseckill.backend.dto.Exposer;
import com.liushaoming.jseckill.backend.dto.SeckillExecution;
import com.liushaoming.jseckill.backend.dto.SeckillMsgBody;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.entity.SuccessKilled;
import com.liushaoming.jseckill.backend.enums.SeckillStateEnum;
import com.liushaoming.jseckill.backend.exception.SeckillException;
import com.liushaoming.jseckill.backend.mq.MQProducer;
import com.liushaoming.jseckill.backend.service.AccessLimitService;
import com.liushaoming.jseckill.backend.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author liushaoming
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //md5盐值字符串,用于混淆MD5
    private final String salt = "aksksks*&&^%%aaaa&^^%%*";

    //注入Service依赖
    @Autowired
    private SeckillDAO seckillDAO;
    @Autowired
    private SuccessKilledDAO successKilledDAO;
    @Autowired
    private RedisDAO redisDAO;
    @Autowired
    private AccessLimitService accessLimitService;

    @Autowired
    private MQProducer mqProducer;
    @Resource(name = "initJedisPool")
    private JedisPool jedisPool;

    @Resource
    private ZKConfigBean zkConfigBean;

    @Resource
    private CuratorClientManager curatorClientManager;

    private Object sharedObj = new Object();

    /**
     * 优先从缓存中获取数据
     * @return
     */
    @Override
    public List<Seckill> getSeckillList() {
        List<Seckill> list = redisDAO.getAllGoods();
        if (list == null || list.size()<1) {
            list = seckillDAO.queryAll(0, 10);
            redisDAO.setAllGoods(list);
        }
        return list;
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDAO.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        // 优化点:缓存优化:超时的基础上维护一致性
        //1.访问Redis
        Seckill seckill = redisDAO.getSeckill(seckillId);
        if (seckill == null) {
            //2.访问数据库
            seckill = seckillDAO.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //3.存入Redis
                redisDAO.putSeckill(seckill);
            }
        }

        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(),
                    endTime.getTime());
        }
        //转化特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    /**
     * 执行秒杀
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException {
        if (accessLimitService.tryAcquireSeckill()) {
            // 如果没有被限流器限制，则执行秒杀处理
            return handleSeckillAsync(seckillId, userPhone, md5);
        } else {    //如果被限流器限制，直接抛出访问限制的异常
            logger.info("--->ACCESS_LIMITED-->seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.ACCESS_LIMIT);
        }
    }

    /**
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @TODO 先在redis里处理，然后发送到mq，最后减库存到数据库
     */
    private SeckillExecution handleSeckillAsync(long seckillId, long userPhone, String md5)
            throws SeckillException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            // 遇到黑客攻击，检测到了数据篡改
            logger.info("seckill_DATA_REWRITE!!!. seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.DATA_REWRITE);
        }

        long threadId = Thread.currentThread().getId();

        Jedis jedis = jedisPool.getResource();
        String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + seckillId;
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId;

        String inventoryStr = jedis.get(inventoryKey);
        int inventory = Integer.valueOf(inventoryStr);
        if (inventory <= 0) {
            jedis.close();
            logger.info("SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.SOLD_OUT);
        }
        if (jedis.sismember(boughtKey, String.valueOf(userPhone))) {
            jedis.close();
            //重复秒杀
            logger.info("SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
        } else {
            jedis.close();

            // 进入待秒杀队列，进行后续串行操作
            SeckillMsgBody msgBody = new SeckillMsgBody();
            msgBody.setSeckillId(seckillId);
            msgBody.setUserPhone(userPhone);
            mqProducer.send(JSON.toJSONString(msgBody));

            // 立即返回给客户端，说明秒杀成功了
            SuccessKilled successKilled = new SuccessKilled();
            successKilled.setUserPhone(userPhone);
            successKilled.setSeckillId(seckillId);
            successKilled.setState(SeckillStateEnum.ENQUEUE_PRE_SECKILL.getState());
            logger.info("ENQUEUE_PRE_SECKILL>>>seckillId={},userPhone={}", seckillId, userPhone);
            return new SeckillExecution(seckillId, SeckillStateEnum.ENQUEUE_PRE_SECKILL, successKilled);
        }
    }

    /**
     * 在Redis中真正进行秒杀操作
     * @param seckillId
     * @param userPhone
     * @throws SeckillException
     */
    @Override
    public void handleInRedis(long seckillId, long userPhone) throws SeckillException {
        Jedis jedis = jedisPool.getResource();

        String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + seckillId;
        String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId;

        String inventoryStr = jedis.get(inventoryKey);
        int inventory = Integer.valueOf(inventoryStr);
        if (inventory <= 0) {
            logger.info("handleInRedis SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.SOLD_OUT);
        }
        if (jedis.sismember(boughtKey, String.valueOf(userPhone))) {
            logger.info("handleInRedis SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, userPhone);
            throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
        }
        jedis.decr(inventoryKey);
        jedis.sadd(boughtKey, String.valueOf(userPhone));
        logger.info("handleInRedis_done");
    }

    /**
     * 先插入秒杀记录再减库存
     */
    @Override
    @Transactional
    public SeckillExecution updateInventory(long seckillId, long userPhone)
            throws SeckillException {
        //执行秒杀逻辑:减库存 + 记录购买行为
        Date nowTime = new Date();
        try {
            //插入秒杀记录(记录购买行为)
            //这处， seckill_record的id等于这个特定id的行被启用了行锁,   但是其他的事务可以insert另外一行， 不会阻止其他事务里对这个表的insert操作
            int insertCount = successKilledDAO.insertSuccessKilled(seckillId, userPhone, nowTime);
            //唯一:seckillId,userPhone
            if (insertCount <= 0) {
                //重复秒杀
                logger.info("seckill REPEATED. seckillId={},userPhone={}", seckillId, userPhone);
                throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
            } else {
                //减库存,热点商品竞争
                // reduceNumber是update操作，开启作用在表seckill上的行锁
                Seckill currentSeckill = seckillDAO.queryById(seckillId);
                boolean validTime = false;
                if (currentSeckill != null) {
                    long nowStamp = nowTime.getTime();
                    if (nowStamp > currentSeckill.getStartTime().getTime() && nowStamp < currentSeckill.getEndTime().getTime()
                            && currentSeckill.getInventory() > 0 && currentSeckill.getVersion() > -1) {
                        validTime = true;
                    }
                }

                if (validTime) {
                    long oldVersion = currentSeckill.getVersion();
                    // update操作开始，表seckill的seckill_id等于seckillId的行被启用了行锁,   其他的事务无法update这一行， 可以update其他行
                    int updateCount = seckillDAO.reduceInventory(seckillId, oldVersion, oldVersion + 1);
                    if (updateCount <= 0) {
                        //没有更新到记录，秒杀结束,rollback
                        logger.info("seckill_DATABASE_CONCURRENCY_ERROR!!!. seckillId={},userPhone={}", seckillId, userPhone);
                        throw new SeckillException(SeckillStateEnum.DB_CONCURRENCY_ERROR);
                    } else {
                        //秒杀成功 commit
                        SuccessKilled successKilled = successKilledDAO.queryByIdWithSeckill(seckillId, userPhone);
                        logger.info("seckill SUCCESS->>>. seckillId={},userPhone={}", seckillId, userPhone);
                        return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                        //return后，事务结束，关闭作用在表seckill上的行锁
                        // update结束，行锁被取消  。reduceInventory()被执行前后数据行被锁定, 其他的事务无法写这一行。
                    }
                } else {
                    logger.info("seckill_END. seckillId={},userPhone={}", seckillId, userPhone);
                    throw new SeckillException(SeckillStateEnum.END);
                }
            }
        } catch (SeckillException e1) {
            throw e1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //  所有编译期异常 转化为运行期异常
            throw new SeckillException(SeckillStateEnum.INNER_ERROR);
        }
    }

    @Override
    public int isGrab(long seckillId, long userPhone) {
        int result = 0 ;

        try {
            String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId;
            Jedis jedis = jedisPool.getResource();
            result = jedis.sismember(boughtKey, String.valueOf(userPhone)) ? 1 : 0;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            result = 0;
        }
        return result;
    }
}
