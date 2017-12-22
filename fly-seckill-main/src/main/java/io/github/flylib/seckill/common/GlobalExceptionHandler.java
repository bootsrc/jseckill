package io.github.flylib.seckill.common;

import io.github.flylib.seckill.exception.SecKillException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice

public class GlobalExceptionHandler {
    private Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = SecKillException.class)
    @ResponseBody
    public Message handleSecKillException(SecKillException secKillException){
        log.info(secKillException.getSecKillEnum().getMessage());
        return new Message(secKillException.getSecKillEnum());
    }
}
