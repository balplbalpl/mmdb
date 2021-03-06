package com.mmdb.common;

import java.util.List;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.ProjectInfo;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.model.bean.Role;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.service.icon.IImageService;
import com.mmdb.service.icon.IViewIconService;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.role.IRoleService;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.HexString;

/**
 * 用于启动时注册用户.
 * 
 * @author xiongjian
 * 
 */
public class Global {
	private Log log = LogFactory.getLogger("Global");
	private IViewInfoService vInfoService;
	private IUserService userService;
	private IRoleService roleService;
	private IImageService svgService;
	private IViewIconService iconService;
	/**
	 * 配置中svg.base 默认的主题blue,主要用于取svg图片
	 */
	public static final String svgBaseTheme;
	
	public static final String projectName;
	
	public static int projectPort;
	
	static{
		svgBaseTheme = SysProperties.get("svg.base");
		projectName = ProjectInfo.getProjectName();
	}
	public Global() {
		// 初始化用户.
	}

	public void init() {
		createDefaultUser();
		// 刷新个人图片

		// 刷新用户图标
		refreshUserIcon();

		// 刷新全部的SVG
		refreshSvg();

		// 刷新全部视图的图片
		refreshGraphSvg();
	}

	private void refreshUserIcon() {
		try {
			log.iLog("更新全部用户图标");
			if (iconService != null)
				iconService.refreshAllUserIcon();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 刷新全部的视图缩略图
	 */
	private void refreshGraphSvg() {
		try {
			log.iLog("更新视图缩略图");
			List<ViewInformation> all = vInfoService.getAll();
			for (ViewInformation viewInformation : all) {
				String svg = viewInformation.getSvg();
				ViewCategory nc = viewInformation.getCategory();
				String Hexname = HexString.encode(HexString.json2Str(
						nc.getName(), viewInformation.getName()));
				vInfoService.createSvg(Hexname, svg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建默认的admin用戶
	 */
	private void createDefaultUser() {
		log.iLog("初始化管理员用户!");
		User defualt = userService.getUserByLoginName("admin");
		if (defualt == null) {
			User admin = new User();
			admin.setLoginName("admin");
			admin.setUserName("admin");
			admin.setPassword("admin");
			admin.setCompany("uinv");
			admin.setDept("admin");
			StringBuffer roles = new StringBuffer();
			List<Role> allRole = roleService.getAllRole();
			if (allRole != null && allRole.size() > 0) {
				for (Role role2 : allRole) {
					roles.append(role2.getRoleName());
					roles.append(",");
				}
				roles.delete(roles.length() - 1, roles.length());
			}
			admin.setOwnerRoles(roles.toString());
			userService.saveUser(admin);
		}
	}

	/**
	 * ci的图标
	 */
	protected void refreshSvg() {
		log.iLog("更新图标");
		try {
			svgService.copyToDesk();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public IViewInfoService getvInfoService() {
		return vInfoService;
	}

	public void setvInfoService(IViewInfoService vInfoService) {
		this.vInfoService = vInfoService;
	}

	public IUserService getUserService() {
		return userService;
	}

	public void setUserService(IUserService userService) {
		this.userService = userService;
	}

	public IRoleService getRoleService() {
		return roleService;
	}

	public void setRoleService(IRoleService roleService) {
		this.roleService = roleService;
	}

	public IImageService getSvgService() {
		return svgService;
	}

	public void setSvgService(IImageService svgService) {
		this.svgService = svgService;
	}

	public IViewIconService getIconService() {
		return iconService;
	}

	public void setIconService(IViewIconService iconService) {
		this.iconService = iconService;
	}

}
