package com.mmdb.service.subscription;

import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.Page;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.ViewInformation;

/**
 * 
 * @author xiongjian
 * 
 */
public interface ISubscriptionService {

	/**
	 * 订阅一个视图,并取默认的阈值配置
	 * 
	 * @param username
	 * @param viewId
	 */
	public void save(String username, String viewId) throws Exception;

	/**
	 * 订阅一个视图,并取默认的阈值配置
	 * 
	 * @param username
	 * @param viewId
	 */
	public void save(String username, ViewInformation info) throws Exception;

	/**
	 * 全量更新订阅
	 * 
	 * @param username
	 *            订阅人
	 * @param viewId
	 *            视图id
	 * @param ciKpis
	 *            key 为ci的HexId,value是对应的kpis
	 */
	public void update(String username, String viewId, String viewAuthor,
			List<Map<String, Object>> unfoldCiKpis) throws Exception;

	public void asSave(String username,String viewId, List<Map<String, Object>> unfoldCiKpis)
			throws Exception;

	/**
	 * 增量更新
	 * 
	 * @param username
	 *            订阅人
	 * @param viewId
	 *            视图id
	 * @param ciKpis
	 *            key 为ci的HexId,value是对应的kpis
	 */
	public void incUpdate(String username, String viewId, String viewAuthor,
			List<Map<String, Object>> unfoldCiKpis) throws Exception;

	/**
	 * 视图中添加一组
	 * 
	 * @param viewId
	 * @param ciHex
	 * @throws Exception
	 */
	public void addCiByView(ViewInformation view, List<String> ciHex)
			throws Exception;

	public void delCIByView(String viewId, List<String> ciHex) throws Exception;

	/**
	 * 当前用户取消订阅
	 * 
	 * @param viewId
	 *            视图的id
	 * @param userName
	 *            用户的名称
	 */
	public void delete(String username, String viewId) throws Exception;

	/**
	 * 删除这张视图的全部订阅
	 * 
	 * @param viewId
	 *            视图的id
	 * @param viewDeleted
	 *            true时代表视图本身被删除掉了,连着作者的订阅一起删掉. false适用于作者将视图变为私有视图其他用户无法订阅.
	 */
	public void delete(String viewId, boolean viewDeleted) throws Exception;

	/**
	 * 一个kpi被删除了
	 * 
	 * @param kpiId
	 * @throws Exception
	 */
	public void deleteByKpi(String kpiId) throws Exception;

	/**
	 * 刪除一个ci与kpi的关联
	 * 
	 * @param ciId
	 * @param kpiId
	 * @throws Exception
	 */
	public void deleteByCiKpiRel(String ciId, String kpiId) throws Exception;

	/**
	 * ci被删除,将相关的ci->kpi关系全删除
	 * 
	 * @param ciId
	 * @throws Exception
	 */
	public void deleteByCi(String ciId) throws Exception;

	/**
	 * 增加一个ci与kpi的关联,
	 * 
	 * @param kpi
	 * @throws Exception
	 */
	public void addByCiKpiRel(String ciId, KpiInformation kpi) throws Exception;

	//
	/**
	 * 该用户订阅的全部视图
	 * 
	 * @param Subscriber
	 *            订阅人
	 * @return
	 * @throws Exception
	 */
	public List<String> getViewBySubscriber(String subscriber) throws Exception;

	/**
	 * 订阅这个视图的 人
	 * 
	 * @param viewId
	 *            视图id
	 * @return
	 * @throws Exception
	 */
	public List<String> getSubscriberByView(String viewId) throws Exception;

	/**
	 * 通过视图获取对应的kpi(阈值)
	 * 
	 * @param viewId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getThresholdByView(String viewId,
			String userName) throws Exception;

	public Page<Map<String, Object>> getThresholdByView(String viewId,
			String userName, int page, int pageSize) throws Exception;

}
