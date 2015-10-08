package com.mmdb.model.bean;

public class ChartBean {
	
	private String label;
	private String value;
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public ChartBean(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
	public ChartBean() {
		super();
	}
	
}
