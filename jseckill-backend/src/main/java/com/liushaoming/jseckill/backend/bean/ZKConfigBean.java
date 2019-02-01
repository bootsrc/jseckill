package com.liushaoming.jseckill.backend.bean;

public class ZKConfigBean {
    String lockRoot;
    String connectStr;
    int sessionTimeout;
    int connectTimeout;
    int lockAcquireTimeout;

    public String getLockRoot() {
        return lockRoot;
    }

    public void setLockRoot(String lockRoot) {
        this.lockRoot = lockRoot;
    }

    public String getConnectStr() {
        return connectStr;
    }

    public void setConnectStr(String connectStr) {
        this.connectStr = connectStr;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getLockAcquireTimeout() {
        return lockAcquireTimeout;
    }

    public void setLockAcquireTimeout(int lockAcquireTimeout) {
        this.lockAcquireTimeout = lockAcquireTimeout;
    }
}
