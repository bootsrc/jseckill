#Java秒杀与抢购模型的架构设计与实现

## 开发环境:
-----------------------------------
IntelliJ IDEA + Maven

## 使用框架:
Spring Boot + Mybatis + Redis + ActiveMQ

##具体内容:
对高并发高负载情形下的应用场景进行分析，以高效地处理资源竞争为目的，设计一个秒杀与抢购模型。
本项目提供了三种解决方案来比较系统的性能： <br/>
1.利用MySQL的update行锁实现悲观锁。 <br/>
2.MySQL加字段version实现乐观锁。<br/>
3.使用Redis作为原子计数器（watch事务+decr操作），RabbitMQ作为消息队列记录用户抢购行为，MySQL做异步存储。 <br/>

##备注:
    1.此项目包含了sql文件，包括表单创建和添加数据。

## 核心原理：
1. 静态内容放在nginx上，有条件的放到CDN里
2. 动态的java处理数据经过下面的过程
     redis --> mq --> db
     经过redis处理后， 只有抢到商品的用户的数据才会写入mq， 程序从mq拉取数据，

     使用java.util.concurrent.CyclicBarrier每次积累N条，再写入db

     写入db的时候，需要先做insert into seckill_cecord， 再update seckill_product (line lock)
     缓解行锁的问题

## 核心代码：
-----------------------------------

### MySQL悲观锁
```java
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
```
### MySQL乐观锁
```java
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
```
Redis 原子计数器+watch
```java
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
```
## 测试方法:
测试地址
```
http://localhost:26000/seckill/baseOnRedisWatchSimple?userId=3&productId=1
```
打包
```
mvn package -Dmaven.test.skip=true
```
