package com.mmdb.service.subscription;

import java.util.List;
/**
 * 视图与用户的关系
 * @author xiongjian
 *
 */
public interface ISubscriptionViewPortfolioService {
	/**
	 * 保存用户和组合视图的关系
	 * 
	 * @param loginName
	 * @param viewPId
	 */
	void save(String viewPId, String loginName) throws Exception;

	/**
	 * 删除用户和组合视图的关系
	 * 
	 * @param loginName
	 * @param viewPId
	 */
	void del(String viewPId, String loginName) throws Exception;

	List<String> getSubscribersByViewPortfolioId(String viewPId)
			throws Exception;

	List<String> getViewPortfoliosBySubscriber(String sub) throws Exception;

	void delForCloseView(String viewId, String loginName) throws Exception;
}
