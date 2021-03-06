package com.mmdb.rest.role;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.Company;
import com.mmdb.model.bean.Dept;
import com.mmdb.model.bean.Module;
import com.mmdb.model.bean.User;
import com.mmdb.model.bean.VitiationToken;
import com.mmdb.service.role.ICompanyService;
import com.mmdb.service.role.IDeptService;
import com.mmdb.service.role.IModuleService;
import com.mmdb.service.role.IRoleService;
import com.mmdb.service.role.ITokenService;
import com.mmdb.service.role.IUserService;
import com.mmdb.service.role.impl.CompanyService;
import com.mmdb.service.role.impl.DeptService;
import com.mmdb.service.role.impl.ModuleService;
import com.mmdb.service.role.impl.RoleService;
import com.mmdb.service.role.impl.TokenService;
import com.mmdb.service.role.impl.UserService;
import com.mmdb.util.des.Des;

@SuppressWarnings("unused")
public class RoleManagerService extends ServerResource {

	IUserService userService;

	IModuleService moduleService;

	ITokenService tokenService;

	IRoleService roleService;

	IDeptService deptService;

	ICompanyService companyService;

	Des des = new Des();

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		userService = (IUserService) SpringContextUtil.getApplicationContext()
				.getBean("userService");
		moduleService = (IModuleService) SpringContextUtil
				.getApplicationContext().getBean("moduleService");
		tokenService = (ITokenService) SpringContextUtil
				.getApplicationContext().getBean("tokenService");
		roleService = (IRoleService) SpringContextUtil.getApplicationContext()
				.getBean("roleService");
		deptService = (IDeptService) SpringContextUtil.getApplicationContext()
				.getBean("deptService");
		deptService = (IDeptService) SpringContextUtil.getApplicationContext()
				.getBean("deptService");
		companyService = (ICompanyService) SpringContextUtil
				.getApplicationContext().getBean("companyService");
	}

	@Override
	@SuppressWarnings("unchecked")
	@Post
	public Representation post(Representation entity) {
		Form form = new Form(entity);
		String operation = null;
		String message = "获取Token成功!";
		boolean success = true;
		String token = null;
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			System.out.println(operation);
			JSONObject json = JSONObject.fromObject(operation);
			String username = json.getString("username");
			String password = json.getString("password");
			User user = userService.getUserByUserAndPwd(username, password);
			if (user == null) {
				message = "获取Token失败,请输入正确的用户名和密码!";
				success = false;
			} else {
				token = des.encrypt(username + "|" + password + "|"
						+ System.currentTimeMillis());
				getResponse().getHeaders().add("token", token);
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "不是有效的JSON数据," + e.getMessage();
			success = false;
		}
		if (success) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("message", message);
			map.put("success", success);
			Map<String, String> data = new HashMap<String, String>();
			if (token != null) {
				data.put("token", token);
			}
			map.put("data", data);
			System.out.println(data);
			return new JsonRepresentation(JSONObject.fromObject(map));
		} else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("message", message);
			map.put("success", success);
			getResponse().setStatus(new Status(401));
			return new JsonRepresentation(JSONObject.fromObject(map));
		}
	}

	@SuppressWarnings("unchecked")
	@Delete
	public Representation delete(Representation entity) {
		String[] paths = getRequest().getResourceRef().getPath().split("/");
		String operation = paths[paths.length - 1];
		String message = "注销成功!";
		boolean success = true;
		if (operation.equals("logout")) {
			String token = this.getRequest().getHeaders()
					.getFirstValue("token");
			if (token != null) {
				VitiationToken bean = new VitiationToken(token,
						System.currentTimeMillis());
				tokenService.deleteTokenByDate(System.currentTimeMillis() - 24
						* 60 * 60 * 1000);
				success = tokenService.saveVitiationToken(bean);
				if (!success) {
					message = "注销失败!";
				}
			} else {
				success = false;
				message = "没有登录的用户!";
			}
		} else {
			getResponse().setStatus(new Status(404));
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message", message);
		map.put("success", success);
		Map<String, String> data = new HashMap<String, String>();
		return new JsonRepresentation(JSONObject.fromObject(map));
	}

	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		// System.out.println(name);
		if (name == null || name.equals("null")) {
			getResponse().setStatus(new Status(404));
			return null;
		}
		String json = "";
		try {
			name = URLDecoder.decode(name, "utf-8");
			// operation = URLDecoder.decode(operation,"utf-8");
			if (name.equals("getTree")) {
				// String head =
				// "[{\"id\":\"根目录\",\"text\":\"根目录\",\"icon\":\"folder.gif\",\"state\" : { \"opened\" : true,\"selected\" : true },\"children\":[";
				String head = "[{\"id\":\"根目录\",\"text\":\"根目录\",\"state\" : { \"opened\" : true,\"selected\" : true },\"children\":[";
				String str = getModuleFolderTree("根目录", "");
				json = head + str + "]}]";
			}

			if (name.equals("getRoleTree")) {
				String head = "[{\"id\":\"根目录\",\"text\":\"根目录\",\"state\" : { \"opened\" : true,\"selected\" : true },\"children\":[";
				String str = getModuleTree("根目录", "");
				json = head + str + "]}]";
			}

			if (name.equals("getCompanyTree")) {
				json = getCompanyTree();
				System.out.println(json);
				// json =
				// "[{\"id\":\"北京优锘科技有限公司\",\"text\":\"北京优锘科技有限公司\",\"state\" : { \"opened\" : true },\"children\":[{\"id\":\"开发部\",\"text\":\"开发部\"},{\"id\":\"服务部\",\"text\":\"服务部\"}]}]";
			}

			if (name.equals("getTreeByUser")) {
				String token = this.getRequest().getHeaders()
						.getFirstValue("token");
				String loginName = des.decrypt(token).split("\\|")[0];
				User user = userService.getUserByLoginName(loginName);
				String role = user.getOwnerRoles();
				String[] roles = role.split(",");
				JSONArray arr = new JSONArray();
				for (String roleName : roles) {
					String ms = roleService.getRoleByName(roleName)
							.getModules();
					JSONArray uarr = JSONArray.fromObject(ms);
					for (int i = 0; i < uarr.size(); i++) {
						arr.add(uarr.getJSONObject(i));
					}
				}
				String head = "[{\"id\":\"根目录\",\"text\":\"根目录\",\"state\" : { \"opened\" : true,\"selected\" : true },\"children\":[";
				String str = getModuleTreeByRoles("根目录", "", arr);
				json = head + str + "]}]";
			}

		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(new Status(600), e.getMessage());
			json = "[]";
		}
		return new StringRepresentation(json);
	}

	/**
	 * 获取功能模块树
	 * 
	 * @param name
	 * @param json
	 * @return
	 */
	public String getModuleTree(String name, String json) {
		List<Module> seclist = moduleService.getModuleByParent(name);
		if (seclist != null && seclist.size() >= 0) {
			for (Module m : seclist) {
				List<Module> list = moduleService
						.getModuleByParent(m.getName());
				String id = m.getId();
				if (list.size() > 0) {
					json = json
							+ "{\"id\":\""
							+ id
							+ "\",\"text\":\""
							+ m.getName()
							+ "\",\"state\" : { \"opened\" : true,\"selected\" : false },\"children\":[";
					String str = getModuleTree(m.getName(), "");
					json = json + str;
					json += "]},";
				} else {
					json = json + "{\"id\":\"" + id + "\",\"text\":\""
							+ m.getName() + "\"},";

				}
			}
		}
		if (json.length() > 5) {
			json = json.substring(0, json.length() - 1);
		}
		return json;
	}

	/**
	 * 获取文件夹树的JSON
	 * 
	 * @param name
	 * @param json
	 * @return
	 */
	public String getModuleFolderTree(String name, String json) {
		List<Module> seclist = moduleService.getFolderByParent(name);
		if (seclist != null && seclist.size() >= 0) {
			for (Module m : seclist) {
				String id = m.getId();
				List<Module> list = moduleService
						.getFolderByParent(m.getName());
				if (list.size() > 0) {
					json = json
							+ "{\"id\":\""
							+ id
							+ "\",\"text\":\""
							+ m.getName()
							+ "\",\"state\" : { \"opened\" : true,\"selected\" : false },\"children\":[";
					String str = getModuleFolderTree(m.getName(), "");
					json = json + str;
					json += "]},";
				} else {
					json = json + "{\"id\":\"" + id + "\",\"text\":\""
							+ m.getName() + "\"},";
				}
			}
		}
		if (json.length() > 5) {
			json = json.substring(0, json.length() - 1);
		}
		return json;
	}

	/**
	 * 获取用户的功能菜单
	 * 
	 * @param name
	 * @param json
	 * @param arr
	 * @return
	 */
	public String getModuleTreeByRoles(String name, String json, JSONArray arr) {

		List<Module> seclist = moduleService.getModuleByParent(name);
		if (seclist != null && seclist.size() >= 0) {
			for (Module m : seclist) {
				List<Module> list = moduleService
						.getModuleByParent(m.getName());
				String id = m.getId();
				boolean flag = false;
				for (int i = 0; i < arr.size(); i++) {
					JSONObject mjson = arr.getJSONObject(i);
					String mname = mjson.getString("name");
					if (mname.equals(m.getName())) {
						flag = true;
					}
				}
				// System.out.println(flag +"   " + m.getName());
				if (flag) {
					if (list.size() > 0) {
						json = json
								+ "{\"id\":\""
								+ id
								+ "\",\"text\":\""
								+ m.getName()
								+ "\",\"state\" : { \"opened\" : true,\"selected\" : false },\"children\":[";
						String str = getModuleTreeByRoles(m.getName(), "", arr);
						json = json + str;
						json += "]},";
					} else {
						json = json + "{\"id\":\"" + id + "\",\"text\":\""
								+ m.getName() + "\"},";
					}
				}
			}
		}
		if (json.length() > 5) {
			json = json.substring(0, json.length() - 1);
		}
		return json;
	}

	/**
	 * 获取组织机构的树状结构
	 * 
	 * @return
	 */
	public String getCompanyTree() {
		List<Company> cs = companyService.getAllCompany();
		List<Dept> list = deptService.getAllDept();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Dept dept : list) {
			String companyName = dept.getCompanyName();
			String deptName = dept.getDeptName();
			if (map.get(companyName) == null) {
				List<String> depts = new ArrayList<String>();
				depts.add(deptName);
				map.put(companyName, depts);
			} else {
				map.get(companyName).add(deptName);
			}
		}

		StringBuffer sf = new StringBuffer(
				"[{\"id\":\"组织机构\",\"text\":\"组织机构\",\"state\" : { \"opened\" : true},\"children\":[");
		for (Company company : cs) {
			String key = company.getCompanyName();
			sf.append("{\"id\":\"")
					.append(key)
					.append("\",\"text\":\"")
					.append(key)
					.append("\",\"state\" : { \"opened\" : true},\"children\":[");
			List<String> ls = map.get(key);
			if (ls != null) {
				for (String l : ls) {
					sf.append("{\"id\":\"").append(l).append("\",\"text\":\"")
							.append(l).append("\"},");
				}
				sf.delete(sf.length() - 1, sf.length());
			}
			sf.append("]},");
		}

		if (!cs.isEmpty()) {
			sf.delete(sf.length() - 1, sf.length());
		}
		sf.append("]}]");
		return sf.toString();
	}

	public static void main(String[] args) {
		RoleManagerService manager = new RoleManagerService();

		IUserService userService = new UserService();

		IModuleService moduleService = new ModuleService();

		ITokenService tokenService = new TokenService();

		IRoleService roleService = new RoleService();

		IDeptService deptService = new DeptService();

		ICompanyService companyService = new CompanyService();

		List<Company> cs = companyService.getAllCompany();

		List<Dept> list = deptService.getAllDept();

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		for (Dept dept : list) {
			String companyName = dept.getCompanyName();
			String deptName = dept.getDeptName();
			if (map.get(companyName) == null) {
				List<String> depts = new ArrayList<String>();
				depts.add(deptName);
				map.put(companyName, depts);
			} else {
				map.get(companyName).add(deptName);
			}
		}

		StringBuffer sf = new StringBuffer(
				"[{\"id\":\"组织架构\",\"text\":\"组织架构\",\"state\" : { \"opened\" : true},\"children\":[");

		// Iterator<String> it = map.keySet().iterator();
		// while(it.hasNext()){
		for (Company company : cs) {
			// key = it.next();
			String key = company.getCompanyName();
			sf.append("{\"id\":\"")
					.append(key)
					.append("\",\"text\":\"")
					.append(key)
					.append("\",\"state\" : { \"opened\" : true},\"children\":[");
			List<String> ls = map.get(key);
			if (ls != null) {
				for (String l : ls) {
					sf.append("{\"id\":\"").append(l).append("\",\"text\":\"")
							.append(l).append("\"},");
				}
				sf.delete(sf.length() - 1, sf.length());
			}
			sf.append("]},");
		}

		if (!cs.isEmpty()) {
			sf.delete(sf.length() - 1, sf.length());
		}
		sf.append("]}]");
		System.out.println(sf.toString());

		/*
		 * User user = userService.getUserByLoginName("user2"); String role =
		 * user.getOwnerRoles(); String[] roles = role.split(","); JSONArray arr
		 * = new JSONArray(); for(String roleName : roles){ String ms =
		 * roleService.getRoleByName(roleName).getModules(); JSONArray uarr =
		 * JSONArray.fromObject(ms); for(int i=0;i<uarr.size();i++){
		 * arr.add(uarr.getJSONObject(i)); } }
		 * 
		 * System.out.println(arr.toString());
		 * //System.out.println(manager.getModuleTreeByRoles("根目录", "",arr));
		 * String head =
		 * "[{\"id\":\"根目录\",\"text\":\"根目录\",\"state\" : { \"opened\" : true,\"selected\" : true },\"children\":["
		 * ; String str = manager.getModuleTreeByRoles("根目录", "",arr); String s
		 * = head + str + "]}]"; System.out.println(s);
		 */
	}
}
