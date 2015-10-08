package com.mmdb.service.performance;

import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.PerformanceViewBean;

public interface IPerformanceViewService {
	
	/**
	 * 保存性能视图
	 * @param view PerformanceViewBean性能视图对象
	 * @return
	 */
	public boolean savePerformanceView(PerformanceViewBean view);
	
	/**
	 * 更新性能视图
	 * @param view PerformanceViewBean性能视图对象
	 * @return
	 */
	public boolean updatePerformanceView(PerformanceViewBean view);
	
	/**
	 * 删除性能视图
	 * @param user  用户名
	 * @param viewName 视图名
	 * @return
	 */
	public boolean deletePerformanceView(String user,String viewName);
	
	/**
	 * 获取用户下的性能视图（分页）
	 * @param user 用户名
	 * @param start 
	 * @param limit
	 * @return
	 */
	public Map<Integer,List<PerformanceViewBean>> getViewByUser(String user,int start,int limit);
	
	/**
	 * 获取性能视图
	 * @param user 用户名
	 * @param viewName 视图名
	 * @return
	 */
	public PerformanceViewBean getViewByUserAndName(String user,String viewName);
}
