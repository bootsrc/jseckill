package io.github.flylib.seckill.mq;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

/**
 * @author Frank Liu
 * @version 创建时间：2017年8月17日 上午11:44:22 类说明
 *
 * ActiveMQ
 */
@Component
public class MQSenderService {
	private static final Logger logger = LoggerFactory.getLogger(MQSenderService.class);

	@Resource(name = "jmsTemplate")
	private JmsTemplate jmsTemplate;

	public void sendMessage(Destination destination, final String msg) {
		logger.info(Thread.currentThread().getName() + " 向队列" + destination.toString()
				+ "发送消息---------------------->" + msg);
		jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(msg);
			}
		});
	}

	public void sendMessage(final String msg) {
		String destination = jmsTemplate.getDefaultDestinationName();
		logger.info(Thread.currentThread().getName() + " 向队列" + destination + "发送消息---------------------->" + msg);
		jmsTemplate.send(new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(msg);
			}
		});
	}
}
