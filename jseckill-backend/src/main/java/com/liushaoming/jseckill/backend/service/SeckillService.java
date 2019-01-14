package com.liushaoming.jseckill.backend.service;

import com.liushaoming.jseckill.backend.dto.Exposer;
import com.liushaoming.jseckill.backend.dto.SeckillExecution;
import com.liushaoming.jseckill.backend.entity.Seckill;
import com.liushaoming.jseckill.backend.exception.RepeatKillException;
import com.liushaoming.jseckill.backend.exception.SeckillCloseException;
import com.liushaoming.jseckill.backend.exception.SeckillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询所有秒杀记录
     *
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     *
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启输出秒杀接口地址,
     * 否则输出系统时间和秒杀时间
     *
     * @param seckillId
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;


    /**
     * 执行秒杀操作by 存储过程
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     */
    SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5);

}

