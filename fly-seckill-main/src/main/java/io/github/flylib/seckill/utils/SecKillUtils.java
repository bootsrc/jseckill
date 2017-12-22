package io.github.flylib.seckill.utils;

import io.github.flylib.seckill.constant.RedisCacheConst;

public class SecKillUtils {
    /**
     * 获取用户已买队列key
     * @param productName
     * @return
     */
    public static String getRedisHasBoughtSetKey(String productName){
        String hasBySet = "";
        if (productName!=null && !productName.isEmpty()){
            switch (productName){
                case "iphone":
                    hasBySet = RedisCacheConst.IPHONE_HAS_BOUGHT_SET;
                    break;
                case "huawei":
                    hasBySet = RedisCacheConst.HUAWEI_HAS_BOUGHT_SET;
                    break;
                case "samsung":
                    hasBySet = RedisCacheConst.SAMSUNG_HAS_BOUGHT_SET;
                    break;
                case "xiaomi":
                    hasBySet = RedisCacheConst.XIAOMI_HAS_BOUGHT_SET;
                    break;
            }
        }
        return hasBySet;
    }
}
