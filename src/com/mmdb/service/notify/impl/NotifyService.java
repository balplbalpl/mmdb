package com.mmdb.service.notify.impl;

import java.util.Map;

import javax.jms.JMSException;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.service.mq.EPTopicSender;
import com.mmdb.service.mq.MQTopicSender;
import com.mmdb.service.notify.INotifyService;

@Component("notifyService")
public class NotifyService implements INotifyService{
	private Log logger = LogFactory.getLogger("NotifyService");
	
	/**
	 * 更新TP(阈值管理)中的缓存信息
	 * 
	 * 
	 * @param tableName	 
	 * 			需要更新的数据库表名称
	 * @param action
	 * 			要进行的操作:[添加:ADD，修改:UPD，删除:DEL]
	 * @param key
	 * 			缓存的主键:[匹配规则和性能映射主键为id,视图的主键为:viewId^_^ciId^_^kpiId]
	 * @param map
	 * 			要更新的对象转换成MAP
	 */
	@Override
	public void refreshCache(String tableName,String action,
				String key,Map<String,Object> map) throws JMSException{
		try{
			//发送更新缓存的信息到MQ
			JSONObject sendObj = new JSONObject();
			sendObj.put("tableName", tableName);
			sendObj.put("action", action);
			sendObj.put("key", key);
			sendObj.put("map", map);
			//调用MQ Topic发送接口
			MQTopicSender tSender = new MQTopicSender();
			tSender.send(sendObj.toString());
			tSender.destory();
			logger.iLog("更新TP中的缓存信息："+tableName);
		} catch (Exception e) {
			logger.eLog("发送刷新缓存消息时出现异常,原因:" + e.getMessage(), e);
			throw new JMSException("发送刷新缓存消息时出现异常,原因:" + e.getMessage());
		}
	}
	
	/**
	 * 更新TP(阈值管理)中的缓存信息
	 * 
	 * 
	 * @param tableName	 
	 * 			需要更新的数据库表名称
	 * @param action
	 * 			要进行的操作:[添加:ADD，修改:UPD，删除:DEL]
	 * @param key
	 * 			缓存的主键:[匹配规则和性能映射主键为id,视图的主键为:viewId^_^ciId^_^kpiId]
	 * @param map
	 * 			要更新的对象转换成MAP
	 */
	@Override
	public void refreshEpCache(String tableName,String action,
				String key,Map<String,Object> map) throws JMSException{
		try{
			//发送更新缓存的信息到MQ
			JSONObject sendObj = new JSONObject();
			sendObj.put("tableName", tableName);
			sendObj.put("action", action);
			sendObj.put("key", key);
			sendObj.put("map", map);
			//调用MQ Topic发送接口
			EPTopicSender tSender = new EPTopicSender();
			tSender.send(sendObj.toString());
			tSender.destory();
			logger.iLog("更新EP中的缓存信息："+tableName);
		} catch (Exception e) {
			logger.eLog("发送刷新缓存消息时出现异常,原因:" + e.getMessage(), e);
			throw new JMSException("发送刷新缓存消息时出现异常,原因:" + e.getMessage());
		}
	}

}
