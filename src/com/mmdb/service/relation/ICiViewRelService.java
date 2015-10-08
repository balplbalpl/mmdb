package com.mmdb.service.relation;

import java.util.List;
import java.util.Map;

/**
 * CI-VIEW 数据关系 - 服务层
 * 
 * @author XIE
 */
public interface ICiViewRelService {

	public void save(String viewId, List<String> ciHexIds) throws Exception;

	public void update(String id, List<String> ciHexIds) throws Exception;

	public Map<String, List<String>> getAll() throws Exception;
	/**
	 * 通過Ci的hexid获取相关的视图id
	 * @param hexId
	 * @return
	 * @throws Exception
	 */
	public List<String> getByCi(String hexId) throws Exception;
	
	/**
	 * 通过viewId获取相关的CIid
	 * @param viewId
	 * @return
	 * @throws Exception
	 */
	public List<String> getByView(String viewId) throws Exception;
	
	
	public void deleteByView(String viewId) throws Exception;

	/**
	 * 刪除对应ci那列值
	 * @param hexId
	 * @throws Exception
	 */
	public void deleteByCi(String hexId) throws Exception;
	/**
	 * 删除全部
	 * @throws Exception
	 */
	public void deleteAll() throws Exception;

}
