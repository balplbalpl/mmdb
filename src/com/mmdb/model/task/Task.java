package com.mmdb.model.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.KpiSyncMapping;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.SourceToRelationMapping;

/**
 * 命名空间为[tasks]的定时器类<br>
 * 
 * @author XIE
 */
public class Task {

	/**
	 * 任务名称，不可重复
	 */
	private String name;
	/**
	 * 内部CI分类关系映射
	 */
	// @RelationTo(elementClass = InCiCateMap.class, type = "TASK-INREL")
	private List<InCiCateMap> inCiCateMap = new ArrayList<InCiCateMap>();

	private List<String> inCiCateMapIds = new ArrayList<String>();
	/**
	 * 外部CI分类关系映射
	 */
	// @RelationTo(elementClass = SourceToRelationMapping.class, type =
	// "TASK-OUTREL")
	private List<SourceToRelationMapping> outCiCateMap = new ArrayList<SourceToRelationMapping>();

	private List<String> outCiCateMapIds = new ArrayList<String>();

	private List<PerfToDbMapping> perfDbMap = new ArrayList<PerfToDbMapping>();

	private List<String> perfDbMapIds = new ArrayList<String>();

	/**
	 * DB表和CI分类映射
	 */
	// @RelationTo(elementClass = SourceToCategoryMapping.class, type =
	// "TASK-DATACATE")
	private List<SourceToCategoryMapping> dbCiCateMap = new ArrayList<SourceToCategoryMapping>();

	private List<String> dbCiCateMapIds = new ArrayList<String>();

	/**
	 * KPI同步映射
	 */
	private List<KpiSyncMapping> kpiSyncMap = new ArrayList<KpiSyncMapping>();

	private List<String> kpiSyncMapIds = new ArrayList<String>();
	/**
	 * 执行时间
	 */
	private Map<String, String> timing;
	/**
	 * 是否开启此任务
	 */
	private boolean open = true;

	/**
	 * 是否过期
	 */
	private boolean timeOut = true;

	/**
	 * neo4jid
	 * 
	 * */

	private String id;
	
	private String owner;

	public List<String> getInCiCateMapIds() {
		return inCiCateMapIds;
	}

	public void setInCiCateMapIds(List<String> inCiCateMapIds) {
		this.inCiCateMapIds = inCiCateMapIds;
	}

	public List<String> getOutCiCateMapIds() {
		return outCiCateMapIds;
	}

	public void setOutCiCateMapIds(List<String> outCiCateMapIds) {
		this.outCiCateMapIds = outCiCateMapIds;
	}

	public List<String> getDbCiCateMapIds() {
		return dbCiCateMapIds;
	}

	public void setDbCiCateMapIds(List<String> dbCiCateMapIds) {
		this.dbCiCateMapIds = dbCiCateMapIds;
	}

	public List<PerfToDbMapping> getPerfDbMap() {
		return perfDbMap;
	}

	public void setPerfDbMap(List<PerfToDbMapping> perfDbMap) {
		this.perfDbMap = perfDbMap;
	}

	public List<String> getPerfDbMapIds() {
		return perfDbMapIds;
	}

	public void setPerfDbMapIds(List<String> perfDbMapIds) {
		this.perfDbMapIds = perfDbMapIds;
	}

	public List<KpiSyncMapping> getKpiSyncMap() {
		return kpiSyncMap;
	}

	public void setKpiSyncMap(List<KpiSyncMapping> kpiSyncMap) {
		this.kpiSyncMap = kpiSyncMap;
	}

	public List<String> getKpiSyncMapIds() {
		return kpiSyncMapIds;
	}

	public void setKpiSyncMapIds(List<String> kpiSyncMapIds) {
		this.kpiSyncMapIds = kpiSyncMapIds;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * [构造函数]
	 */
	public Task() {

	}

	/**
	 * CI关系映射[构造函数]
	 * 
	 * @param name
	 *            任务名称
	 * @param timing
	 *            计时器
	 * @param dbms
	 *            CI数据映射
	 * @param cims
	 *            CI内部关系映射
	 * @param ocms
	 *            CI外部关系映射
	 * @throws Exception
	 */
	public Task(String name, Map<String, String> timing,
			List<SourceToCategoryMapping> dbms, List<InCiCateMap> cims,
			List<SourceToRelationMapping> ocms) throws Exception {
		if (dbms != null && dbms.size() != 0) {
			this.dbCiCateMap = dbms;
		}
		if (cims != null && cims.size() != 0) {
			this.inCiCateMap = cims;
		}
		if (ocms != null && ocms.size() != 0) {
			this.outCiCateMap = ocms;
		}
		this.name = name;
		this.timing = timing;
	}

	/**
	 * 获取任务名称
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 设置任务名称
	 * 
	 * @param name
	 *            名称
	 * @throws Exception
	 */
	public void setName(String name) throws Exception {
		if (name == null || "".equals(name)) {
			throw new Exception("任务名称不能为[" + name + "]");
		}
		this.name = name;
	}

	/**
	 * 获取CI分类内部关系映射
	 * 
	 * @return
	 */
	public List<InCiCateMap> getInCiCateMap() {
		return inCiCateMap;
	}

	/**
	 * 设置CI分类内部关系映射
	 * 
	 * @param inCiCateMap
	 *            内部CI分类关系映射
	 */
	public void setInCiCateMap(List<InCiCateMap> inCiCateMap) {
		this.inCiCateMap = inCiCateMap;
	}

	/**
	 * 获取DB表和CI分类之间的映射
	 * 
	 * @return
	 */
	public List<SourceToCategoryMapping> getDbCiCateMap() {
		return dbCiCateMap;
	}

	/**
	 * 设置DB表和CI分类之间的映射
	 * 
	 * @param dbCiCateMap
	 */
	public void setDbCiCateMap(List<SourceToCategoryMapping> dbCiCateMap) {
		this.dbCiCateMap = dbCiCateMap;
	}

	/**
	 * 获取外部表数据和CI分类数据间的关系映射
	 * 
	 * @return
	 */
	public List<SourceToRelationMapping> getOutCiCateMap() {
		return outCiCateMap;
	}

	/**
	 * 设置外部表数据和CI分类数据间的关系映射
	 * 
	 * @param outCiCateMap
	 */
	public void setOutCiCateMap(List<SourceToRelationMapping> outCiCateMap) {
		this.outCiCateMap = outCiCateMap;
	}

	/**
	 * 获取定时器
	 * 
	 * @return
	 */
	public Map<String, String> getTiming() {
		return timing;
	}

	/**
	 * 设置定时器
	 * 
	 * @param timing
	 *            计时器
	 */
	public void setTiming(Map<String, String> timing) {
		this.timing = timing;
	}

	/**
	 * 是否开启此任务
	 * 
	 * @return
	 */
	public boolean getOpen() {
		return open;
	}

	/**
	 * 设置开启此任务
	 * 
	 * @param open
	 */
	public void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * 获取是否过期
	 * 
	 * @return
	 */
	public boolean getTimeOut() {
		return timeOut;
	}

	/**
	 * 设置是否过期
	 * 
	 * @param timeOut
	 */
	public void setTimeOut(boolean timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * 将任务对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		// ret.put("任务名称", this.getName());
		// ret.put("定时器", this.getTiming());
		// ret.put("是否开启", this.getOpen());
		// ret.put("是否过期", this.getTimeOut());
		//
		// ret.put("DB映射", this.getDbCmIds());
		// ret.put("CI内部映射", this.getInCmIds());
		// ret.put("CI外部映射", this.getOutCmIds());
		ret.put("id", this.getId());
		ret.put("name", this.getName());
		ret.put("timing", this.getTiming());
		ret.put("open", this.getOpen());
		ret.put("timeOut", this.getTimeOut());

		ret.put("sourceToCategoryMappingIds", this.getDbCmIds());
		ret.put("inCiCateMapIds", this.getInCmIds());
		ret.put("sourceToRelationMappingIds", this.getOutCmIds());
		ret.put("perfToDbMappingIds", this.getPerfDbIds());
		ret.put("kpiMapIds", this.getKpiSyncDbIds());
		ret.put("owner", owner);
		return ret;
	}

	/**
	 * 获取已分配CI数据库映射
	 * 
	 * @return
	 */
	public Set<Map<String, String>> getDbCmIds() {
		Set<Map<String, String>> ids = new HashSet<Map<String, String>>();
		List<SourceToCategoryMapping> oms = this.getDbCiCateMap();
		for (SourceToCategoryMapping om : oms) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", om.getId());
			m.put("name", om.getName());
			ids.add(m);
		}
		return ids;
	}

	/**
	 * 获取已分配CI内部关系映射
	 * 
	 * @return
	 */
	public Set<Map<String, String>> getInCmIds() {
		Set<Map<String, String>> ids = new HashSet<Map<String, String>>();
		List<InCiCateMap> ims = this.getInCiCateMap();
		for (InCiCateMap im : ims) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", im.getId());
			m.put("name", im.getName());
			ids.add(m);
		}
		return ids;
	}

	/**
	 * 获取已分配CI外部关系映射
	 * 
	 * @return
	 */
	public Set<Map<String, String>> getOutCmIds() {
		Set<Map<String, String>> ids = new HashSet<Map<String, String>>();
		List<SourceToRelationMapping> oms = this.getOutCiCateMap();
		for (SourceToRelationMapping om : oms) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", om.getId());
			m.put("name", om.getName());
			ids.add(m);
		}
		return ids;
	}

	/**
	 * 获取已分配性能数据映射
	 * 
	 * @return
	 */
	public Set<Map<String, String>> getPerfDbIds() {
		Set<Map<String, String>> ids = new HashSet<Map<String, String>>();
		List<PerfToDbMapping> oms = this.getPerfDbMap();
		for (PerfToDbMapping om : oms) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", om.getId());
			m.put("name", om.getName());
			ids.add(m);
		}
		return ids;
	}

	public Set<Map<String, String>> getKpiSyncDbIds() {
		Set<Map<String, String>> ids = new HashSet<Map<String, String>>();
		List<KpiSyncMapping> kms = this.getKpiSyncMap();
		for (KpiSyncMapping km : kms) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("id", km.getId());
			m.put("name", km.getName());
			ids.add(m);
		}
		return ids;
	}

}
