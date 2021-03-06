package com.mmdb.service.db.impl;

import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.database.storage.DataSourceStorage;
import com.mmdb.service.db.IDataSourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * db数据库配置 服务接口类 Created by XIE on 2015/3/30.
 */
@Component("dataSourceService")
public class DataSourceServiceImpl extends AbstractDomain implements
		IDataSourceService {
	@Autowired
	private DataSourceStorage dataSourceStorage;

	@Override
	public List<DataSourcePool> getAll() throws Exception {
		return dataSourceStorage.getAll();
	}

	@Override
	public DataSourcePool getById(String id) throws Exception {
		return dataSourceStorage.getById(id);
	}

	@Override
	public DataSourcePool getByName(String name) throws Exception {
		return dataSourceStorage.getByName(name);
	}

	@Override
	public DataSourcePool save(DataSourcePool dp) throws Exception {
		return dataSourceStorage.save(dp);
	}

	@Override
	public DataSourcePool update(DataSourcePool dp) throws Exception {
		return dataSourceStorage.update(dp);
	}

	@Override
	public void delete(DataSourcePool db) throws Exception {
		dataSourceStorage.delete(db);
	}

	@Override
	public void deleteAll() throws Exception {
		dataSourceStorage.deleteAll();
	}

	@Override
	public Set<String> getDataConfigIds() throws Exception {
		Set<String> ids = new HashSet<String>();
		List<DataSourcePool> dss = dataSourceStorage.getAll();
		for (DataSourcePool ds : dss) {
			ids.add(ds.getDatabaseConfigId());
		}
		return ids;
	}
	
	@Override
	public List<DataSourcePool> getByAuthor(String username) throws Exception {
		return dataSourceStorage.getByProperty("author", username);
	}
}
