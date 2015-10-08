package com.mmdb.service.mq;


import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;


public class SendToMQ {
	
	public SendToMQ(){
		
	}

	public void sendMessage(String param,String mqurl,String queuename){
		Logger log = Logger.getLogger(this.getClass());
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		String user = ActiveMQConnection.DEFAULT_USER;
		String pwd = ActiveMQConnection.DEFAULT_PASSWORD;
		
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, pwd, mqurl);
		try{
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(queuename);
			producer = session.createProducer(destination);
			connection.start();
			TextMessage message = session.createTextMessage(param);
			producer.send(message);
			System.out.println(message);
		}catch(Exception e){
			e.printStackTrace();
			log.error(e.getMessage(), e);
		}finally{
			if(null != producer){
				try {
					producer.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
			if(null != producer){
				try {
					connection.close();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
