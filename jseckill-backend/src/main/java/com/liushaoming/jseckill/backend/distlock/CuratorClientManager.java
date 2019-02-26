package com.liushaoming.jseckill.backend.distlock;

import com.liushaoming.jseckill.backend.bean.ZKConfigBean;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 暂时没有用上分布式锁，注释掉代码
 * 等需要启用的时候再取消掉注释
 */
@Component
public class CuratorClientManager {

    private CuratorClientManager() {

    }

//    @Resource
//    private ZKConfigBean zkConfigBean;
//
//    private final ThreadLocal<CuratorFramework> localLock = new ThreadLocal<CuratorFramework>() {
//        @Override
//        protected CuratorFramework initialValue() {
//            return newClient();
//        }
//    };
//
//    private CuratorFramework newClient(){
//        RetryPolicy retry = new ExponentialBackoffRetry(1000, 3);
//        return CuratorFrameworkFactory.builder().connectString(zkConfigBean.getConnectStr())
//                .retryPolicy(retry)
//                .sessionTimeoutMs(zkConfigBean.getSessionTimeout())
//                .connectionTimeoutMs(zkConfigBean.getConnectTimeout())
//                .build();
//    }
//
//    public CuratorFramework getClient() {
//        CuratorFramework client = localLock.get();
//        if (client == null) {
//            client = newClient();
//            localLock.set(client);
//        }
//        return client;
//    }
//
//    public void setClient(CuratorFramework client) {
//        localLock.set(client);
//    }
}
