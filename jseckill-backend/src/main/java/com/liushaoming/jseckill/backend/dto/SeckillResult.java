package com.liushaoming.jseckill.backend.dto;

import java.io.Serializable;

/**
 * Created by liushaoming on 15/11/4.
 */
//所有ajax请求放回类型,封装json结果
public class SeckillResult<T> implements Serializable {

    private static final long serialVersionUID = -7301291894175524606L;
    private boolean success;

    private T data;

    private String error;


    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
