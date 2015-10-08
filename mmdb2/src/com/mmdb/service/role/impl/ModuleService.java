package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.Module;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.role.IModuleService;

@Service
public class ModuleService implements IModuleService {
	@Autowired
	private IRoleDao roleDao;
	// private IRoleDao roleDao = new RoleDao();

	private Log log = LogFactory.getLogger("ModuleService");

	@Override
	public boolean addModule(Module module) {
		String sql = "insert into tb_portal_module(`parentname`,`name`,`type`,`url`,`desc`,`iconclass`,`orderindex`) values("
				+ "'"
				+ module.getParentName()
				+ "',"
				+ "'"
				+ module.getName()
				+ "',"
				+ "'"
				+ module.getType()
				+ "',"
				+ "'"
				+ module.getUrl()
				+ "',"
				+ "'"
				+ module.getDesc()
				+ "',"
				+ "'"
				+ module.getIconClass() + "'," + module.getOrderIndex() + ")";
		return roleDao.saveObject(sql);
	}

	@Override
	public boolean updateModule(Module module) {
		String sql = "update tb_portal_module set " + "`parentname`='"
				+ module.getParentName() + "'," + "`type`='" + module.getType()
				+ "'" + ",`url`='" + module.getUrl() + "'" + ",`desc`='"
				+ module.getDesc() + "'" + ",`iconclass`='"
				+ module.getIconClass() + "'" + ",`orderindex`="
				+ module.getOrderIndex() + " where `name`='" + module.getName()
				+ "'";
		return roleDao.updateObject(sql);
	}

	@Override
	public boolean deleteModule(String name) {
		String sql = "delete from tb_portal_module where `name`='" + name
				+ "' or `parentname`='" + name + "'";
		return roleDao.deleteObject(sql);
	}

	@Override
	public boolean deleteModuleByParent(String parentName) {
		String sql = "delete from tb_portal_module where `parentname`='"
				+ parentName + "'";
		return roleDao.deleteObject(sql);
	}

	@Override
	public List<Module> getModuleByParent(String parentName) {
		List<Module> modules = new ArrayList<Module>();
		String sql = "select * from tb_portal_module where `parentname`='"
				+ parentName + "' order by orderindex";
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Module module = new Module();
				String name = rs.getString("name");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return modules;
	}

	@Override
	public List<Module> getFolderByParent(String parentName) {
		List<Module> modules = new ArrayList<Module>();
		String sql = "select * from tb_portal_module where `type` = 'folder' and `parentname`='"
				+ parentName + "' order by orderindex";
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Module module = new Module();
				String name = rs.getString("name");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return modules;
	}

	@Override
	public Module getModuleByName(String name) {
		Module module = null;
		String sql = "select * from tb_portal_module where `name`='" + name
				+ "'";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				module = new Module();
				String parentName = rs.getString("parentname");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return module;
	}

	@Override
	public List<Module> getAllModule() {
		List<Module> modules = new ArrayList<Module>();
		String sql = "select * from tb_portal_module order by orderindex";
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Module module = new Module();
				String parentName = rs.getString("parentname");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return modules;
	}

	@Override
	public int getCountModule() {
		return roleDao.getCount("select * from tb_portal_module");
	}

	@Override
	public int getCountModuleByParentName(String parentName) {
		return roleDao
				.getCount("select * from tb_portal_module where `parentname`='"
						+ parentName + "'");

	}

	@Override
	public List<Module> getAllModule(int start, int limit) {
		List<Module> modules = new ArrayList<Module>();
		String sql = "select * from tb_portal_module order by orderindex limit "
				+ start + "," + limit;
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Module module = new Module();
				String parentName = rs.getString("parentname");
				String name = rs.getString("name");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return modules;
	}

	@Override
	public List<Module> getModuleByParent(String parentName, int start,
			int limit) {
		List<Module> modules = new ArrayList<Module>();
		String sql = "select * from tb_portal_module where `parentname`='"
				+ parentName + "' order by orderindex limit " + start + ","
				+ limit;
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				Module module = new Module();
				String name = rs.getString("name");
				String type = rs.getString("type");
				String url = rs.getString("url");
				String desc = rs.getString("desc");
				String iconClass = rs.getString("iconclass");
				int orderIndex = rs.getInt("orderindex");
				module.setName(name);
				module.setParentName(parentName);
				module.setType(type);
				module.setUrl(url);
				module.setDesc(desc);
				module.setIconClass(iconClass);
				module.setOrderIndex(orderIndex);
				modules.add(module);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getLocalizedMessage());
		}
		return modules;
	}

}
