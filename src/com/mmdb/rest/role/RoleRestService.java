package com.mmdb.rest.role;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.Role;
import com.mmdb.service.role.IManageService;
import com.mmdb.service.role.IRoleService;

public class RoleRestService extends ServerResource {

	private IRoleService service;

	private IManageService manageService;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);

		service = (IRoleService) SpringContextUtil.getApplicationContext()
				.getBean("roleService");

		manageService = (IManageService) SpringContextUtil
				.getApplicationContext().getBean("manageService");
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		Role role = null;
		String message = "操作成功!";
		boolean success = true;
		try {
			name = URLDecoder.decode(name, "utf-8");
			role = service.getRoleByName(name);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (role == null) {
			message = "找不到此角色!";
			success = false;
		} else {
			map.put("data", role);
		}
		map.put("message", message);
		map.put("success", success);
		return new JsonRepresentation(JSONObject.fromObject(map));
	}

	@Override
	@SuppressWarnings("unchecked")
	@Put
	public Representation put(Representation entity) {
		Form form = new Form(entity);
		String name = (String) getRequestAttributes().get("name");
		String operation = null;
		boolean flag = true;
		String message = "操作成功!";
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			JSONObject json = JSONObject.fromObject(operation);
			String roleName = json.getString("roleName");
			Role role = new Role();
			String moduels = json.containsKey("modules") == false ? "" : json
					.getString("modules");
			String roleDesc = json.containsKey("roleDesc") == false ? "" : json
					.getString("roleDesc");
			role.setRoleName(roleName);
			role.setModules(moduels);
			role.setRoleDesc(roleDesc);
			if (name != null && name.equals("update")) {
				flag = service.updateRole(role);
				if (!flag)
					message = "操作失败!";
			} else {
				if (service.getRoleByName(roleName) == null) {
					flag = service.saveRole(role);
					if (!flag)
						message = "操作失败!";
				} else {
					message = "此角色已经存在,请重新填写!";
					flag = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message", message);
		map.put("success", flag);
		if (!flag)
			getResponse().setStatus(new Status(600), message);
		return new JsonRepresentation(JSONObject.fromObject(map));
	}

	@SuppressWarnings("unchecked")
	@Delete
	public Representation delete(Representation entity) {
		Map<String, Object> map = new HashMap<String, Object>();
		String name = (String) getRequestAttributes().get("name");
		String message = "操作成功!";
		boolean flag = true;
		try {
			name = URLDecoder.decode(name, "utf-8");
			if (!service.deleteRoleByName(name)) {
				flag = false;
			}
			if (!manageService.deleteUserRole(name)) {
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		if (!flag)
			message = "操作失败!";
		map.put("message", message);
		map.put("success", flag);
		if (!flag)
			getResponse().setStatus(new Status(600), message);
		return new JsonRepresentation(JSONObject.fromObject(map));
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Post
	public Representation post(Representation entity) {

		Form form = new Form(entity);
		String operation = null;
		int start = 0;
		int limit = 50;
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			JSONObject json = JSONObject.fromObject(operation);
			start = json.containsKey("start") == false ? start : Integer
					.parseInt(json.getString("start"));
			limit = json.containsKey("limit") == false ? limit : Integer
					.parseInt(json.getString("limit"));
		} catch (Exception e) {
			// e.printStackTrace();
		}
		List<Role> list = service.getAllRole(start, limit);
		Page page = new Page(list);
		page.setStart(start);
		page.setPageSize(limit);
		int totalCount = service.getCountRole();
		page.setTotalCount(totalCount);
		return new JsonRepresentation(JSONObject.fromObject(page));
	}

}
