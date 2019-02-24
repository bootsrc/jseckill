package com.liushaoming.jseckill.backend.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by liushaoming on 15/10/5.
 * Goods to grab
 * 被抢购的商品
 */
public class Seckill implements Serializable {
    private static final long serialVersionUID = -5161466177783266963L;
    private long seckillId;
    private String name;
    private int inventory;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private long version;

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Seckill{" +
                "seckillId=" + seckillId +
                ", name='" + name + '\'' +
                ", inventory=" + inventory +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", createTime=" + createTime +
                ", version=" + version +
                '}';
    }
}
