package com.mmdb.service.role;

import java.util.List;

import com.mmdb.model.bean.User;

public interface IUserService {
    
	/**
	 * 保存用户
	 * @param user
	 * @return
	 */
	public boolean saveUser(User user);
	
	/**
	 * 根据登录名删除用户,登录名为唯一键
	 * @param loginName
	 * @return
	 */
	public boolean deleteUserByLoginName(String loginName);
	
	/**
	 * 更新用户
	 * @param user
	 * @return
	 */
	public boolean updateUser(User user);
	
	/**
	 * 根据登录名获取用户
	 * @param loginName
	 * @return
	 */
	public User getUserByLoginName(String loginName);
	
	/**
	 * 根据登录名和密码获取用户
	 * @param loginName
	 * @param password
	 * @return
	 */
	public User getUserByUserAndPwd(String loginName,String password);
	
	/**
	 * 获取所有用户
	 * @return
	 */
	public List<User> getAllUsers();
	
	/**
	 * 根据角色名获取用户
	 * @param roleName
	 * @return
	 */
	public List<User> getUsersByRoleName(String roleName);
	
	/**
	 * 获取所有用户(分页)
	 * @param start 
	 * @param limit
	 * @return
	 */
	public List<User> getAllUsers(int start,int limit);
	
	/**
	 * 获取所有用户的统计数
	 * @return
	 */
	public int getCount();

	/**
	 * 根据部门名称获取用户（分页）
	 * @param start
	 * @param limit
	 * @param dept
	 * @return
	 */
	List<User> getAllUsers(int start, int limit, String dept);

	/**
	 * 统计部门下的用户统计
	 * @param dept
	 * @return
	 */
	int getCountByDept(String dept);
	
}
