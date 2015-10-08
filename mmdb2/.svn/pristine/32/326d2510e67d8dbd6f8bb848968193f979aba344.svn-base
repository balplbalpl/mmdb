package com.mmdb.service.db.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.storage.DataBaseConfigStorage;
import com.mmdb.service.db.IDataBaseConfigService;

/**
 * db数据库配置 服务接口类
 * 
 * @author XIE
 */
@Component("dbConfigService")
public class DataBaseConfigServiceImpl extends AbstractDomain implements
		IDataBaseConfigService {
	@Autowired
	private DataBaseConfigStorage dbConfigStorage;

	@Override
	public List<DataBaseConfig> getAll() throws Exception {
		return dbConfigStorage.getAll();
	}

	@Override
	public DataBaseConfig getById(String dbId) throws Exception {
		return dbConfigStorage.getById(dbId);
	}

	@Override
	public DataBaseConfig getByName(String name) throws Exception {
		return dbConfigStorage.getByName(name);
	}

	@Override
	// //@Transaction
	public DataBaseConfig save(DataBaseConfig db) throws Exception {
		return dbConfigStorage.save(db);
	}

	@Override
	// @Transaction
	public void delete(DataBaseConfig db) throws Exception {
		dbConfigStorage.delete(db);
	}

	@Override
	// @Transaction
	public void deleteAll() throws Exception {
		dbConfigStorage.deleteAll();
	}

	@Override
	// @Transaction
	public DataBaseConfig update(DataBaseConfig db) throws Exception {
		return dbConfigStorage.update(db);
	}
	@Override
	public List<DataBaseConfig> getByAuthor(String username) throws Exception {
		return dbConfigStorage.getByProperty("author", username);
	}
}