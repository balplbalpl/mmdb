package com.mmdb.rest.icon;

import java.io.InputStream;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.utils.MD5;
import com.mmdb.model.bean.User;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.icon.IViewIconService;
/**
 * 存储用户头像的接口
 * @author xiongjian
 *
 */
public class UserIconRest extends BaseRest {
	private IViewIconService iconService;

	@Override
	public void ioc(ApplicationContext context) {
		iconService = context.getBean(IViewIconService.class);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return this.notFindMethod(entity);
	}

	@Override
	public Representation getHandler() throws Exception {
		return this.notFindMethod(null);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
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
		for (FileItem fi : items) {
			String contentType = fi.getContentType();
			if (contentType == null || !contentType.startsWith("image")) {
				throw new MException("文件格式有误");
			}
			InputStream is = fi.getInputStream();
			if (is.available() > 524288) {// 512kb
				throw new MException("文件太大了");
			}
			User user = getUser();
			filename = fi.getName();

			filename = new String(filename.getBytes("gbk"), "utf-8");
			filename = user.getLoginName()
					+ filename.substring(filename.indexOf("."));
			byte[] content = new byte[is.available()];
			is.read(content);
			ViewIcon icon = new ViewIcon(filename, user.getLoginName(),
					contentType, content, MD5.md5(content));

			iconService.saveOrUpdateUserIcon(icon);
			iconService.refreshUserIcon(user.getLoginName());
			user.setIcon(filename);
			userService.updateUser(user);
			ret.put("message", "保存成功");
			ret.put("data", user.asMapForRest());
		}
		return new JsonRepresentation(ret.toString());
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return this.notFindMethod(entity);
	}
}
