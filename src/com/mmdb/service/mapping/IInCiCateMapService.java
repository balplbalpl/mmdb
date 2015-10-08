package com.mmdb.service.mapping;

import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.InCiCateMap;

/**
 * 内部配置项分类映射 - 服务层
 * 
 * @author XIE
 * 
 */
public interface IInCiCateMapService {
	/**
	 * 获取所有配置项分类映射
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<InCiCateMap> getAll() throws Exception;

	/**
	 * 通过映射名称获取映射
	 * 
	 * @param uName
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public InCiCateMap getByName(String uName) throws Exception;

	public List<InCiCateMap> getByAuthor(String username) throws Exception;
	
	/**
	 * 保存分类映射
	 * 
	 * @param inCiCateMap
	 *            分类间的映射
	 * @throws Exception
	 */
	
	public InCiCateMap save(InCiCateMap inCiCateMap) throws Exception;

	/**
	 * [批量]保存分类映射
	 * 
	 * @param ims
	 *            分类映射
	 * @throws Exception
	 */
	
	public void save(List<InCiCateMap> ims) throws Exception;

	/**
	 * 删除分类映射
	 * 
	 * @param inCiCateMap
	 *            分类映射
	 * @throws Exception
	 */
	
	public void delete(InCiCateMap inCiCateMap) throws Exception;

	/**
	 * 删除分类映射(数据初始化使用)
	 * 
	 * @throws Exception
	 */
	
	public void deleteAll() throws Exception;

	/**
	 * 更新分类映射
	 * 
	 * @param inCiCateMap
	 *            分类映射
	 * @throws Exception
	 */
	
	public InCiCateMap update(InCiCateMap inCiCateMap) throws Exception;

	/**
	 * [不带事务]执行映射，批量处理数据间的关系
	 * 
	 * @param im
	 *            关系映射
	 * @param infos
	 *            关系的起点或终点数据数组
	 * @throws Exception
	 */
	public void runNow(InCiCateMap im, List<CiInformation> infos) throws Exception;

	/**
	 * 执行映射，处理数据间的关系
	 * 
	 * @param inCiCateMap
	 *            关系映射
	 * @return
	 * @throws Exception
	 */
	
	public Map<String, Long> runNow(String inCiCateMap, Map<String, CiCategory> ciCateMap, Map<String, RelCategory> relCateMao) throws Exception;

	/**
	 * 获取使用指定关系分类的映射
	 * 
	 * @param rc
	 *            关系分类
	 * @return
	 * @throws Exception
	 */
	public List<InCiCateMap> getByRelCate(RelCategory rc) throws Exception;

	/**
	 * 获取映射中存在的关系分类id
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getRelCateIds() throws Exception;

	/**
	 * 获取映射中存在的关系分类对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<RelCategory> getRelCates() throws Exception;

	/**
	 * 获取包含CI分类的映射
	 * 
	 * @param category
	 *            CI分类
	 * @return
	 * @throws Exception
	 */
	public List<InCiCateMap> getMapsByCate(CiCategory category) throws Exception;
	
	/**
	 * 是否存在
	 * @throws Exception
	 */
	public boolean exist(String id);
	/**
	 * 批量更新
	 * @param ims
	 */
	public void update(List<InCiCateMap> ims) throws Exception;

	public InCiCateMap getById(String id) throws Exception;
}
