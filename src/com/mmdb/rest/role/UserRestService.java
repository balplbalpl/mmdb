package com.mmdb.rest.role;

import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import com.mmdb.model.bean.User;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.des.Des;

public class UserRestService extends ServerResource {

	private IUserService userService;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);

		userService = (IUserService) SpringContextUtil.getApplicationContext()
				.getBean("userService");

	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		Map<String, Object> map = new HashMap<String, Object>();
		User user = null;
		String message = "操作成功!";
		boolean flag = true;
		if (name == null || "".equals(name)) {
			org.restlet.util.Series<org.restlet.data.Header> headers = getRequest()
					.getHeaders();
			String values = headers.getValues("token");
			if (values == null || "".equals(values)) {
				map.put("message", "用户未登陆");
				return new JsonRepresentation(JSONObject.fromObject(map));
			}
			Des des = new Des();
			String decrypt = des.decrypt(values);
			String[] split = decrypt.split("\\|");
			user = userService.getUserByLoginName(split[0]);
			user.setPassword("");

			String ownerRoles = user.getOwnerRoles();
			String[] roles = ownerRoles.split(",");
			boolean isManager = false;
			for (String role : roles) {
				if("管理员".equals(role)){
					isManager =true;
				}
			}
			Map<String, Object> asMap = user.asMapForRest();
			asMap.put("isManager", isManager);
			map.put("data", asMap);
		} else {
			try {
				name = URLDecoder.decode(name, "utf-8");
				user = userService.getUserByLoginName(name);
			} catch (Exception e) {
				e.printStackTrace();
				message = e.getMessage();
				flag = false;
			}
			if (user == null) {
				message = "找不到此用户!";
				flag = false;
			} else {
				user.setPassword("");
				map.put("data", user);
			}
		}
		map.put("message", message);
		map.put("success", flag);
		return new JsonRepresentation(JSONObject.fromObject(map).toString());
	}

	@Override
	@SuppressWarnings("unchecked")
	@Put
	public Representation put(Representation entity) {
		Form form = new Form(entity);
		String name = (String) getRequestAttributes().get("name");
		String operation = null;
		boolean flag = true;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = sf.format(cal.getTime());
		String message = "操作成功!";
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			JSONObject json = JSONObject.fromObject(operation);
			String userName = json.getString("userName");
			String company = json.containsKey("company") == false ? "" : json
					.getString("company");
			String dept = json.containsKey("dept") == false ? "" : json
					.getString("dept");
			String telphone = json.containsKey("telphone") == false ? "" : json
					.getString("telphone");
			String email = json.containsKey("email") == false ? "" : json
					.getString("email");
			String ownerRoles = json.getString("ownerRoles");
			// String selfRoles = json.getString("selfRoles");
			String loginName = json.getString("loginName");
			String password = json.getString("password");
			User user = new User();
			user.setLoginName(loginName);
			user.setPassword(password);
			user.setUserName(userName);
			user.setCompany(company);
			user.setDept(dept);
			user.setTelphone(telphone);
			user.setEmail(email);
			user.setUpdateTime(currentTime);
			user.setOwnerRoles(ownerRoles);
			// user.setSelfRoles(selfRoles);
			if (name != null && name.equals("update")) {
				flag = userService.updateUser(user);
				if (!flag)
					message = "操作失败!";
			} else {
				user.setCreateTime(currentTime);
				if (userService.getUserByLoginName(loginName) == null) {
					flag = userService.saveUser(user);
					if (!flag)
						message = "操作失败!";
				} else {
					message = "此用户已经存在,请重新填写!";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
			flag = false;
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
			flag = userService.deleteUserByLoginName(name);
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
		List<User> list = null;
		int start = 0;
		int limit = 50;
		String dept = "";
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			JSONObject json = JSONObject.fromObject(operation);
			start = json.containsKey("start") == false ? start : Integer
					.parseInt(json.getString("start"));
			limit = json.containsKey("limit") == false ? limit : Integer
					.parseInt(json.getString("limit"));
			dept = json.containsKey("dept") == false ? dept : json
					.getString("dept");
			dept = dept.replaceAll("\n|\t", "");

		} catch (Exception e) {
			// e.printStackTrace();
		}
		int totalCount = userService.getCount();
		if (dept.length() <= 0) {
			list = userService.getAllUsers(start, limit);
		} else {
			list = userService.getAllUsers(start, limit, dept);
			totalCount = userService.getCountByDept(dept);
		}
		Page page = new Page(list);

		page.setStart(start);
		page.setPageSize(limit);
		page.setTotalCount(totalCount);
		return new JsonRepresentation(JSONObject.fromObject(page));
	}

}
