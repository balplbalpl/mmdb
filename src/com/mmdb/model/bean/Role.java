package com.mmdb.model.bean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Role implements Serializable{
	
	private String roleName; //角色名字，唯一键
	private String roleDesc; //角色描述
	private String modules; //该角色的包含的功能模块
	
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public String getRoleDesc() {
		return roleDesc;
	}
	public void setRoleDesc(String roleDesc) {
		this.roleDesc = roleDesc;
	}	
	public String getModules() {
		return modules;
	}
	public void setModules(String modules) {
		this.modules = modules;
	}
	public Role() {
		super();
	}
	
}
