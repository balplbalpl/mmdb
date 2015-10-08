package com.mmdb.service.db;

import java.util.List;

import com.mmdb.model.database.bean.DataBaseConfig;

public interface IDataBaseConfigService {
	/**
	 * 获取所有的db配置
	 * 
	 * @return
	 * @throws Exception
	 */
	List<DataBaseConfig> getAll() throws Exception;

	/**
	 * 获取单个db配置
	 * 
	 * @param id
	 *            dbid
	 * @return
	 * @throws Exception
	 */
	DataBaseConfig getById(String id) throws Exception;

	DataBaseConfig getByName(String name) throws Exception;
	
	List<DataBaseConfig> getByAuthor(String username) throws Exception;

	/**
	 * 保存db配置
	 * 
	 * @param dc
	 *            db配置
	 * @throws Exception
	 */

	DataBaseConfig save(DataBaseConfig dc) throws Exception;

	/**
	 * 删除db配置
	 * 
	 * @param dc
	 *            db配置
	 * @throws Exception
	 */

	void delete(DataBaseConfig dc) throws Exception;

	/**
	 * 删除所有db配置(数据初始化使用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 更新db配置
	 * 
	 * @param dc
	 *            db配置
	 * @throws Exception
	 */

	DataBaseConfig update(DataBaseConfig dc) throws Exception;
}
