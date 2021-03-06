package com.mmdb.service.role.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.bean.User;
import com.mmdb.service.role.IManageService;
import com.mmdb.service.role.IUserService;

@Service
public class ManageService implements IManageService {
	@Autowired
	private IUserService userService;

	// private IUserService userService = new UserService();

	@Override
	public boolean deleteUserRole(String roleName) {
		boolean flag = true;
		List<User> list = userService.getUsersByRoleName(roleName);
		for (User user : list) {
			String[] oldRoles = user.getOwnerRoles().split(",");
			String newRole = "";
			for (String oldRole : oldRoles) {
				if (!roleName.equals(oldRole)) {
					newRole += oldRole + ",";
				}
			}
			if (newRole.length() > 1) {
				newRole = newRole.substring(0, newRole.length() - 1);
			}
			user.setOwnerRoles(newRole);
			boolean f = userService.updateUser(user);
			if (!f) {
				flag = f;
			}
		}
		return flag;
	}

}
