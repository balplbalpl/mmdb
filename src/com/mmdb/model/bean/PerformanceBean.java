package com.mmdb.model.bean;

public class PerformanceBean {
	
	private String ciCate;
	
	private String ciName;
	
	private String kpiCate;
	
	private String kpiName;
	
	private String instance;
	
	private String startTime;
	
	private String kpiId;
	
	private String ciId;
	
	private String value;

	public String getKpiId() {
		return kpiId;
	}

	public void setKpiId(String kpiId) {
		this.kpiId = kpiId;
	}

	public String getCiId() {
		return ciId;
	}

	public void setCiId(String ciId) {
		this.ciId = ciId;
	}

	public String getCiCate() {
		return ciCate;
	}

	public void setCiCate(String ciCate) {
		this.ciCate = ciCate;
	}

	public String getCiName() {
		return ciName;
	}

	public void setCiName(String ciName) {
		this.ciName = ciName;
	}

	public String getKpiCate() {
		return kpiCate;
	}

	public void setKpiCate(String kpiCate) {
		this.kpiCate = kpiCate;
	}

	public String getKpiName() {
		return kpiName;
	}

	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public PerformanceBean() {
		super();
	}

}
