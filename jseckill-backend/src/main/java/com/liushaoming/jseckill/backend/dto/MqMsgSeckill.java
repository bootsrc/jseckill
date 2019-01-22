package com.liushaoming.jseckill.backend.dto;

import java.io.Serializable;

public class MqMsgSeckill implements Serializable {
    private static final long serialVersionUID = -4206751408398568444L;
    private long seckillId;
    private long userPhone;

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }
}
