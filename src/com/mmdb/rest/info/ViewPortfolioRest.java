package com.mmdb.rest.info;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.model.info.ViewPortfolio;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.info.IViewPortfolioService;
import com.mmdb.service.relation.IViewViewProtfolioRelService;
import com.mmdb.service.role.IUserService;
import com.mmdb.service.subscription.ISubscriptionService;
import com.mmdb.service.subscription.ISubscriptionViewPortfolioService;

public class ViewPortfolioRest extends BaseRest {
	private Log log = LogFactory.getLogger("ViewPortfolioRest");
	/**
	 * 确认权限时使用的动作,删除动作
	 */
	private final int DELETE = 0;
	/**
	 * 确认权限时使用的动作,添加动作
	 */
	private final int ADD = 1;
	/**
	 * 确认权限时使用的动作,更新动作
	 */
	private final int UPDATE = 2;
	/**
	 * 确认权限时使用的动作,查看动作
	 */
	private final int SEE = 4;

	private IViewPortfolioService viewPorService;
	private IViewViewProtfolioRelService viewRelToProtService;
	private IUserService userService;
	private ISubscriptionViewPortfolioService subViewPortService;
	private ISubscriptionService subViewService;
	private IViewInfoService vInfoService;

	@Override
	public void init(Context context, Request request, Response response) {
		super.init(context, request, response);

	}

	@Override
	public void ioc(ApplicationContext context) {
		viewPorService = context.getBean(IViewPortfolioService.class);
		viewRelToProtService = context
				.getBean(IViewViewProtfolioRelService.class);
		subViewPortService = context
				.getBean(ISubscriptionViewPortfolioService.class);
		subViewService = context.getBean(ISubscriptionService.class);
		vInfoService = context.getBean(IViewInfoService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll(null);
		} else if ("private".equals(param1)) {
			return getAll(false);
		} else if ("public".equals(param1)) {
			String param2 = (String) getRequestAttributes().get("param2");
			if (param2 != null) {
				try {
					param2 = URLDecoder.decode(param2, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}
			}
			if (param2 == null || param2.equals("")) {
				return getOpenViewPortfolioAuthors();
			} else {
				return getOpenViewPortfolioByAuthor(param2);
			}
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		// String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		return save(params);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		if ("open".equals(param1)) {
			return openView(param2);
		} else if ("close".equals(param1)) {
			return closeView(param2);
		}
		JSONObject params = parseEntity(entity);
		return edit(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll(null);
		} else if ("private".equals(param1)) {
			return deleteAll(false);
		} else if ("public".equals(param1)) {
			return deleteAll(true);
		} else {
			return deleteById(param1);
		}
	}

	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		ViewPortfolio viewProt = viewPorService.getById(id);
		if (viewProt != null) {
			Map<String, Object> asMap = viewProt.asMap();
			ret.put("data", asMap);
			ret.put("message", "获取视图分类成功");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getAll(Boolean open) throws Exception {
		JSONObject ret = new JSONObject();
		List<ViewPortfolio> list = null;
		if (open == null) {
			list = viewPorService.getAllOpenViewPort();
		} else if (open) {
			list = viewPorService.getAllOpenViewPort();
		} else {
			list = viewPorService.getAllByUser(getUser());
		}
		JSONArray data = new JSONArray();
		for (ViewPortfolio viewCategory : list) {
			Map<String, Object> asMap = viewCategory.asMap();
			data.add(asMap);
		}
		ret.put("data", data);
		ret.put("message", "获取全部组合视图成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getOpenViewPortfolioByAuthor(String param2)
			throws Exception {
		JSONObject ret = new JSONObject();
		User user = userService.getUserByLoginName(param2);
		if (user == null) {
			throw new MException("用户[" + param2 + "]不存在");
		}
		List<ViewPortfolio> list = viewPorService.getAllOpenViewPortByUser(user
				.getLoginName());
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		for (ViewPortfolio viewPortfolio : list) {
			Map<String, Object> asMap = viewPortfolio.asMap();
			retData.add(asMap);
		}
		ret.put("message", "获取组合视图成功");
		ret.put("data", retData);
		return new JsonRepresentation(ret.toString());
	}

	private Representation getOpenViewPortfolioAuthors() throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("OpenViewAuthors");
		List<String> allOpenViewAuthor = viewPorService
				.getAllOpenViewPorAuthor();
		User curUser = getUser();
		List<Object> retData = new ArrayList<Object>();
		for (String loginName : allOpenViewAuthor) {
			if (!loginName.equals(curUser.getLoginName())) {
				User user = userService.getUserByLoginName(loginName);
				user.setPassword("");
				retData.add(user);
			}
		}
		ret.put("data", retData);
		ret.put("message", "获取用户成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation save(JSONObject params) throws Exception {
		log.dLog("save");
		JSONObject ret = new JSONObject();
		boolean open = true;
		String content = null;
		String name = null;
		JSONArray viewIds = null;
		try {
			open = params.getBoolean("open");
		} catch (Exception e) {
		}

		try {
			content = params.getString("content");
			name = params.getString("name");
			viewIds = params.getJSONArray("views");
		} catch (Exception e) {
			throw e;
		}

		if (open) {
			checkAuth(ADD, name);
		}

		if (name == null || "".equals(name)) {
			throw new MException("组合视图名称不能为空");
		}

		User user = getUser();
		// TODO 检测视图是否是自己的,不是自己的视图不让建组合视图

		boolean isExist = false;
		if (open) {
			isExist = viewPorService.exist(true, name, null);
		} else {
			isExist = viewPorService.exist(false, name, user.getLoginName());
		}
		if (isExist) {
			throw new MException("组合视图[" + name + "]已存在");
		}
		ViewPortfolio nv = new ViewPortfolio();
		nv.setContent(content);
		nv.setName(name);
		nv.setOpen(open);
		nv.setTime(new Date().getTime());
		nv.setUserName(user.getLoginName());
		ViewPortfolio save = viewPorService.save(nv);

		// 保存视图与组合视图的关系
		viewRelToProtService.save(save.getId(), viewIds);
		// 订阅该组合视图
		subViewPortService.save(save.getId(), user.getLoginName());

		ret.put("message", "保存视图分类[" + name + "]成功");
		ret.put("data", save.asMap());

		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unchecked")
	private Representation edit(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("edit");
		String id = null;
		String name = null;
		String content = null;
		boolean open = true;
		JSONArray viewIds = null;
		try {
			open = params.getBoolean("open");
		} catch (Exception e) {
		}

		try {
			id = params.getString("id");
			name = params.getString("name");
			content = params.getString("content");
			viewIds = params.getJSONArray("views");
		} catch (Exception e) {
			throw e;
		}
		// TODO 检测视图是否是自己的,不是自己的视图不让建组合视图

		if (open)
			checkAuth(UPDATE, name);

		ViewPortfolio viewp = viewPorService.getById(id);
		if (viewp == null) {
			throw new Exception("组合视图不存在");
		}
		viewp.setContent(content);
		viewp.setName(name);
		viewp.setTime(new Date().getTime());
		// viewp.setOpen(open);
		ViewPortfolio update = viewPorService.update(viewp);
		// 判断哪些视图是新增的.新增加的需要让他人订阅.
		if (viewp.isOpen()) {
			List<String> oldRelViewIds = viewRelToProtService
					.getViewIdByProtfolio(update.getId());
			List<String> subers = subViewPortService
					.getSubscribersByViewPortfolioId(update.getId());

			for (Object object : viewIds) {
				String newViewId = (String) object;
				if (!oldRelViewIds.contains(newViewId)) {
					for (String username : subers) {
						subViewService.save(username, newViewId);
					}
				}
			}
		}
		// 更新视图与组合视图的关系
		viewRelToProtService.update(update.getId(), viewIds);

		ret.put("data", update.asMap());
		ret.put("message", "更新组合视图[" + update.getName() + "成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 将私有视图变为共有视图
	 * 
	 * @param viewPId
	 * @return
	 * @throws Exception
	 */
	private Representation openView(String viewPId) throws Exception {
		JSONObject ret = new JSONObject();
		ViewPortfolio vPInfo = null;
		vPInfo = viewPorService.getById(viewPId);
		if (vPInfo == null) {
			throw new MException("组合视图不存在!");
		}

		vPInfo.setOpen(true);
		viewPorService.update(vPInfo);
		// 将私有的视图公开.
		List<String> viewIds = viewRelToProtService.getViewIdByProtfolio(vPInfo
				.getId());
		List<ViewInformation> views = vInfoService.getByids(viewIds);
		for (ViewInformation vInfo : views) {
			if (!vInfo.getOpen()) {
				vInfo.setOpen(true);
				vInfoService.save(vInfo);
			}
		}
		ret.put("message", "分享成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 将共有视图变为私有的视图
	 * 
	 * @param viewId
	 * @return
	 * @throws MException
	 */
	private Representation closeView(String viewId) throws Exception {
		JSONObject ret = new JSONObject();
		ViewPortfolio vInfo = null;
		vInfo = viewPorService.getById(viewId);
		if (vInfo == null) {
			throw new MException("组合视图不存在!");
		}
		vInfo.setOpen(false);
		viewPorService.update(vInfo);
		subViewPortService.delForCloseView(viewId, getUser().getLoginName());
		ret.put("message", "取消分享成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll(Boolean open) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteAll");
		if (open == null) {
			viewPorService.deleteAll();
		} else if (open) {
			viewPorService.deleteAllOpenViewPort();
		} else {
			viewPorService.deleteAllByUser(getUser());
		}
		ret.put("message", "清除成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteById");
		ViewPortfolio view = viewPorService.getById(id);
		if (view == null) {
			throw new Exception("组合视图不存在");
		}
		viewPorService.deleteById(id);
		subViewPortService.del(id, getUser().getLoginName());
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 判断用户是否有操作该目录的权限
	 * 
	 * @param method
	 *            上面定义的几个final 变量 DELETE,ADD,UPDATE,SEE
	 * @param viewCateName
	 *            要添加的视图名称
	 * @return
	 */
	private void checkAuth(int method, String viewCateName) throws Exception {
		if (method == 100) {
			log.eLog("权限不足");
			throw new MException("权限不足");
		}
	}
}
