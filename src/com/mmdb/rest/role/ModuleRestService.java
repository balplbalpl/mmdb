package com.mmdb.rest.role;

import java.net.URLDecoder;
import java.util.ArrayList;
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
import com.mmdb.model.bean.Module;
import com.mmdb.model.bean.Page;
import com.mmdb.service.role.IModuleService;

public class ModuleRestService extends ServerResource {

	private IModuleService service;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		service = (IModuleService) SpringContextUtil.getApplicationContext()
				.getBean("moduleService");
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		Module module = null;
		String message = "操作成功!";
		boolean success = true;
		try {
			name = URLDecoder.decode(name, "utf-8");
			module = service.getModuleByName(name);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (module == null) {
			message = "找不到此功能模块!";
			success = false;
		} else {
			map.put("datas", module);
		}
		map.put("message", message);
		map.put("success", success);
		if (!success)
			getResponse().setStatus(new Status(600), message);
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
			String parentName = json.getString("parentName");// 父类的名字
			String mname = json.getString("name");// 名字 ，唯一键
			String type = json.getString("type");// 是目录还是节点（folder,file）
			String url = json.containsKey("url") == false ? "" : json
					.getString("url");// url地址，如果type=folder该属性为空
			String desc = json.containsKey("desc") == false ? "" : json
					.getString("desc");// 描述
			String iconClass = json.containsKey("iconClass") == false ? ""
					: json.getString("iconClass");// 图标
			/*
			 * if (iconClass.length() == 0) { if (type.equals("folder")) {
			 * iconClass = "folder"; } else { iconClass = "file"; } }
			 */
			int orderIndex = json.getInt("orderIndex");// 排序字段
			Module module = new Module(parentName, mname, type, url, desc,
					iconClass, orderIndex);
			if (name != null && name.equals("update")) {
				flag = service.updateModule(module);
				if (!flag)
					message = "操作失败!";
			} else {
				if (service.getModuleByName(mname) == null) {
					flag = service.addModule(module);
					if (!flag)
						message = "操作失败!";
				} else {
					message = "此模块已经存在,请重新填写!";
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
			flag = service.deleteModule(name);
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
		List<Module> list = new ArrayList<Module>();
		Form form = new Form(entity);
		String operation = null;
		int start = 0;
		int limit = 50;
		String name = "";
		try {
			operation = URLDecoder.decode(form.getQueryString(), "utf-8");
			operation = new String(operation.getBytes("iso-8859-1"), "utf-8");
			JSONObject json = JSONObject.fromObject(operation);
			start = json.containsKey("start") == false ? start : Integer
					.parseInt(json.getString("start"));
			limit = json.containsKey("limit") == false ? limit : Integer
					.parseInt(json.getString("limit"));
			name = json.containsKey("name") == false ? "" : json
					.getString("name");
		} catch (Exception e) {
			// e.printStackTrace();
		}
		int totalCount = service.getCountModule();
		if (name.length() <= 0) {
			list = service.getAllModule(start, limit);
			totalCount = service.getCountModule();
		} else {
			list = service.getModuleByParent(name, start, limit);
			totalCount = service.getCountModuleByParentName(name);
		}
		Page page = new Page(list);
		page.setStart(start);
		page.setPageSize(limit);
		page.setTotalCount(totalCount);
		return new JsonRepresentation(JSONObject.fromObject(page));
	}

}
