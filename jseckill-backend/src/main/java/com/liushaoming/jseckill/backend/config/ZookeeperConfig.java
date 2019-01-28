package com.liushaoming.jseckill.backend.config;

import com.liushaoming.jseckill.backend.bean.ZKConfigBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZookeeperConfig {
    @Value("${zookeeper.lock-root}")
    private String lockRoot;

    @Value("${zookeeper.session-timeout}")
    private String sessionTimeout;

    @Value("${zookeeper.connect-str}")
    private String connectStr;

    @Bean
    public ZKConfigBean zkConfigBean() {
        ZKConfigBean zkConfigBean=new ZKConfigBean();
        zkConfigBean.setLockRoot(lockRoot);
        zkConfigBean.setSessionTimeout(Integer.valueOf(sessionTimeout));
        zkConfigBean.setConnectStr(connectStr);
        return zkConfigBean;
    }
}
