package com.mmdb.model.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Module implements Serializable{
	private String parentName;//父类的名字
	private String name;//名字 ，唯一键
	private String type;//是目录还是节点（folder,file）
	private String url;//url地址，如果type=folder该属性为空
	private String desc;//描述
	private String iconClass;//图标
	private int orderIndex;//排序字段
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getIconClass() {
		return iconClass;
	}
	public void setIconClass(String iconClass) {
		this.iconClass = iconClass;
	}
	public int getOrderIndex() {
		return orderIndex;
	}
	public void setOrderIndex(int orderIndex) {
		this.orderIndex = orderIndex;
	}
	public Module(String parentName, String name, String type, String url,
			String desc, String iconClass, int orderIndex) {
		super();
		this.parentName = parentName;
		this.name = name;
		this.type = type;
		this.url = url;
		this.desc = desc;
		this.iconClass = iconClass;
		this.orderIndex = orderIndex;
	}
	
	public String getId(){
		return parentName+"@@"+name+"@@"+type+"@@"+url+"@@"+desc+"@@"+iconClass+"@@"+orderIndex;
	}
	public Module() {
		super();
	}
}
