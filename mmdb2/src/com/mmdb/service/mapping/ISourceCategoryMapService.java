package com.mmdb.service.mapping;

import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToCategoryMapping;

/**
 * 数据数据映射 Created by XIE on 2015/3/30.
 */
public interface ISourceCategoryMapService {
	/**
	 * 获取所有db映射
	 * 
	 * @return
	 * @throws Exception
	 */
	List<SourceToCategoryMapping> getAll() throws Exception;

	List<SourceToCategoryMapping> getByAuthor(String username) throws Exception;
	
	/**
	 * 通过映射名称获取db映射
	 * 
	 * @param id
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	SourceToCategoryMapping getById(String id) throws Exception;

	/**
	 * 获取已映射的db配置id
	 * 
	 * @return
	 * @throws Exception
	 */
	List<String> getSourceIdByUsed() throws Exception;

	/**
	 * 保存db映射
	 * 
	 * @param dcm
	 *            数据库映射
	 * @throws Exception
	 */

	SourceToCategoryMapping save(SourceToCategoryMapping dcm) throws Exception;

	/**
	 * [批量]保存db映射
	 * 
	 * @param dbms
	 *            数据库映射
	 * @throws Exception
	 */

	void save(List<SourceToCategoryMapping> dbms) throws Exception;

	/**
	 * 删除db映射
	 * 
	 * @param scm
	 *            数据库映射
	 * @throws Exception
	 */

	void delete(SourceToCategoryMapping scm) throws Exception;

	/**
	 * 删除分类映射(数据初始化使用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 更新分类映射
	 * 
	 * @param scm
	 *            数据库映射
	 * @throws Exception
	 */

	SourceToCategoryMapping update(SourceToCategoryMapping scm)
			throws Exception;

	/**
	 * 执行映射，处理数据间的关系
	 * 
	 * @param scm
	 *            数据库映射
	 * @return
	 * @throws Exception
	 */
	Map<String, Integer> runNow(String scm, Map<String, CiCategory> ciCateMap)
			throws Exception;

	/**
	 * 获取数据配置id
	 * 
	 * @return
	 * @throws Exception
	 */
	List<Long> getDataSourceIds() throws Exception;

	/**
	 * 获取包含CI分类的映射
	 * 
	 * @param category
	 *            CI分类
	 * @return
	 * @throws Exception
	 */
	List<SourceToCategoryMapping> getMappingByCategory(CiCategory category)
			throws Exception;

	/**
	 * 获取包含CI分类的映射
	 * 
	 * @param pool
	 * @return
	 * @throws Exception
	 */
	List<SourceToCategoryMapping> getMappingBySourcePool(DataSourcePool pool)
			throws Exception;

	/**
	 * 是否存在
	 * 
	 * @throws Exception
	 */
	public boolean exist(String id);

	SourceToCategoryMapping getByName(String name) throws Exception;
}