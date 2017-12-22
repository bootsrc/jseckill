package io.github.flylib.seckill.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author frank.liu
 *
 */
public class MQListener implements MessageListener {
	private static final Logger log = LoggerFactory.getLogger(MQListener.class);
	
	@Override
	public void onMessage(Message message) {
		log.info("onMessage()-->");
		// message是什么类型（ActiveMQBytesMessage，还是ActiveMQTextMessage）需要根据消息生产者发送的消息的类型
//		ActiveMQBytesMessage msg = (ActiveMQBytesMessage) message;
//		ByteSequence sequence = msg.getContent();
//		String msgStr = new String(sequence.data);
		
		ActiveMQTextMessage msg = (ActiveMQTextMessage)message;
		String msgStr = "";
		try {
			msgStr = msg.getText();
			log.info("MQ read TextMessage={}", msgStr);
//			throw new RuntimeException();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
