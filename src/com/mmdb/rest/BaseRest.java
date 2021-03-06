package com.mmdb.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.sf.json.JSONObject;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.User;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.des.Des;

public abstract class BaseRest extends ServerResource {
	protected Log log;
	protected IUserService userService;

	public BaseRest() {
		super();
		log = LogFactory.getLogger(this.getClass().getSimpleName());
	}

	@Override
	public void init(Context arg0, Request arg1, Response arg2) {
		super.init(arg0, arg1, arg2);
		ApplicationContext appContext = SpringContextUtil
				.getApplicationContext();
		userService = (IUserService) appContext.getBean("userService");
		ioc(appContext);
	}

	public abstract void ioc(ApplicationContext context);

	@Override
	protected Representation get() throws ResourceException {
		JSONObject ret = new JSONObject();
		try {
			return getHandler();
		} catch (Exception e) {
			log.eLog(e);
			if (e instanceof MException) {
				setStatus(412);
				ret.put("message", e.getMessage());
			} else {
				setStatus(417);
				ret.put("message", "执行失败");
			}
		}
		return new JsonRepresentation(ret.toString());
	}

	@Override
	protected Representation post(Representation entity)
			throws ResourceException {
		JSONObject ret = new JSONObject();
		try {
			return postHandler(entity);
		} catch (Exception e) {
			log.eLog(e);
			if (e instanceof MException) {
				setStatus(412);
				ret.put("message", e.getMessage());
			} else {
				setStatus(417);
				ret.put("message", "执行失败");
			}
		}
		return new JsonRepresentation(ret.toString());
	}

	@Override
	protected Representation put(Representation entity)
			throws ResourceException {
		JSONObject ret = new JSONObject();
		try {
			return putHandler(entity);
		} catch (Exception e) {
			log.eLog(e);
			if (e instanceof MException) {
				setStatus(412);
				ret.put("message", e.getMessage());
			} else {
				setStatus(417);
				ret.put("message", "执行失败");
			}
		}
		return new JsonRepresentation(ret.toString());
	}

	@Delete
	public Representation delete(Representation entity) {
		JSONObject ret = new JSONObject();
		try {
			return delHandler(entity);
		} catch (Exception e) {
			log.eLog(e);
			if (e instanceof MException) {
				setStatus(412);
				ret.put("message", e.getMessage());
			} else {
				setStatus(417);
				ret.put("message", "执行失败");
			}
		}
		return new JsonRepresentation(ret.toString());
	}

	public abstract Representation getHandler() throws Exception;

	public abstract Representation postHandler(Representation entity)
			throws Exception;

	public abstract Representation putHandler(Representation entity)
			throws Exception;

	public abstract Representation delHandler(Representation entity)
			throws Exception;

	protected String getValue(String param) {
		Object data = getRequestAttributes().get(param);
		if (data != null) {
			try {
				return URLDecoder.decode(data.toString(), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 获取用户的loginName
	 * 
	 * @return
	 * @throws Exception
	 */
	protected String getUsername() throws Exception {
		Series<Header> headers = getRequest().getHeaders();
		String values = headers.getValues("token");
		Des des = new Des();
		String decrypt = des.decrypt(values);
		String[] split = decrypt.split("\\|");
		return split[0];
	}

	/**
	 * 获取用户
	 * 
	 * @return
	 */
	protected User getUser() throws Exception {
		User user = null;
		try {
			Series<Header> headers = getRequest().getHeaders();
			String values = headers.getValues("token");
			Des des = new Des();
			String decrypt = des.decrypt(values);
			String[] split = decrypt.split("\\|");
			user = userService.getUserByLoginName(split[0]);
		} catch (Exception e) {
		}
		return user;
	}

	protected boolean isAdmin() {
		try {
			User user = getUser();
			String ownerRoles = user.getOwnerRoles();
			String[] roles = ownerRoles.split(",");
			for (String role : roles) {
				if ("管理员".equals(role)) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	protected void setStatus(int status) {
		getResponse().setStatus(new Status(status));
	}

	protected JSONObject parseEntity(Representation entity) {
		if (entity != null) {
			try {
				String name2 = entity.getMediaType().getName();
				if (name2 != null && name2.indexOf("multipart/form-data") != -1) {
					return new JSONObject();
				}
				Form form = new Form(entity);
				String operation = form.getQueryString();
				operation = URLDecoder.decode(operation, "utf-8");
				operation = new String(operation.getBytes("iso-8859-1"),
						"utf-8");
				return JSONObject.fromObject(operation);
			} catch (Exception e) {
				log.eLog("解析entity失败");
			}
		}
		return new JSONObject();
	}

	protected Representation notFindMethod(Representation entity)
			throws Exception {
		Exception exception = new Exception("未匹配到有效方法");
		log.eLog(exception);
		throw exception;
	}
}
