package com.mmdb.service.mapping.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.storage.SourceToCategoryStorage;
import com.mmdb.service.mapping.ISourceCategoryMapService;
import com.mmdb.service.sync.SourceCategoryMapSync;

/**
 * 数据库对象映射 Created by XIE on 2015/3/31.
 */
@Component("sourceCateMapService")
public class SourceCategoryMapServiceImpl extends AbstractDomain implements
		ISourceCategoryMapService {
	@Autowired
	private SourceToCategoryStorage sourceToCategoryStorage;
	@Autowired
	private SourceCategoryMapSync sourceCategoryMapSync;

	@Override
	public List<SourceToCategoryMapping> getAll() throws Exception {
		return sourceToCategoryStorage.getAll();
	}

	@Override
	public SourceToCategoryMapping getById(String id) throws Exception {
		return sourceToCategoryStorage.getById(id);
	}

	@Override
	public List<String> getSourceIdByUsed() throws Exception {
		List<String> sources = new ArrayList<String>();
		List<SourceToCategoryMapping> dbms = sourceToCategoryStorage.getAll();
		for (SourceToCategoryMapping dbm : dbms) {
			String id = dbm.getDataSource().getId();
			if (!sources.contains(id)) {
				sources.add(id);
			}
		}
		return sources;
	}
	
	@Override
	public List<SourceToCategoryMapping> getByAuthor(String username)
			throws Exception {
		return sourceToCategoryStorage.getByProperty("author", username);
	}
	
	@Override
	public SourceToCategoryMapping save(SourceToCategoryMapping dcm)
			throws Exception {
		return sourceToCategoryStorage.save(dcm);
	}

	@Override
	public void save(List<SourceToCategoryMapping> dbms) throws Exception {
		for (SourceToCategoryMapping dbm : dbms) {
			sourceToCategoryStorage.save(dbm);
		}
	}

	@Override
	public void delete(SourceToCategoryMapping dcm) throws Exception {
		sourceToCategoryStorage.delete(dcm);
	}

	@Override
	public void deleteAll() throws Exception {
		sourceToCategoryStorage.deleteAll();
	}

	@Override
	public SourceToCategoryMapping update(SourceToCategoryMapping dcm)
			throws Exception {
		return sourceToCategoryStorage.update(dcm);
	}

	@Override
	public Map<String, Integer> runNow(String dcm,
			Map<String, CiCategory> ciCateMap) throws Exception {
		return sourceCategoryMapSync.run(dcm, ciCateMap);
	}

	@Override
	public List<Long> getDataSourceIds() throws Exception {
		List<Long> ids = new ArrayList<Long>();
		List<SourceToCategoryMapping> dcm = sourceToCategoryStorage.getAll();
		for (SourceToCategoryMapping m : dcm) {
			// Long nid = m.getDataSource().getNeo4jid();
			// if (!ids.contains(nid)) {
			// ids.add(nid);
			// }
		}
		return ids;
	}

	@Override
	public List<SourceToCategoryMapping> getMappingByCategory(
			CiCategory category) throws Exception {
		List<SourceToCategoryMapping> ms = new ArrayList<SourceToCategoryMapping>();
		String id = category.getId();
		List<SourceToCategoryMapping> dcm = sourceToCategoryStorage.getAll();
		for (SourceToCategoryMapping m : dcm) {
			String cid = m.getCateId();
			if (cid.equals(id)) {
				if (!ms.contains(m)) {
					ms.add(m);
				}
			}
		}
		return ms;
	}

	@Override
	public List<SourceToCategoryMapping> getMappingBySourcePool(
			DataSourcePool pool) throws Exception {
		List<SourceToCategoryMapping> ms = new ArrayList<SourceToCategoryMapping>();
		String id = pool.getId();
		List<SourceToCategoryMapping> dcm = sourceToCategoryStorage.getAll();
		for (SourceToCategoryMapping m : dcm) {
			String cid = m.getDataSource().getId();
			if (cid.equals(id)) {
				if (!ms.contains(m)) {
					ms.add(m);
				}
			}
		}
		return ms;
	}

	@Override
	public boolean exist(String id) {
		return sourceToCategoryStorage.exist(id);
	}

	@Override
	public SourceToCategoryMapping getByName(String name) throws Exception {
		return sourceToCategoryStorage.getByName(name);
	}
}
