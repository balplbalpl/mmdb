package com.mmdb.service.mq;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

public class MQTopicSender {
	private Log logger = LogFactory.getLogger("MQTopicSender");
	
	public  String queueURI ;
	
	public  String queueName ;
	/**
	 * 队列名。
	 */
	private String topicName;

	private TopicConnection topicConn;

	private Session session;

	private Topic topic;

	/**
	 * 消息发送器。
	 */
	private MessageProducer producer;

	public MQTopicSender() {
		try {
			init();
		} catch (Exception e) {
			logger.eLog("建立消息生产者时失败,原因:"+e.getMessage(), e);
		}
	}
	
	public void init() throws Exception {
		loadConfig();
		this.connect2MQ();
	}

	//加载配置文件到内存
	public void loadConfig() {
		try {
			ResourceBundle init = ResourceBundle
							.getBundle("config.demo.demo-global");
			
			queueURI = init.getString("amq.url");
			topicName = init.getString("amq.queue.topicName");
			
		} catch (Exception e) {
			logger.eLog("加载MQ配置时发生错误",e);
		}
	}
	
	private void connect2MQ() throws Exception {

		if (topicName == null) {
			throw new Exception("No topic name is defined.");
		}
		TopicConnectionFactory topicConnFactory = new ActiveMQConnectionFactory(queueURI);
		
		topicConn = topicConnFactory.createTopicConnection();
		// 4. start queue connection
		topicConn.start();
		// 5.create queue session object
		session = topicConn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		topic = session.createTopic(topicName);
		// 6.create a queue receiver from queue session
		try {
			producer = session.createProducer(topic);

		} catch (Exception e) {
			logger.eLog("Create topic receiver failed: " + e.getMessage());
			throw e;
		}
		// Listening on port
		logger.iLog("Connect to topic " + this.topicName + " completely.");
	}

	/**
	 * 发送结果到指定队列。
	 * 
	 * @param jsonString
	 * @return 发送成功返回true;否则返回false.
	 */
	public boolean send(String jsonString) {
//		logger.info("Send to topic " + topicName);
		try {
			// 发送的数据
			Message msg = this.session.createTextMessage(jsonString);
			producer.send(msg);
			return true;
		} catch (JMSException e) {
			if (e instanceof IllegalStateException) {
				this.destory();
				for (int i = 1; i < 100; i++) {
					logger.eLog("MQ连接状态异常, 重连第" + i + "次.");
					try {
						connect2MQ();
						// 发送的数据
						Message msg = this.session.createTextMessage(jsonString);

						producer.send(msg);
						break;
					} catch (Exception e1) {
						logger.eLog("重连失败!", e);
					}
				}
			}
			return false;
		}
	}

	/**
	 * 发送结果到指定队列。
	 * 
	 * @param xml
	 * @return 发送成功返回true;否则返回false.
	 */
	public boolean send(Object obj) {
//		logger.info("Send to topic " + topicName);
		try {
			// 发送的数据
			ObjectMessage msg = this.session
					.createObjectMessage((Serializable) obj);

			producer.send(msg);
			return true;
		} catch (JMSException e) {
			if (e instanceof IllegalStateException) {
				this.destory();
				for (int i = 1; i < 100; i++) {
					logger.iLog("MQ连接状态异常, 重连第" + i + "次.");
					try {
						connect2MQ();
						// 发送的数据
						ObjectMessage msg = this.session
						.createObjectMessage((Serializable) obj);

						producer.send(msg);
						break;
					} catch (Exception e1) {
						logger.eLog("重连失败!", e);
					}
				}
			}
			return false;
		}
	}

	public void destory() {
		try {
			this.producer.close();
		} catch (Exception ex) {
			logger.iLog("closed producer exception :" + ex.getMessage(), ex);
		}
		try {

			this.session.close();
		} catch (Exception ex) {
			logger.iLog("closed session exception :" + ex.getMessage(), ex);
		}
		try {
			this.topicConn.close();
		} catch (Exception ex) {
			logger.iLog("closed connection exception :" + ex.getMessage(), ex);
		}

	}
}
