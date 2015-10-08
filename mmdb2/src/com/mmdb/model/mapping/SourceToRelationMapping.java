package com.mmdb.model.mapping;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.database.bean.DataSourcePool;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命名空间为[sourceToRelMap]外部关系映射 Created by XIE on 2015/3/30.
 */
public class SourceToRelationMapping {// extends NodeEntity
	/**
	 * 关系映射名称(不可重复)
	 */
	private String id;
	
	private String name;

	/**
	 * datasource配置
	 */
	private DataSourcePool dataSource;
	
	private String dataSourceId;
	
	/**
	 * 关系分类
	 */
	private RelCategory relCate;
	
	private String relCateId;

	/**
	 * 关系分类的属性对应的值
	 */
	private Map<String, Object> relValue = new HashMap<String, Object>();
	/**
	 * CI分类起点
	 */
	private CiCategory sourceCate;
	/**
	 * CI分类ID - 起点
	 */
	private String sourceCateId;
	/**
	 * CI分类终点
	 */
	private CiCategory targetCate;
	/**
	 * CI分类ID - 终点
	 */
	private String targetCateId;
	/**
	 * 表字段 - 起点
	 */
	private String sourceField;
	/**
	 * 表字段 - 终点
	 */
	private String targetField;

	private String owner;

	public SourceToRelationMapping() {
	}

	/**
	 * 新建外部映射[构造函数]
	 * 
	 * @param id
	 *            映射名称
	 * @param rc
	 *            关系分类
	 * @param relValue
	 *            关系属性对应的值
	 * @param sourceField
	 *            起点表字段
	 * @param targetField
	 *            终点表字段
	 */
	public SourceToRelationMapping(String name, RelCategory rc,
			Map<String, Object> relValue, DataSourcePool dp,
			String sourceField, String targetField) {
		this.name = name;
		this.relCate = rc;
		this.relValue = this.paddingRelValue(relValue);
		this.dataSource = dp;
		this.sourceField = sourceField;
		this.targetField = targetField;
	}

	/**
	 * 新建外部关系映射
	 * 
	 * @param id
	 *            映射名称
	 * @param rc
	 *            关系分类
	 * @param relValue
	 *            关系属性对应的值
	 * @param sc
	 *            起点CI分类
	 * @param ec
	 *            终点CI分类
	 * @param sourceField
	 *            起点表字段
	 * @param targetField
	 *            终点表字段
	 */
	public SourceToRelationMapping(String id, RelCategory rc,
			Map<String, Object> relValue, CiCategory sc, CiCategory ec,
			DataSourcePool dp, String sourceField, String targetField) {
		this.id = id;
		this.relCate = rc;
		this.relValue = this.paddingRelValue(relValue);
		if (sc != null) {
			this.sourceCate = sc;
			this.sourceCateId = sc.getId();
		}
		if (ec != null) {
			this.targetCate = ec;
			this.targetCateId = ec.getId();
		}
		this.dataSource = dp;
		this.sourceField = sourceField;
		this.targetField = targetField;
	}

	/**
	 * 处理关系属性空值
	 * 
	 * @param relValue
	 * @return
	 */
	public Map<String, Object> paddingRelValue(Map<String, Object> relValue) {
		if (relValue == null) {
			relValue = new HashMap<String, Object>();
		}
		List<String> attrs = this.relCate.getAttributeNames();
		Map<String, Object> relVal = new HashMap<String, Object>();
		for (String a : attrs) {
			Object obj = null;
			if (relValue.containsKey(a)) {
				obj = relValue.get(a);
				if (obj == null || obj.equals("")) {
					obj = this.relCate.getAttributeByName(a).getDefaultValue();
				}
			} else {
				obj = this.relCate.getAttributeByName(a).getDefaultValue();
			}
			relVal.put(a, obj);
		}
		return relVal;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DataSourcePool getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSourcePool dataSource) {
		this.dataSource = dataSource;
	}

	public RelCategory getRelCate() {
		return relCate;
	}

	public void setRelCate(RelCategory relCate) {
		this.relCate = relCate;
	}

	public Map<String, Object> getRelValue() {
		return relValue;
	}

	public void setRelValue(Map<String, Object> relValue) {
		this.relValue = relValue;
	}

	public CiCategory getSourceCate() {
		return sourceCate;
	}

	public void setSourceCate(CiCategory sourceCate) {
		this.sourceCate = sourceCate;
	}

	public String getSourceCateId() {
		return sourceCateId;
	}

	public void setSourceCateId(String sourceCateId) {
		this.sourceCateId = sourceCateId;
	}

	public CiCategory getTargetCate() {
		return targetCate;
	}

	public void setTargetCate(CiCategory targetCate) {
		this.targetCate = targetCate;
	}

	public String getTargetCateId() {
		return targetCateId;
	}

	public void setTargetCateId(String targetCateId) {
		this.targetCateId = targetCateId;
	}

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}

	public String getTargetField() {
		return targetField;
	}

	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}
	
	public String getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(String dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public String getRelCateId() {
		return relCateId;
	}

	public void setRelCateId(String relCateId) {
		this.relCateId = relCateId;
	}

	 
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;

		SourceToRelationMapping that = (SourceToRelationMapping) o;

		return !(id != null ? !id.equals(that.id) : that.id != null);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (id != null ? id.hashCode() : 0);
		return result;
	}

	/**
	 * 将数据库关系映射对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
//		ret.put("映射ID", this.getId());
//		ret.put("映射名称", this.getName());
//
//		ret.put("relId", this.getRelCate().getId());
//		ret.put("relName", this.getRelCate().getName());
//		ret.put("关系属性值", this.getRelValue());
//
//		ret.put("sourceCateName", this.getSourceCate() != null ? this
//				.getSourceCate().getName() : null);
//		ret.put("sourceCateId", this.getSourceCateId());
//
//		ret.put("targetCateName", this.getTargetCate() != null ? this
//				.getTargetCate().getName() : null);
//		ret.put("targetCateId", this.getTargetCateId());
//
//		ret.put("数据库连接", this.getDataSource().getId());
//		ret.put("dbMap", this.getDataSource().getDc().asMap());
//		ret.put("selfMap", this.getDataSource().getDs().asMap());
//
//		ret.put("sourceField", this.getSourceField());
//		ret.put("targetField", this.getTargetField());
		ret.put("id", this.getId());
        ret.put("name", this.getName());
        ret.put("dataSourceId", this.getDataSource().getId());
        ret.put("dataSourceName", this.getDataSource().getName());
        ret.put("schemaName", this.getDataSource().getSchema());
        ret.put("tableName", this.getDataSource().getTableName());
        ret.put("customSql", this.getDataSource().getCustomSql());
        ret.put("isSelf", this.getDataSource().isSelf());
        ret.put("databaseConfigId", this.getDataSource().getDatabaseConfigId());
        ret.put("relCateId", this.getRelCateId());
        ret.put("relCateName", this.getRelCate().getName());
        ret.put("relValue", JSONObject.fromObject(this.getRelValue()));
        ret.put("sourceCateId", this.getSourceCateId());
        ret.put("sourceCateName", this.getSourceCate()==null ? "":this.getSourceCate().getName());
        ret.put("sourceField", this.getSourceField());
        ret.put("targetCateId", this.getTargetCateId());
        ret.put("targetCateName", this.getTargetCate()==null ? "":this.getTargetCate().getName());
        ret.put("targetField", this.getTargetField());
        ret.put("owner", this.owner);
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
