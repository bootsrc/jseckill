package io.github.flylib.seckill.exception;

import io.github.flylib.seckill.common.SecKillEnum;

public class SecKillException extends RuntimeException {

    private SecKillEnum secKillEnum;

    public SecKillException(SecKillEnum secKillEnum){
        this.secKillEnum = secKillEnum;
    }

    public SecKillEnum getSecKillEnum() {
        return secKillEnum;
    }

    public void setSecKillEnum(SecKillEnum secKillEnum) {
        this.secKillEnum = secKillEnum;
    }
}
