package io.github.flylib.seckill.common;


import io.github.flylib.seckill.constant.SecKillStateConst;

/**
 * @author twc
 */

public enum SecKillEnum {
    /**
     * 服务级错误
     */
    SUCCESS(SecKillStateConst.SUCCESS,"秒杀成功"),
    LOW_STOCKS(SecKillStateConst.FAIL, "库存不足"),
    FAIL(SecKillStateConst.FAIL, "秒杀失败"),
    REPEAT(SecKillStateConst.REPEAT, "重复秒杀"),
    SYSTEM_EXCEPTION(SecKillStateConst.SYSTEM_EXCEPTION, "系统错误"),
    ;

    private String code;

    private String message;

    SecKillEnum(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
