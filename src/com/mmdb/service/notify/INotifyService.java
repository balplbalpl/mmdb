package com.mmdb.service.notify;

import java.util.Map;

public interface INotifyService {
	
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
	public void refreshCache(String tableName,String action,
				String key,Map<String,Object> map) throws Exception;


	/**
	 * 更新EP(事件管理)中的缓存信息
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
	public void refreshEpCache(String tableName,String action,
				String key,Map<String,Object> map) throws Exception;
}
