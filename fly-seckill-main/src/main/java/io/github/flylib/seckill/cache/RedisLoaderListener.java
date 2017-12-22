package io.github.flylib.seckill.cache;

import io.github.flylib.seckill.entity.Product;
import io.github.flylib.seckill.mapper.SecKillMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class RedisLoaderListener {
    private Logger log = LoggerFactory.getLogger(RedisLoaderListener.class);

    @Autowired
    private RedisCacheHandle redisCacheHandle;

    @Autowired
    private SecKillMapper secKillMapper;

    @PostConstruct
    public void initRedis(){
        Jedis jedis = redisCacheHandle.getJedis();
        //清空Redis缓存
        jedis.flushDB();
        List<Product> productList = secKillMapper.getAllProduct();
        for (Product product:productList) {
            jedis.set("product_"+product.getId(),product.getProductName());
            jedis.set(product.getProductName()+"_stock", String.valueOf(product.getStock()));
        }
        log.info("Redis缓存数据初始化完毕！");
    }
}
