package com.mmdb.rest.icon;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.utils.MD5;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.User;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.icon.IViewIconService;

public class ViewIconRest extends BaseRest {
	private IViewIconService iconService;

	@Override
	public void ioc(ApplicationContext context) {
		iconService = context.getBean(IViewIconService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		// String param1 = getValue("param1");
		return notFindMethod(null);
	}
	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("save".equals(param1)) {
			return save(entity);
		}
		JSONObject params = parseEntity(entity);
		return query(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return clear();
		} else {// 删除一个属性
			return deleteByName(param1);
		}
	}
	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(null);
	}
	private Representation save(Representation entity) throws Exception {
		log.dLog("save icon");
		JSONObject ret = new JSONObject();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			log.eLog(e);
		}

		String filename = "";
		try {
			for (FileItem fi : items) {
				String contentType = fi.getContentType();
				if (contentType == null || !contentType.startsWith("image")) {
					throw new Exception("文件格式有误");
				}
				InputStream is = fi.getInputStream();
				if (is.available() > 524288) {// 512kb
					throw new Exception("文件太大了");
				}
				User user = getUser();
				filename = fi.getName();

				filename = new String(filename.getBytes("gbk"), "utf-8");

				ViewIcon viewIcon = iconService.getByName(filename,
						user.getLoginName());
				if (viewIcon != null) {
					throw new Exception("图片[" + filename + "]已经存在!");
				}

				byte[] content = new byte[is.available()];
				is.read(content);
				ViewIcon icon = new ViewIcon(filename, user.getLoginName(),
						contentType, content, MD5.md5(content));

				ViewIcon save = iconService.save(icon);
			}
			ret.put("message", "保存成功");
			log.dLog("save success");
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation query(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");
		String name = "";
		try {
			name = data.getString("like");
		} catch (Exception e) {
		}
		Page<Map<String, Object>> content = iconService.fuzzyQuery(name, page,
				pageSize, getUser().getLoginName());
		List<Map<String, Object>> datas = content.getDatas();
		JSONObject retData = new JSONObject();
		retData.put("page", page);
		retData.put("pageSize", pageSize);
		retData.put("datas", datas);
		retData.put("count", content.getTotalCount());

		ret.put("data", retData);
		ret.put("message", "获取全部数据成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteByName(String name) {
		JSONObject ret = new JSONObject();
		try {// 前台上没带后缀的图片名称所以这里加个后缀
			iconService.deleteByName(name, getUser().getLoginName());
			ret.put("message", "删除成功!");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation clear() {
		JSONObject ret = new JSONObject();
		try {// 前台上没带后缀的图片名称所以这里加个后缀
			iconService.deleteAll(getUser().getLoginName());
			ret.put("message", "删除成功!");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}
}
