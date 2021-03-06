package com.mmdb.service.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.service.notify.INotifyService;
import com.mmdb.service.role.IUserService;

@Repository
public class UserService implements IUserService {
	@Autowired
	private IRoleDao roleDao;
	// private IRoleDao roleDao = new RoleDao();

	private Log log = LogFactory.getLogger("UserService");
	
	@Autowired
	private INotifyService notifyService;

	@Override
	public boolean saveUser(User user) {
		String sql = "insert into tb_portal_user(`userid`,`username`,`loginname`,`company`,`dept`,`telphone`,`email`,`createtime`,`updatetime`,`ownerroles`,`selfroles`,`password`,`icon`) values("
				+ "'"
				+ user.getUserId()
				+ "','"
				+ user.getUserName()
				+ "','"
				+ user.getLoginName()
				+ "','"
				+ user.getCompany()
				+ "','"
				+ user.getDept()
				+ "','"
				+ user.getTelphone()
				+ "','"
				+ user.getEmail()
				+ "','"
				+ user.getCreateTime()
				+ "','"
				+ user.getUpdateTime()
				+ "','"
				+ user.getOwnerRoles()
				+ "','"
				+ user.getSelfRoles() + "','" + user.getPassword() +"','"+user.getIcon() +"')";
		boolean flag = roleDao.saveObject(sql);
		
		try {
			notifyService.refreshEpCache("User", "ADD",user.getLoginName(), user.toMap());
		} catch (Exception e) {
			log.eLog("添加用户时更新EP缓存出错",e);
		}
		return flag;
	}

	@Override
	public boolean deleteUserByLoginName(String loginName) {
		String sql = "delete from tb_portal_user where `loginname`='"
				+ loginName + "'";
		boolean flag = roleDao.deleteObject(sql);
		
		try {
			notifyService.refreshEpCache("User", "DEL",loginName, null);
		} catch (Exception e) {
			log.eLog("删除用户时更新EP缓存出错",e);
		}
		return flag;
	}

	@Override
	public boolean updateUser(User user) {
		String sql = "update tb_portal_user set `username`='"
				+ user.getUserName() + "',`company`='" + user.getCompany()
				+ "',`dept`='" + user.getDept() + "',`telphone`='"
				+ user.getTelphone() + "',`email`='" + user.getEmail()
				+ "',`updatetime`='" + user.getUpdateTime()
				+ "',`ownerroles`='" + user.getOwnerRoles() + "',`selfroles`='"
				+ user.getSelfRoles() + "',`password`='" + user.getPassword()
				+ "',`icon`='" + user.getIcon() + "' " + " where `loginname`='"
				+ user.getLoginName() + "'";
		boolean flag = roleDao.updateObject(sql);
		try {
			notifyService.refreshEpCache("User", "UPD",user.getLoginName(), user.toMap());
		} catch (Exception e) {
			log.eLog("修改用户信息时更新EP缓存出错",e);
		}
		return flag;
	}

	@Override
	public User getUserByLoginName(String loginName) {
		User user = null;
		String sql = "select * from tb_portal_user where loginname='"
				+ loginName + "'";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String password = rs.getString("password");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return user;
	}

	@Override
	public List<User> getAllUsers() {
		List<User> users = new ArrayList<User>();
		String sql = "select * from tb_portal_user order by createtime desc";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				User user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");
				String loginName = rs.getString("loginname");
				String password = rs.getString("password");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return users;
	}

	@Override
	public List<User> getAllUsers(int start, int limit, String deptName) {
		List<User> users = new ArrayList<User>();
		String sql = "select * from tb_portal_user where dept='" + deptName
				+ "' or company='" + deptName
				+ "' order by createtime desc limit " + start + "," + limit;
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				User user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");
				String loginName = rs.getString("loginname");
				String password = rs.getString("password");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return users;
	}

	@Override
	public List<User> getAllUsers(int start, int limit) {
		List<User> users = new ArrayList<User>();
		String sql = "select * from tb_portal_user order by createtime desc limit "
				+ start + "," + limit;
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				User user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");
				String loginName = rs.getString("loginname");
				String password = rs.getString("password");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return users;
	}

	@Override
	public int getCount() {
		return roleDao.getCount("select * from tb_portal_user");
	}

	@Override
	public int getCountByDept(String dept) {
		return roleDao.getCount("select * from tb_portal_user where dept='"
				+ dept + "' or company='" + dept + "'");

	}

	@Override
	public User getUserByUserAndPwd(String loginName, String pwd) {
		User user = null;
		String sql = "select * from tb_portal_user where loginname='"
				+ loginName + "' and `password`='" + pwd + "'";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String password = rs.getString("password");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return user;
	}

	@Override
	public List<User> getUsersByRoleName(String roleName) {
		List<User> users = new ArrayList<User>();
		String sql = "select * from tb_portal_user where ownerroles like '"
				+ roleName + "'";
		ResultSet rs = roleDao.getObjectById(sql);
		try {
			while (rs.next()) {
				User user = new User();
				String userId = rs.getString("userid");
				String userName = rs.getString("username");
				String company = rs.getString("company");
				String dept = rs.getString("dept");
				String telphone = rs.getString("telphone");
				String email = rs.getString("email");
				String createTime = rs.getString("createtime");
				String updateTime = rs.getString("updatetime");
				String ownerRoles = rs.getString("ownerroles");
				String selfRoles = rs.getString("selfroles");
				String loginName = rs.getString("loginname");
				String password = rs.getString("password");

				String icon = rs.getString("icon");
				user.setIcon(icon);

				user.setLoginName(loginName);
				user.setPassword(password);
				user.setUserId(userId);
				user.setUserName(userName);
				user.setCompany(company);
				user.setDept(dept);
				user.setTelphone(telphone);
				user.setEmail(email);
				user.setCreateTime(createTime);
				user.setUpdateTime(updateTime);
				user.setOwnerRoles(ownerRoles);
				user.setSelfRoles(selfRoles);
				users.add(user);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return users;
	}

}
