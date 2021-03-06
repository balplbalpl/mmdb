package com.mmdb.service.mapping;

import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToRelationMapping;

/**
 * 外部关系映射 Created by XIE on 2015/3/31.
 */
public interface ISourceRelationMapService {
	/**
	 * 获取所有关系映射
	 * 
	 * @return
	 * @throws Exception
	 */
	List<SourceToRelationMapping> getAll() throws Exception;
	
	/**
	 * 获取某个用户下的关系映射
	 * 
	 * @param username 用户名
	 * @return
	 * @throws Exception
	 */
	List<SourceToRelationMapping> getByAuthor(String username) throws Exception;

	
	/**
	 * 通过名称获取关系映射
	 * 
	 * @param id
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	SourceToRelationMapping getById(String id) throws Exception;

	public SourceToRelationMapping getByName(String name) throws Exception;

	/**
	 * 获取关系映射中使用的DB配置IDS
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getSourceIdByUsed() throws Exception;

	/**
	 * 保存关系映射
	 * 
	 * @param srm
	 *            关系映射
	 * @throws Exception
	 */

	SourceToRelationMapping save(SourceToRelationMapping srm) throws Exception;

	/**
	 * [批量]关系映射
	 * 
	 * @param srms
	 *            关系映射
	 * @throws Exception
	 */

	void save(List<SourceToRelationMapping> srms) throws Exception;

	/**
	 * 删除关系映射
	 * 
	 * @param srm
	 *            关系映射
	 * @throws Exception
	 */

	void delete(SourceToRelationMapping srm) throws Exception;

	/**
	 * 清除关系映射(数据初始化使用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 更新关系映射
	 * 
	 * @param srm
	 *            关系映射
	 * @throws Exception
	 */

	SourceToRelationMapping update(SourceToRelationMapping srm)
			throws Exception;

	/**
	 * 执行关系映射，处理数据间的关系
	 * 
	 * @param srm
	 *            关系映射
	 * @return
	 * @throws Exception
	 */

	Map<String, Integer> runNow(String srm, Map<String, CiCategory> ciCateMap,
			Map<String, RelCategory> relCateMap) throws Exception;

	/**
	 * 获取关系映射中使用的DB配置ID
	 * 
	 * @return
	 * @throws Exception
	 */
	List<Long> getDataSourceIds() throws Exception;

	/**
	 * 获取关系映射中使用的REL分类ID
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getRelCateIds() throws Exception;

	/**
	 * 获取指定REL分类的关系映射
	 * 
	 * @param rc
	 *            关系分类
	 * @return
	 * @throws Exception
	 */
	List<SourceToRelationMapping> getByRelCate(RelCategory rc) throws Exception;

	/**
	 * 获取包含CI分类的关系映射
	 * 
	 * @param category
	 *            CI分类
	 * @return
	 * @throws Exception
	 */
	List<SourceToRelationMapping> getMapsByCate(CiCategory category)
			throws Exception;

	/**
	 * 获取包含POOL的关系映射
	 * 
	 * @param pool
	 * @return
	 * @throws Exception
	 */
	List<SourceToRelationMapping> getMappingBySourcePool(DataSourcePool pool)
			throws Exception;

	/**
	 * 是否存在
	 * 
	 * @throws Exception
	 */
	public boolean exist(String id);

}
