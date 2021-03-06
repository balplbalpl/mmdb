package com.mmdb.model.mapping;

import java.util.HashMap;
import java.util.Map;

import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.database.bean.DataSourcePool;

/**
 * 通过数据表同步kpi数据
 * 
 * @author xiongjian
 * 
 */
public class KpiSyncMapping {
	private String id;
	private String name;
	private KpiCategory cate;
	private String cateId;
	private DataSourcePool dataSource;
	private String dataSourceId;
	private String owner;
	
	/**
	 *
	 */
	private Map<String, String> fieldMap = new HashMap<String, String>();

	public KpiSyncMapping() {
	}

	public KpiSyncMapping(String name, KpiCategory cate,
			DataSourcePool dataSource, String dataSourceId,
			Map<String, String> fieldMap) {
		super();
		this.name = name;
		this.cate = cate;
		this.dataSource = dataSource;
		this.dataSourceId = dataSourceId;
		this.fieldMap = fieldMap;
		if (cate != null) {
			this.cateId = cate.getId();
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public KpiCategory getCate() {
		return cate;
	}

	public void setCate(KpiCategory cate) {
		this.cate = cate;
	}

	public DataSourcePool getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSourcePool dataSource) {
		this.dataSource = dataSource;
	}

	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public Map<String, String> getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(Map<String, String> fieldMap) {
		this.fieldMap = fieldMap;
	}

	public String getCateId() {
		return cateId;
	}

	public void setCateId(String cateId) {
		this.cateId = cateId;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("id", id);
		ret.put("name", name);
		ret.put("cateId", cateId);
		ret.put("category", cate == null ? "{}" : cate.toMap());
		ret.put("cateName", cate == null ? "": cate.getName());
		ret.put("fieldMap", fieldMap);
		ret.put("dataSourceId", dataSourceId);
		ret.put("dataSource", dataSource == null ? "{}" : dataSource.asMap());
		ret.put("dataSourceName", dataSource == null ? "" : dataSource.getName());
		ret.put("owner", this.owner);
		return ret;
	}
}
