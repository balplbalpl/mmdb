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
import com.mmdb.model.bean.Company;
import com.mmdb.model.bean.Page;
import com.mmdb.service.role.ICompanyService;

public class CompanyRestService extends ServerResource {

	private ICompanyService service;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);
		service = (ICompanyService) SpringContextUtil.getApplicationContext()
				.getBean("companyService");
	}

	@SuppressWarnings("unchecked")
	@Get
	public Representation get(Representation entity) {
		String name = (String) getRequestAttributes().get("name");
		Company company = null;
		String message = "操作成功!";
		boolean success = true;
		try {
			name = URLDecoder.decode(name, "utf-8");
			company = service.getCompanyByName(name);
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		if (company == null) {
			message = "不存在!";
			success = false;
		} else {
			map.put("data", company);
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
			System.out.println(operation);
			String companyName = json.getString("companyName");
			Company company = new Company();
			String companyDesc = json.containsKey("companyDesc") == false ? ""
					: json.getString("companyDesc");
			company.setCompanyName(companyName);
			company.setCompanyDesc(companyDesc);
			if (name != null && name.equals("update")) {
				flag = service.updateCompany(company);
				if (!flag)
					message = "操作失败!";
			} else {
				if (service.getCompanyByName(companyName) == null) {
					flag = service.saveCompany(company);
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
			flag = service.deleteCompany(name);
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
		List<Company> list = service.getAllCompany();
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
