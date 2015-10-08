package com.mmdb.service.mq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import net.sf.json.JSONArray;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;
import org.neo4j.shell.util.json.JSONObject;

public class SenderPerfService {
	public  Logger logger = Logger.getLogger(SenderPerfService.class);
	public  String amqUsername ;
	public  String amqPassword ;
	public  String queueURI ;
	public  String queueName ;
	public  Connection connection ;//连接
	public  Session senderSession ;//会话
	public  MessageProducer producer ;//消息生产者
	public  ArrayList<String> missMessage ;//发送失败消息备份
	
	public SenderPerfService(){
		init();
	}
	
	public void init(){
		try{
			//System.out.println("@@@@@@@@@@@@@@@@@@@@@@#############init##### ");
			loadConfig();
			missMessage = new ArrayList<String>();
			senderSession = getSession();
			connection = initConection();
			producer = getProducer();
		}catch (JMSException e) {
			logger.error("初始化时出现异常,原因："+e.getMessage(),e);
		}
	}
	
	public Connection getConection() throws JMSException{
		if(connection != null)
			return connection;
		return initConection();
		
	}
	
	public Connection initConection() throws JMSException{
/*		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(//JMS连接工厂
				getAmqUsername(), getAmqPassword(), getQueueURI());*/
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(getQueueURI());
		connection = connectionFactory.createConnection();// 连接对象
		connection.start();// 启动
		return connection;
	}
	
	public void closeConection(){
		try {
			if (null != connection)
				connection.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * 获得无事务会话
	 */
	public Session getSession() throws JMSException{
		if(senderSession == null){
			senderSession = getConection().createSession(Boolean.FALSE,Session.AUTO_ACKNOWLEDGE);
		}	
		return senderSession;
	}
	
	/**
	 * 建立生产者
	 */
    public MessageProducer getProducer() throws JMSException{
    	if(producer == null){
    		Session session = getSession();
        	Destination destination = session.createQueue(getQueueName());
    		producer = session.createProducer(destination);//消息生产者
    		producer.setDeliveryMode(DeliveryMode.PERSISTENT);//消息持久化
		}
		return producer;
	}
    
    /**
	 * 向serviceQueue发送一个服务消息,用于外呼服务接口
	 */
	public int sendMessage(Serializable dataObject) {
		try{
			ObjectMessage message = getSession().createObjectMessage(dataObject);
			getProducer().send(message);
			logger.info("向serviceQueue发送1个消息!");
		}catch (Exception e){
			connection = null;
			senderSession = null;
			producer = null;
			return -1;
		}
		return 1;
	}
	

	
    /**
	 * 向发送一个事件消息,供事件源使用
	 */
	public int sendJsonMessage(JSONObject perfJson ) {
		try{
			TextMessage message = getSession().createTextMessage();
			
			message.setText(perfJson.toString());
			getProducer().send(message);
		}catch (Exception e){
			connection = null;
			senderSession = null;
			producer = null;
			return -1;
		}
		return 1;
	}
	

	
    /**
	 * 发送数组类型的数
	 */
	public int sendJSONArrayMessage(JSONArray perfDatas) {
		try{
			ObjectMessage message = getSession().createObjectMessage();
			
			message.setObject(perfDatas);
			getProducer().send(message);
		}catch (Exception e){
			logger.error("发送消息产生异常",e);
			connection = null;
			senderSession = null;
			producer = null;
			return -1;
		}
		return 1;
	}


	//加载配置文件到内存
	public void loadConfig() {
		try {
			ResourceBundle init = ResourceBundle
							.getBundle("config.demo.demo-global");
			
/*			setAmqUsername(init.getString("amq.username"));
			setAmqPassword( init.getString("amq.password"));*/
			setQueueURI(init.getString("amq.url"));
			setQueueName(init.getString("amq.queue.mmdbMapQueue"));
			
		} catch (Exception e) {
			logger.error("加载MQ配置时发生错误",e);
		}
	}

	public  String getAmqUsername() {
		return amqUsername;
	}

	public  void setAmqUsername(String amqUsername) {
		this.amqUsername = amqUsername;
	}

	public  String getAmqPassword() {
		return amqPassword;
	}

	public  void setAmqPassword(String amqPassword) {
		this.amqPassword = amqPassword;
	}

	public  String getQueueURI() {
		return queueURI;
	}

	public  void setQueueURI(String queueURI) {
		this.queueURI = queueURI;
	}

	public  String getQueueName() {
		return queueName;
	}

	public  void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
}
