package com.mmdb.model.bean;

public class PerformanceViewBean {

	private String viewName;
	
	private String userName;
	
	private String kpis;
	
	private String createTime;
	
	private String desc;
	

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getKpis() {
		return kpis;
	}

	public void setKpis(String kpis) {
		this.kpis = kpis;
	}

	public PerformanceViewBean() {
		super();
	}
	
	
}
