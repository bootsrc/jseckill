package com.liushaoming.jseckill.backend.service;

/**
 * 秒杀前的限流
 */
public interface AccessLimitService {
    /**
     * 尝试获取令牌
     * @return
     */
    boolean tryAcquireSeckill();
}
