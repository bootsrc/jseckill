package com.liushaoming.jseckill.backend.exception;

/**
 * 重复秒杀异常(运行期异常)
 * Created by liushaoming on 2019-01-14.
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
