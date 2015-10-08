package com.mmdb.service.role;

import java.util.List;

import com.mmdb.model.bean.Role;


public interface IRoleService {

	/**
	 * 保存角色
	 * @param role
	 * @return
	 */
	public boolean saveRole(Role role);
	
	/**
	 * 删除角色
	 * @param role
	 * @return
	 */
	public boolean deleteRole(Role role);
	
	/**
	 * 根据角色名删除角色，角色名为唯一值
	 * @param name
	 * @return
	 */
	public boolean deleteRoleByName(String name);
	
	/**
	 * 更新角色
	 * @param role
	 * @return
	 */
	public boolean updateRole(Role role);
	
	/**
	 * 获取所有角色
	 * @return
	 */
	public List<Role> getAllRole();
	
	/**
	 * 根据角色名获取角色
	 * @param name
	 * @return
	 */
	public Role getRoleByName(String name);
	
	/**
	 * 统计所有角色
	 * @return
	 */
	public int getCountRole();

	/**
	 * 获取角色（分页）
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Role> getAllRole(int start, int limit);
}
