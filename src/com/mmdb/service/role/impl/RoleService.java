package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.Role;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.role.IRoleService;

@Service
public class RoleService implements IRoleService {
	@Autowired
	private IRoleDao roleDao;
	// private IRoleDao roleDao = new RoleDao();

	private Log log = LogFactory.getLogger("RoleService");

	@Override
	public boolean saveRole(Role role) {
		String sql = "insert into tb_portal_role(`rolename`,`roledesc`,`modules`) values('"
				+ role.getRoleName()
				+ "','"
				+ role.getRoleDesc()
				+ "','"
				+ role.getModules() + "')";
		return roleDao.saveObject(sql);
	}

	@Override
	public boolean deleteRole(Role role) {
		String sql = "delete from tb_portal_role where `rolename`='"
				+ role.getRoleName() + "'";
		return roleDao.deleteObject(sql);
	}

	@Override
	public boolean deleteRoleByName(String name) {
		String sql = "delete from tb_portal_role where `rolename`='" + name
				+ "'";
		return roleDao.deleteObject(sql);
	}

	@Override
	public boolean updateRole(Role role) {
		String sql = "update tb_portal_role set `roledesc`='"
				+ role.getRoleDesc() + "',`modules`='" + role.getModules()
				+ "' " + "where `rolename`='" + role.getRoleName() + "'";
		return roleDao.updateObject(sql);
	}

	@Override
	public List<Role> getAllRole() {
		List<Role> list = new ArrayList<Role>();
		String sql = "select * from tb_portal_role";
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Role role = new Role();
				String roleName = rs.getString("rolename");
				String roleDesc = rs.getString("roledesc");
				String modules = rs.getString("modules");
				role.setRoleName(roleName);
				role.setRoleDesc(roleDesc);
				role.setModules(modules);
				list.add(role);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return list;
	}

	@Override
	public Role getRoleByName(String name) {
		Role role = null;
		String sql = "select * from tb_portal_role where `rolename`='" + name
				+ "'";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				role = new Role();
				String roleName = rs.getString("rolename");
				String roleDesc = rs.getString("roledesc");
				String modules = rs.getString("modules");
				role.setRoleName(roleName);
				role.setRoleDesc(roleDesc);
				role.setModules(modules);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return role;
	}

	@Override
	public int getCountRole() {
		return roleDao.getCount("select * from tb_portal_role");
	}

	@Override
	public List<Role> getAllRole(int start, int limit) {
		List<Role> list = new ArrayList<Role>();
		String sql = "select * from tb_portal_role limit " + start + ","
				+ limit;
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Role role = new Role();
				String roleName = rs.getString("rolename");
				String roleDesc = rs.getString("roledesc");
				String modules = rs.getString("modules");
				role.setRoleName(roleName);
				role.setRoleDesc(roleDesc);
				role.setModules(modules);
				list.add(role);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return list;
	}
}
