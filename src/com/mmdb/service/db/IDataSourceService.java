package com.mmdb.service.db;

import java.util.List;
import java.util.Set;

import com.mmdb.model.database.bean.DataSourcePool;

/**
 * Created by XIE on 2015/3/30.
 */
public interface IDataSourceService {
	/**
	 * 获取所有的db配置
	 * 
	 * @return
	 * @throws Exception
	 */
	List<DataSourcePool> getAll() throws Exception;

	/**
	 * 获取单个db配置
	 * 
	 * @param dbId
	 *            dbID
	 * @return
	 * @throws Exception
	 */
	DataSourcePool getById(String dbId) throws Exception;

	/**
	 * 获取单个db配置
	 * 
	 * @param dbId
	 *            dbID
	 * @return
	 * @throws Exception
	 */
	DataSourcePool getByName(String name) throws Exception;
	
	List<DataSourcePool> getByAuthor(String username) throws Exception;

	/**
	 * 保存db配置
	 * 
	 * @param db
	 *            db配置
	 * @throws Exception
	 */

	DataSourcePool save(DataSourcePool db) throws Exception;

	/**
	 * 更新db配置
	 * 
	 * @param db
	 *            db配置
	 * @throws Exception
	 */

	DataSourcePool update(DataSourcePool db) throws Exception;
	
	/**
	 * 删除db配置
	 * 
	 * @param db
	 *            db配置
	 * @throws Exception
	 */

	void delete(DataSourcePool db) throws Exception;

	/**
	 * 删除所有db配置(数据初始化使用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 获取已使用的数据库连接
	 * 
	 * @return
	 */
	Set<String> getDataConfigIds() throws Exception;
}
