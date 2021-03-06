package com.mmdb.service.mapping;

import com.mmdb.model.mapping.PerfToDbMapping;

import java.util.List;
import java.util.Map;

/**
 * 数据集和性能数据映射接口
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-19
 */
public interface IPerfDbMapService {
	
	/**
	 * 获取所有映射
	 * 
	 * @return List<PerfToDbMapping>
	 * @throws Exception
	 */
	public List<PerfToDbMapping> getAll() throws Exception;

	/**
	 * 取到某个用户的所有映射
	 * @param username
	 * @return
	 * @throws Exception
	 */
	List<PerfToDbMapping> getByOwner(String username) throws Exception;
	
	/**
	 * 通过名称查询映射
	 * 
	 * @param name
	 *            映射名称
	 * @return List<PerfToDbMapping>
	 * @throws Exception
	 */
	public List<PerfToDbMapping> getByName(String name) throws Exception;
	
	/**
	 * 通过映射ID获取映射
	 * 
	 * @param id
	 *            映射ID
	 * @return PerfToDbMapping
	 * @throws Exception
	 */
	public PerfToDbMapping getMappingById(String id) throws Exception;


	/**
	 * 保存PerfToDbMapping映射
	 * 
	 * @param PerfToDbMapping
	 *            		映射对象
	 * @throws Exception
	 */
	public void save(PerfToDbMapping mapping) throws Exception;

	/**
	 * 删除映射
	 * 
	 * @param PerfToDbMapping
	 *            映射对象
	 * @throws Exception
	 */
	public void deleteById(String id) throws Exception;

	/**
	 * 更新映射
	 * 
	 * @param PerfToDbMapping
	 *            		映射对象
	 * @throws Exception
	 */
	public void update(PerfToDbMapping mapping) throws Exception;

	/**
	 * 执行映射，处理数据间的关系
	 * 
	 * @param mapping
	 *            映射对象
	 * @return
	 * @throws Exception
	 */
	public Map<String, Integer> runNow(PerfToDbMapping mapping) throws Exception;
	
	/**
	 * 预览映射匹配结果
	 * 
	 * @param mapping 映射对象
	 * @return Map<String, List<?>> 
	 * 				匹配到性能数据:matchedList 未匹配到数据:unMatchedList
	 * @throws Exception
	 */
    public Map<String, List<?>> preView(PerfToDbMapping mapping) throws Exception;
	

}