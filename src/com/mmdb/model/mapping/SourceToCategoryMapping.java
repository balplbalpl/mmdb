package com.mmdb.model.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.database.bean.DataSourcePool;

/**
 * 命名空间为["sourceToCateMap"]的数据库和分类之间的映射 Created by XIE on 2015/3/30.
 */
public class SourceToCategoryMapping {
	/**
	 * 关系映射名称(不可重复)
	 */
	private String id;

	private String name;
	/**
	 * 配置项分类
	 */
	// @RelationTo(elementClass = CiCategory.class, type = "SOURCE-DATA",
	// direction = Direction.INCOMING)
	private CiCategory cate;

	private String cateId;
	/**
	 * datasource配置
	 */
	// @RelationTo(elementClass = DataSourcePool.class, type = "SOURCE-POOL",
	// direction = Direction.INCOMING)
	private DataSourcePool dataSource;

	private String dataSourceId;

	/**
	 * 分类和数据库表字段的映射关系(未来会扩展到多字段根据规则组合成为一个新字段)
	 */
	private Map<String, String> fieldMap = new HashMap<String, String>();

	private String owner;

	public SourceToCategoryMapping() {
	}

	public SourceToCategoryMapping(String name, DataSourcePool dataSource,
			CiCategory cate, Map<String, String> fieldMap) {
		this.name = name;
		this.cate = cate;
		this.dataSource = dataSource;
		this.fieldMap = fieldMap;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public CiCategory getCate() {
		return cate;
	}

	public void setCate(CiCategory cate) {
		this.cate = cate;
	}

	public DataSourcePool getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSourcePool dataSource) {
		this.dataSource = dataSource;
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

	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		SourceToCategoryMapping that = (SourceToCategoryMapping) o;

		return !(id != null ? !id.equals(that.id) : that.id != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}

	/**
	 * 反转键值对字段映射
	 * 
	 * @return
	 */
	public Map<String, List<String>> reversalFieldMap() {
		Map<String, List<String>> fm = new HashMap<String, List<String>>();
		Iterator<Map.Entry<String, String>> iter = this.fieldMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, String> entry = iter.next();
			String key = entry.getKey();
			String val = entry.getValue();
			List<String> vs = new ArrayList<String>();
			if (fm.containsKey(val)) {
				vs = fm.get(val);
			}
			vs.add(key);
			fm.put(val, vs);
		}
		return fm;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		// ret.put("映射ID", this.getId());
		// ret.put("映射名称", this.getName());
		//
		// ret.put("dataSource", this.getDataSource().getId());
		// ret.put("数据库连接", this.getDataSource().getDatabaseConfigId());
		// ret.put("分类ID", this.getCate().getId());
		// ret.put("分类名称", this.getCate().getName());
		// ret.put("映射字段", this.getFieldMap());
		ret.put("id", this.getId());
		ret.put("name", this.getName());

		ret.put("dataSourceId", this.getDataSource().getId());
		ret.put("dataSourceName", this.getDataSource().getName());
		ret.put("schemaName", this.getDataSource().getSchema());
		ret.put("tableName", this.getDataSource().getTableName());
		ret.put("customSql", this.getDataSource().getCustomSql());
		ret.put("isSelf", this.getDataSource().isSelf());
		ret.put("databaseConfigId", this.getDataSource().getDatabaseConfigId());
		ret.put("cateId", this.getCate().getId());
		ret.put("cateName", this.getCate().getName());
		ret.put("fieldMap", this.getFieldMap());
		ret.put("owner", owner);
		return ret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
 
}
