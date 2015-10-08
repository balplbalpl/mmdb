package com.mmdb.service.role;

public interface IManageService {

	/**
	 * 删除用户下的角色
	 * @param roleName
	 * @return
	 */
	public boolean deleteUserRole(String roleName);

}
