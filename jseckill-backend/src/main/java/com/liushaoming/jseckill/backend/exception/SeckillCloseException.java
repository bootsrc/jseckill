package com.liushaoming.jseckill.backend.exception;

/**
 * 秒杀关闭异常
 * Created by liushaoming on 2019-01-14.
 */
public class SeckillCloseException extends SeckillException {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
