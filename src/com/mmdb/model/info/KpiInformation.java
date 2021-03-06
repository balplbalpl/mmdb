package com.mmdb.model.info;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.mmdb.model.categroy.KpiCategory;

/**
 * KPI的配置数据
 * 
 * @author yuhao.guan
 */
public class KpiInformation implements Serializable{
	private static final long serialVersionUID = 1L;

	/**
	 * 数据id(唯一)
	 */
	private String id;
	
	private String name;
	/**
	 * 所属分类
	 */
	private String kpiCategoryId;
	
	private KpiCategory kpiCategory;
	
	/**
	 * 分类名称
	 */
	private String kpiCategoryName;
	
	/**
	 * kpiName+kpiCategroyName 进行的base64编码
	 */
	private String kpiHex;

	/**
	 * 阈值设置
	 */
	private String threshold;
	
	/**
	 * 单位
	 */
	private String unit;
	
	
	private String source;
	
	/**
	 * 创建者
	 */
	private String owner;
	
	public KpiInformation(){}
	
	public KpiInformation (KpiCategory category, String source,
			Map<String, Object> data) throws Exception{
		if (source == null) {
			throw new Exception("数据来源不存在");
		} else {
			this.source = source;
		}
		if (category == null) {
			throw new Exception("分类不存在");
		} else {
			this.kpiCategory = category;
			this.kpiCategoryId = category.getId();
		}
		if (data == null || data.size() < 1) {
			throw new Exception("data无数据");
		}
		
		this.name = (String)data.get("name");
		this.kpiCategoryName = category.getName();
		this.kpiCategoryId = category.getId();
		this.threshold = (String)data.get("threshold");
		this.unit = (String)data.get("unit");
		this.owner = (String)data.get("owner");
		//this.source = source;
	}
	
	
	public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", this.getId());
        map.put("name", this.getName());
        map.put("kpiCategoryId",this.getKpiCategoryId());
        map.put("kpiCategoryName",this.getKpiCategoryName());
        map.put("kpiHex", this.getKpiHex());
        map.put("threshold", this.getThreshold());
        map.put("unit", this.getUnit());
        map.put("source", this.getSource());
        map.put("owner", this.getOwner());
        return map;
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

	public String getKpiCategoryId() {
		return kpiCategoryId;
	}

	public void setKpiCategoryId(String kpiCategoryId) {
		this.kpiCategoryId = kpiCategoryId;
	}

	public String getKpiHex() {
		return kpiHex;
	}

	public void setKpiHex(String kpiHex) {
		this.kpiHex = kpiHex;
	}

	public String getThreshold() {
		return threshold;
	}

	public void setThreshold(String threshold) {
		this.threshold = threshold;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public KpiCategory getKpiCategory() {
		return kpiCategory;
	}

	public void setKpiCategory(KpiCategory kpiCategory) {
		this.kpiCategory = kpiCategory;
	}
	
	public String getKpiCategoryName() {
		return kpiCategoryName;
	}

	public void setKpiCategoryName(String kpiCategoryName) {
		this.kpiCategoryName = kpiCategoryName;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
