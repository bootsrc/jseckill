package com.liushaoming.jseckill.backend.dao;

import com.liushaoming.jseckill.backend.entity.SuccessKilled;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

public interface SuccessKilledDAO {

    /**
     * 插入购买明细,可过滤重复
     * @param seckillId
     * @param userPhone
     * @return
     * 插入的行数
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone,
                            @Param("nowTime") Date nowTime);

    /**
     * 根据id查询SuccessKilled并携带秒杀产品对象实体
     * @param seckillId
     * @return
     */
    SuccessKilled queryByIdWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

}
