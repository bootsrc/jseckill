package com.liushaoming.jseckill.backend.config;

import com.liushaoming.jseckill.backend.bean.MQConfigBean;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class MQConfig {
    private static final Logger logger = LoggerFactory.getLogger(MQConfig.class);

    /**
     * RabbitMQ集群配置
     */
    @Value("${rabbitmq.address-list}")
    private String addressList;

    @Value("${rabbitmq.username}")
    private String username;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.publisher-confirms}")
    private boolean publisherConfirms;

    @Value("${rabbitmq.virtual-host}")
    private String virtualHost;

    @Value("${rabbitmq.queue}")
    private String queue;

    @Bean
    public MQConfigBean mqConfigBean() {
        MQConfigBean mqConfigBean = new MQConfigBean();

        if (StringUtils.isEmpty(addressList)) {
            throw new InvalidPropertyException(MQConfigBean.class, "addressList", "rabbitmq.address-list is Empty");
        }

        String[] addressStrArr = addressList.split(",");
        List<Address> addressList = new LinkedList<Address>();
        for (String addressStr : addressStrArr) {
            String[] strArr = addressStr.split(":");

            addressList.add(new Address(strArr[0], Integer.valueOf(strArr[1])));
        }
        mqConfigBean.setAddressList(addressList);

        mqConfigBean.setUsername(username);
        mqConfigBean.setPassword(password);
        mqConfigBean.setPublisherConfirms(publisherConfirms);
        mqConfigBean.setVirtualHost(virtualHost);
        mqConfigBean.setQueue(queue);
        return mqConfigBean;
    }

    @Bean("mqConnectionSeckill")
    public Connection mqConnectionSeckill(@Autowired MQConfigBean mqConfigBean) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //用户名
        factory.setUsername(username);
        //密码
        factory.setPassword(password);
        //虚拟主机路径（相当于数据库名）
        factory.setVirtualHost(virtualHost);
        //返回连接
        return factory.newConnection(mqConfigBean.getAddressList());
    }

    @Bean("mqConnectionReceive")
    public Connection mqConnectionReceive(@Autowired MQConfigBean mqConfigBean) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        //用户名
        factory.setUsername(username);
        //密码
        factory.setPassword(password);
        //虚拟主机路径（相当于数据库名）
        factory.setVirtualHost(virtualHost);
        //返回连接
        return factory.newConnection(mqConfigBean.getAddressList());
    }
}

