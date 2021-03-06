package com.mmdb.model.rule;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

public class LinkRule implements Serializable{

	private static final long serialVersionUID = 429092474989304570L;
	
	/**
	 * 规则唯一ID
	 */
	private String id;
	
	private String name;				//选择器名称
	
	/**
	 * 规则类型：
	 * 
	 * link2ci   匹配CI规则
	 * link2kpi  匹配KPI规则
	 */
	private String ruleType;			//规则类型
	
	private String ruleGroup;			//所属规则组
	private String description;			//描述
	private String active;				//1：激活；0：未激活
	private String priority;			//优先级
	private String condition;			//规则条件表达式
	private JSONObject conditionJson;		//规则条件表达式的html
	private String owner;			//创建人

	
	/**
	 * 将匹配规则对象转换成Map
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> rule2Map() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("ruleType", this.getRuleType());
		map.put("ruleGroup", this.getRuleGroup());
		map.put("active", this.getActive());
		map.put("priority", this.getPriority());
		map.put("condition", this.getCondition());
		map.put("conditionJson", this.getConditionJson());
		map.put("owner", this.getOwner());
		return map;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRuleType() {
		return ruleType;
	}

	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleGroup() {
		return ruleGroup;
	}

	public void setRuleGroup(String ruleGroup) {
		this.ruleGroup = ruleGroup;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public JSONObject getConditionJson() {
		return conditionJson;
	}

	public void setConditionJson(JSONObject conditionJson) {
		this.conditionJson = conditionJson;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
