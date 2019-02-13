package com.liushaoming.jseckill.backend.config;

import com.liushaoming.jseckill.backend.constant.MQConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class MQConfig {
    private static final Logger logger = LoggerFactory.getLogger(MQConfig.class);

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

    @Bean("mqConnectionReceive")
    public Connection mqConnectionReceive() throws IOException, TimeoutException {
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

    @Bean("connectFactoryForReceive")
    public ConnectionFactory connectFactoryForReceive() throws IOException, TimeoutException {
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
        return factory;
    }

//    @Bean
//    public org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory() {
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setAddresses(host);
//        connectionFactory.setPort(Integer.valueOf(port));
//        connectionFactory.setUsername(username);
//        connectionFactory.setPassword(password);
//        connectionFactory.setPublisherConfirms(true);
//        connectionFactory.setVirtualHost(virtualHost);
//        return connectionFactory;
//    }

//    @Bean
//    public SimpleMessageListenerContainer messageContainer() {
//        logger.info("---messageContainer_exe--->");
//        SimpleMessageListenerContainer container = null;
//        container = new SimpleMessageListenerContainer(connectionFactory());
//
//        Queue queue = new Queue(MQConstant.QUEUE_NAME_SECKILL, true, false, false, null);
//
//        container.setQueues(queue);
//        container.setExposeListenerChannel(true);
//        container.setMaxConcurrentConsumers(1);
//        container.setConcurrentConsumers(1);
//        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);//消息确认后才能删除
//        container.setPrefetchCount(1);//每次处理5条消息
//        container.setMessageListener(new ChannelAwareMessageListener() {
//            @Override
//            public void onMessage(Message message, Channel channel) throws Exception {
//                byte[] body = message.getBody();
//                logger.info(">>>消费端接收到消息 : " + new String(body));
//                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            }
//        });
//        container.start();
//        logger.info("---SimpleMessageListenerContainer_started>>>");
//        return container;
//    }
}

