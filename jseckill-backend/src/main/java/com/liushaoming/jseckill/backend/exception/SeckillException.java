package com.liushaoming.jseckill.backend.exception;

/**
 * 秒杀相关业务异常
 * Created by liushaoming on 2019-01-14.
 */
public class SeckillException extends RuntimeException {

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
