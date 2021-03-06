package com.mmdb.model.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class User implements Serializable {
	private String userId;
	private String userName;
	private String loginName;//唯一的
	private String password;
	private String company;
	private String dept;
	private String telphone;
	private String email;
	private String createTime;
	private String updateTime;
	private String ownerRoles;// 拥有的权限
	private String selfRoles;// 自身定义的权限
	private String icon;
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	/**
	 * 用户唯一标示
	 * @return
	 */
	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public String getTelphone() {
		return telphone;
	}

	public void setTelphone(String telphone) {
		this.telphone = telphone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getOwnerRoles() {
		return ownerRoles;
	}

	public void setOwnerRoles(String ownerRoles) {
		this.ownerRoles = ownerRoles;
	}

	public String getSelfRoles() {
		return selfRoles;
	}

	public void setSelfRoles(String selfRoles) {
		this.selfRoles = selfRoles;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}
	
	public Map<String,Object> asMapForRest(){
		Map<String,Object> ret = new HashMap<String,Object>();
		ret.put("company", getCompany());
		ret.put("id", getLoginName());
		ret.put("createTime", getCreateTime());
		ret.put("dept", getDept());
		ret.put("email", getEmail());
		String icon = getIcon();
		if(icon==null||"".equals(icon)||icon.indexOf(".")==-1){
			icon = "default_user_icon.png";
		}
		ret.put("icon", "/resource/usericon/"+icon);
		ret.put("loginName", getLoginName());
		ret.put("ownerRoles", getOwnerRoles());
		ret.put("selfRoles", getSelfRoles());
		ret.put("telphone", getTelphone());
		ret.put("updateTime", getUpdateTime());
		ret.put("userName", getUserName());
		ret.put("password", "******");
		return ret;
	}
	
	public Map<String,Object> toMap(){
		Map<String,Object> ret = new HashMap<String,Object>();
		ret.put("id",getUserId());
		ret.put("username", getUserName());
		ret.put("email", getEmail());
		ret.put("loginname", getLoginName());
		ret.put("telphone", getTelphone());
		return ret;
	}
	
	public User() {
		super();
	}
}
