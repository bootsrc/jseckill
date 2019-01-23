package com.liushaoming.jseckill.backend.boot;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class MQConfig {
    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private String port;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.publisher-confirms}")
    private String ublisherConfirms;

    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;


    @Bean("mqConnectionSeckill")
    public Connection mqConnectionSeckill() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //主机
        factory.setHost(host);
        //协议端口号
        factory.setPort(Integer.valueOf(port));
        //用户名
        factory.setUsername(username);
        //密码
        factory.setPassword(password);
        //虚拟主机路径（相当于数据库名）
        factory.setVirtualHost(virtualHost);
        //返回连接
        return factory.newConnection();
    }
}

