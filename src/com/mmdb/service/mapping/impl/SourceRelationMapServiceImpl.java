package com.mmdb.service.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.framework.neo4j.proxy.AbstractDomain;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.categroy.storage.RelCategoryStorage;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.database.storage.DataSourceStorage;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.mapping.storage.SourceToRelationStorage;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.sync.SourceRelationMapSync;

/**
 * 数据库关系映射 Created by XIE on 2015/3/31.
 */
@Component("sourceRelMapService")
public class SourceRelationMapServiceImpl extends AbstractDomain implements
		ISourceRelationMapService {
	@Autowired
	private SourceToRelationStorage sourceToRelationStorage;
	@Autowired
	private SourceRelationMapSync sourceRelationMapSync;
	@Autowired
	private DataSourceStorage dsStorage;
	@Autowired
	private RelCategoryStorage relCategoryStorage;

	@Override
	public List<SourceToRelationMapping> getAll() throws Exception {
		return sourceToRelationStorage.getAll();
	}

	@Override
	public SourceToRelationMapping getById(String id) throws Exception {
		return sourceToRelationStorage.getById(id);
	}

	@Override
	public SourceToRelationMapping getByName(String name) throws Exception {
		return sourceToRelationStorage.getByName(name);
	}

	@Override
	public List<String> getSourceIdByUsed() throws Exception {
		List<String> ids = new ArrayList<String>();
		List<SourceToRelationMapping> dbms = sourceToRelationStorage.getAll();
		for (SourceToRelationMapping dbm : dbms) {
			String id = dbm.getDataSource().getId();
			if (!ids.contains(id)) {
				ids.add(id);
			}
		}
		return ids;
	}
	
	@Override
	public List<SourceToRelationMapping> getByAuthor(String username)
			throws Exception {
		return sourceToRelationStorage.getByProperty("owner", username);
	}
	
	@Override
	//
	public SourceToRelationMapping save(SourceToRelationMapping srm)
			throws Exception {
		return sourceToRelationStorage.save(srm);
	}

	@Override
	//
	public void save(List<SourceToRelationMapping> ms) throws Exception {
		for (SourceToRelationMapping m : ms) {
			sourceToRelationStorage.save(m);
		}
	}

	@Override
	//
	public void delete(SourceToRelationMapping srm) throws Exception {
		sourceToRelationStorage.delete(srm);
	}

	@Override
	//
	public void deleteAll() throws Exception {
		sourceToRelationStorage.deleteAll();
	}

	@Override
	//
	public SourceToRelationMapping update(SourceToRelationMapping srm)
			throws Exception {
		return sourceToRelationStorage.update(srm);
	}

	@Override
	public Map<String, Integer> runNow(String srm,
			Map<String, CiCategory> ciCateMap,
			Map<String, RelCategory> relCateMap) throws Exception {
		return sourceRelationMapSync.run(srm, ciCateMap, relCateMap);
	}

	@Override
	public List<Long> getDataSourceIds() throws Exception {
		List<Long> retList = new ArrayList<Long>();
		List<SourceToRelationMapping> cMaps = sourceToRelationStorage.getAll();
		for (SourceToRelationMapping cMap : cMaps) {
			// Long nid = cMap.getDataSource().getNeo4jid();
			// if (!retList.contains(nid)) {
			// retList.add(nid);
			// }
		}
		return retList;
	}

	@Override
	public List<String> getRelCateIds() throws Exception {
		List<String> retList = new ArrayList<String>();
		List<RelCategory> all = relCategoryStorage.getAll();
		Map<String, RelCategory> cache = new HashMap<String, RelCategory>();
		for (RelCategory relCate : all) {
			cache.put(relCate.getId(), relCate);
		}
		all = null;
		List<SourceToRelationMapping> cMaps = sourceToRelationStorage.getAll();
		for (SourceToRelationMapping cMap : cMaps) {
			RelCategory relCategory = cache.get(cMap.getRelCateId());
			if (relCategory != null) {
				String nid = relCategory.getName();
				if (!retList.contains(nid)) {
					retList.add(nid);
				}
			}
		}
		return retList;
	}

	@Override
	public List<SourceToRelationMapping> getByRelCate(RelCategory rc)
			throws Exception {
		List<SourceToRelationMapping> oms = new ArrayList<SourceToRelationMapping>();
		List<SourceToRelationMapping> cMaps = sourceToRelationStorage.getAll();
		// List<DataSourcePool> all = dsStorage.getAll();
		// Map<String, DataSourcePool> dsc = new HashMap<String,
		// DataSourcePool>();
		// for (DataSourcePool dataSourcePool : all) {
		// dsc.put(dataSourcePool.getId(), dataSourcePool);
		// }
		List<RelCategory> all = relCategoryStorage.getAll();
		Map<String, RelCategory> dsc = new HashMap<String, RelCategory>();
		for (RelCategory rct : all) {
			dsc.put(rct.getId(), rct);
		}
		String rl = rc.getName();
		for (SourceToRelationMapping cMap : cMaps) {
			String nid = dsc.get(cMap.getRelCateId()).getName();
			if (rl.equals(nid)) {
				oms.add(cMap);
			}
		}
		return oms;
	}

	@Override
	public List<SourceToRelationMapping> getMapsByCate(CiCategory category)
			throws Exception {
		List<SourceToRelationMapping> retList = new ArrayList<SourceToRelationMapping>();
		String id = category.getId();
		List<SourceToRelationMapping> cMaps = sourceToRelationStorage.getAll();
		for (SourceToRelationMapping cMap : cMaps) {
			String sid = cMap.getSourceCateId();
			String eid = cMap.getTargetCateId();
			if (sid != null && eid != null) {
				if (sid.equals(id) || eid.equals(id)) {
					if (!retList.contains(cMap)) {
						retList.add(cMap);
					}
				}
			}
		}
		return retList;
	}

	@Override
	public List<SourceToRelationMapping> getMappingBySourcePool(
			DataSourcePool pool) throws Exception {
		List<SourceToRelationMapping> ms = new ArrayList<SourceToRelationMapping>();
		String id = pool.getId();
		List<SourceToRelationMapping> dcm = sourceToRelationStorage.getAll();
		for (SourceToRelationMapping m : dcm) {
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
		return sourceToRelationStorage.exist(id);
	}
}
