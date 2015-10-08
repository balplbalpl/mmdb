package com.mmdb.model.mapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mmdb.model.database.bean.DataSourcePool;

import net.sf.json.JSONObject;

/**
 * DB数据集和性能数据映射
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-19
 */
public class PerfToDbMapping implements Serializable{

	private static final long serialVersionUID = 429092474989314571L;
	
	private String id;					   //规则唯一ID
	
	private String name;				   //选择器名称
	
	private String active = "1";				   //1：激活；0：未激活
	
	private String ciCondition;			   //CI匹配规则条件表达式
	
	private String kpiCondition;			//规则条件表达式
	
	private JSONObject ciConditionJson;		//规则条件表达式的json
	
	private JSONObject kpiConditionJson; 	//规则条件表达式的json

	//@RelationTo(elementClass = DataSourcePool.class, type = "SOURCE-POOL", direction = Direction.INCOMING)
    private DataSourcePool dataSource;
    
	private String dataSourceId;		 	//DB 数据集ID
    
	private String createPerson;			//创建人

	private String taskNames;				//使用此映射的任务们
	
	private JSONObject fieldMap;			//数据库字段映射关系
	
	private JSONObject customFieldsMap;		//存放自定义值的字段
	
	private String valExp;
	
	
	//CI KPI信息只有在用户在规则中配置的是自定义的固定KPI、CI信息的时候才有用
	
	private String ciHex;
	
	private String kpiHex;

	private String ciCategoryId;
	
	private String ciCategoryName;
	
	private String ciId;
	
	private String ciName;
	
	private String kpiCategoryName;
	
	private String kpiCategoryId;
	
	private String kpiId;
	
	private String kpiName;
	
	private String owner;
	
	private String isAddSync; //数据是否为增量同步 true:增量  false:全量
	
	/**
	 * 将匹配规则对象转换成Map
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("active", this.getActive());
		map.put("ciCondition", this.getCiCondition());
		map.put("kpiCondition", this.getKpiCondition());
		map.put("ciConditionJson", this.getCiConditionJson());
		map.put("kpiConditionJson", this.getKpiConditionJson());
		map.put("dataSourceId", this.getDataSourceId());
		if(this.dataSource!=null){
			map.put("dataSourceName", this.getDataSource().getName());
		}
		map.put("taskNames", this.getTaskNames());
		map.put("createPerson", this.getCreatePerson());
		map.put("fieldMap", this.getFieldMap());
		map.put("customFieldsMap", this.getCustomFieldsMap());
		map.put("valExp", this.getValExp());
		
		map.put("ciName", this.getCiName());
		map.put("ciCategoryId", this.getCiCategoryId());
		map.put("ciCategoryName", this.getCiCategoryName());
		map.put("ciId", this.getCiId());
		map.put("kpiCategoryName", this.getKpiCategoryName());
		map.put("kpiCategoryId", this.getKpiCategoryId());
		map.put("kpiId", this.getKpiId());
		map.put("kpiName", this.getKpiName());
		map.put("owner", this.owner);
		if(this.getIsAddSync()!=null){
			map.put("isAddSync", Boolean.parseBoolean(this.getIsAddSync()));
		}
		return map;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public String getCreatePerson() {
		return createPerson;
	}

	public void setCreatePerson(String createPerson) {
		this.createPerson = createPerson;
	}

	
	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCiCondition() {
		return ciCondition;
	}

	public void setCiCondition(String ciCondition) {
		this.ciCondition = ciCondition;
	}

	public String getKpiCondition() {
		return kpiCondition;
	}

	public void setKpiCondition(String kpiCondition) {
		this.kpiCondition = kpiCondition;
	}

	public JSONObject getCiConditionJson() {
		return ciConditionJson;
	}

	public void setCiConditionJson(JSONObject ciConditionJson) {
		this.ciConditionJson = ciConditionJson;
	}

	public JSONObject getKpiConditionJson() {
		return kpiConditionJson;
	}

	public void setKpiConditionJson(JSONObject kpiConditionJson) {
		this.kpiConditionJson = kpiConditionJson;
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
	

	public String getTaskNames() {
		return taskNames;
	}

	public void setTaskNames(String taskNames) {
		this.taskNames = taskNames;
	}

	public JSONObject getFieldMap() {
		return fieldMap;
	}

	public void setFieldMap(JSONObject fieldMap) {
		this.fieldMap = fieldMap;
	}

	public String getValExp() {
		return valExp;
	}

	public void setValExp(String valExp) {
		this.valExp = valExp;
	}
	
	public JSONObject getCustomFieldsMap() {
		return customFieldsMap;
	}

	public void setCustomFieldsMap(JSONObject customFieldsMap) {
		this.customFieldsMap = customFieldsMap;
	}
	
	public String getCiHex() {
		return ciHex;
	}

	public void setCiHex(String ciHex) {
		this.ciHex = ciHex;
	}

	public String getKpiHex() {
		return kpiHex;
	}

	public void setKpiHex(String kpiHex) {
		this.kpiHex = kpiHex;
	}
	
	public String getCiCategoryId() {
		return ciCategoryId;
	}

	public void setCiCategoryId(String ciCategoryId) {
		this.ciCategoryId = ciCategoryId;
	}

	public String getCiCategoryName() {
		return ciCategoryName;
	}

	public void setCiCategoryName(String ciCategoryName) {
		this.ciCategoryName = ciCategoryName;
	}

	public String getCiId() {
		return ciId;
	}

	public void setCiId(String ciId) {
		this.ciId = ciId;
	}

	public String getCiName() {
		return ciName;
	}

	public void setCiName(String ciName) {
		this.ciName = ciName;
	}

	public String getKpiCategoryName() {
		return kpiCategoryName;
	}

	public void setKpiCategoryName(String kpiCategoryName) {
		this.kpiCategoryName = kpiCategoryName;
	}

	public String getKpiCategoryId() {
		return kpiCategoryId;
	}

	public void setKpiCategoryId(String kpiCategoryId) {
		this.kpiCategoryId = kpiCategoryId;
	}

	public String getKpiId() {
		return kpiId;
	}

	public void setKpiId(String kpiId) {
		this.kpiId = kpiId;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getIsAddSync() {
		return isAddSync;
	}

	public void setIsAddSync(String isAddSync) {
		this.isAddSync = isAddSync;
	}
}
