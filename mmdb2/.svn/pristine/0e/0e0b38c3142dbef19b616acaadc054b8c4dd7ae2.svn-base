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
import com.mmdb.model.bean.Dept;
import com.mmdb.model.bean.Page;
import com.mmdb.service.role.IDeptService;

public class DeptRestService extends ServerResource {

	// private IDeptService service = new DeptService();
	private IDeptService service;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		service = (IDeptService) SpringContextUtil.getApplicationContext()
				.getBean("deptService");
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		Dept dept = null;
		String message = "操作成功!";
		boolean success = true;
		try {
			name = URLDecoder.decode(name, "utf-8");
			dept = service.getDeptByName(name);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (dept == null) {
			message = "不存在!";
			success = false;
		} else {
			map.put("data", dept);
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
			String deptName = json.getString("deptName");
			Dept dept = new Dept();
			String deptDesc = json.containsKey("deptDesc") == false ? "" : json
					.getString("deptDesc");
			String companyName = json.containsKey("companyName") == false ? ""
					: json.getString("companyName");
			dept.setDeptName(deptName);
			dept.setCompanyName(companyName);
			dept.setDeptDesc(deptDesc);
			if (name != null && name.equals("update")) {
				flag = service.updateDept(dept);
				if (!flag)
					message = "操作失败!";
			} else {
				// System.out.println(service.getDeptByName(deptName).getDeptName());
				if (service.getDeptByName(deptName) == null) {
					flag = service.saveDept(dept);
					if (!flag)
						message = "操作失败!";
				} else {
					message = "已存在,请重新填写!";
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
			flag = service.deleteDept(name);
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
		List<Dept> list = service.getAllDept();
		Page page = new Page(list);
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
		page.setStart(start);
		page.setPageSize(limit);
		int totalCount = list.size();
		page.setTotalCount(totalCount);
		return new JsonRepresentation(JSONObject.fromObject(page));
	}

}
