package com.liushaoming.jseckill.backend.bean;

public class ZKConfigBean {
    String lockRoot;
    int sessionTimeout;
    String connectStr;

    public String getLockRoot() {
        return lockRoot;
    }

    public void setLockRoot(String lockRoot) {
        this.lockRoot = lockRoot;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public String getConnectStr() {
        return connectStr;
    }

    public void setConnectStr(String connectStr) {
        this.connectStr = connectStr;
    }
}
