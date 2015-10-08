package com.mmdb.service.relation;

import java.util.List;
import java.util.Map;

/**
 *	视图与组合视图的关系
 * 
 * @author xj
 */
public interface IViewViewProtfolioRelService {

	public void save(String protfolioId, List<String> viewIds) throws Exception;

	public void update(String id, List<String> viewIds) throws Exception;

	public Map<String, List<String>> getAll() throws Exception;
	/**
	 * 通過viewId获取相关的组合视图id
	 * @param hexId
	 * @return
	 * @throws Exception
	 */
	public List<String> getProtfolioViewIdByView(String viewId) throws Exception;
	
	/**
	 * 通过组合视图id获取相关的viewId
	 * @param protfolioId
	 * @return
	 * @throws Exception
	 */
	public List<String> getViewIdByProtfolio(String protfolioId) throws Exception;
	
	
	public void deleteByProtfolio(String protfolioId) throws Exception;

	/**
	 * 刪除对应视图那列值
	 * @param hexId
	 * @throws Exception
	 */
	public void deleteByView(String viewId) throws Exception;
	/**
	 * 删除全部
	 * @throws Exception
	 */
	public void deleteAll() throws Exception;

}
