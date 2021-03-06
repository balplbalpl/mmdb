package com.mmdb.rest.category;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.IViewCateService;
import com.mmdb.service.info.IViewInfoService;

/**
 * 对外提供的View视图分类接口类
 * 
 * @author XIE
 */
public class ViewCateRest extends BaseRest {
	// private Log log = LogFactory.getLogger("ViewCateRest");
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

	private IViewCateService vCateService;
	private IViewInfoService vInfoService;

	@Override
	public void ioc(ApplicationContext context) {
		vCateService = context.getBean(IViewCateService.class);
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
			return getAll(true);
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return save(params);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
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
		ViewCategory viewCate = vCateService.getById(id);
		if (viewCate != null) {
			ret.put("data", aggregation(viewCate));
			ret.put("message", "获取视图分类成功");
		} else {
			throw new MException("视图分类未找到");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getAll(Boolean open) throws Exception {
		log.dLog("getAll");
		JSONObject ret = new JSONObject();
		List<ViewCategory> list = null;
		if (open == null) {
			list = vCateService.getAll();
		} else if (open) {
			list = vCateService.getAllOpenViewCate();
		} else {
			list = vCateService.getAllByUser(getUser());
		}
		JSONArray data = new JSONArray();
		for (ViewCategory viewCategory : list) {
			data.add(aggregation(viewCategory));
		}

		ret.put("data", data);
		ret.put("message", "获取视图分类成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation save(JSONObject params) throws Exception {
		log.dLog("save");
		JSONObject ret = new JSONObject();
		boolean open = false;
		String parentId = null;
		String cateName = null;
		try {
			open = params.getBoolean("open");
			parentId = params.getString("parent");
			cateName = params.getString("name");
		} catch (Exception e) {
			throw e;
		}

		if (open) {
			checkAuth(ADD, cateName);
		}

		if (cateName == null || "".equals(cateName)) {
			throw new MException("视图分类名称不能为空");
		}

		User user = getUser();
		ViewCategory nCategory = null;
		if (parentId == null || parentId.equals("")) {
			if (vCateService.exist(open, user.getLoginName(), cateName,
					parentId)) {
				throw new MException("视图分类[" + cateName + "]已存在");
			}
			nCategory = new ViewCategory(null, cateName, user.getLoginName(),
					open);
		} else {
			ViewCategory category = vCateService.getById(parentId);
			if (category == null) {
				throw new MException("父类[" + parentId + "]不存在");
			}
			List<ViewCategory> childrens = category.getAllChildren();
			for (ViewCategory viewCategory : childrens) {
				if (viewCategory.getName().equals(cateName)) {
					throw new MException("视图分类[" + cateName + "]已存在");
				}
			}
			nCategory = new ViewCategory(null, cateName, user.getLoginName(),
					open, category);
		}
		nCategory.setCreateTime(System.currentTimeMillis());
		nCategory.setUpdateTime(nCategory.getCreateTime());

		ViewCategory nc = vCateService.save(nCategory);
		ret.put("message", "保存视图分类[" + cateName + "]成功");
		ret.put("data", nc.asMapForRest());

		return new JsonRepresentation(ret.toString());
	}

	private Representation edit(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("edit");
		String cateId = null;
		String name = null;
		String parent = null;
		boolean open = false;
		try {
			cateId = params.getString("id");
			name = params.getString("name");
			parent = params.getString("parent");
		} catch (Exception e) {
			throw e;
		}
		ViewCategory vCategory = null;
		List<ViewCategory> allCate = vCateService.getAll();

		ViewCategory parentCate = null;
		for (ViewCategory viewCategory : allCate) {
			if (viewCategory.getId().equals(cateId)) {
				vCategory = viewCategory;
			}
			if (viewCategory.getId().equals(parent)) {
				parentCate = viewCategory;
			}
		}
		open = vCategory.getOpen();

		if (open)
			checkAuth(UPDATE, name);

		if (vCategory.getParent() != null) {
			if (vCategory.getParent().getId().equals(parent)) {
				// 父亲没改变
				if (!vCategory.getName().equals(name)) {
					List<ViewCategory> children = vCategory.getParent()
							.getChildren();
					int i = 0;
					for (ViewCategory viewCategory : children) {
						if (viewCategory.getName().equals(name)) {
							i++;
						}
					}
					if (i > 1) {
						throw new MException("视图分类[" + name + "]已存在");
					}
				}
			} else {
				// 父亲改变
				if (parentCate != null) {
					List<ViewCategory> children = parentCate.getChildren();
					int i = 0;
					for (ViewCategory viewCategory : children) {
						if (viewCategory.getName().equals(name)) {
							i++;
						}
					}
					if (i > 1) {
						throw new MException("视图分类[" + name + "]已存在");
					}
				}
				vCategory.setParent(parentCate);
			}
		} else {
			if (parentCate != null) {
				List<ViewCategory> children = parentCate.getChildren();
				int i = 0;
				for (ViewCategory viewCategory : children) {
					if (viewCategory.getName().equals(name)) {
						i++;
					}
				}
				if (i > 1) {
					throw new MException("视图分类[" + name + "]已存在");
				}
				vCategory.setParent(parentCate);
			} else if (!vCategory.getName().equals(name))
				for (ViewCategory viewCategory : allCate) {
					if (viewCategory.getParent() == null
							&& viewCategory.getName().equals(name)) {
						throw new MException("视图分类[" + name + "]已存在");
					}
				}
		}

		vCategory.setName(name);
		vCategory.setUpdateTime(System.currentTimeMillis());
		ViewCategory update = vCateService.update(vCategory);
		ret.put("data", update.asMapForRest());
		ret.put("message", "更新分类[" + update.getName() + "成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll(Boolean open) {
		JSONObject ret = new JSONObject();
		ret.put("message", "不支持此功能");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteById(String cateId) throws Exception {
		log.dLog("deleteById");
		JSONObject ret = new JSONObject();
		ViewCategory viewCate = vCateService.getById(cateId);
		String name = viewCate.getName();
		List<String> viewIds = vInfoService.getViewIdsByCate(viewCate.getId());
		if (viewIds != null && viewIds.size() > 0) {
			throw new MException("此分类有视图,请先删除视图!");
		}

		List<ViewCategory> children = viewCate.getChildren();
		if (children != null && children.size() != 0) {
			throw new MException("此分类有继承的子类,需要先删除子类");
		}

		vCateService.deleteById(cateId);
		ret.put("message", "删除分类[" + name + "]成功");
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

		if (method == ADD || method == DELETE || method == UPDATE) {
			User user = getUser();
			String ownerRoles = user.getOwnerRoles();
			String[] roles = ownerRoles.split(",");
			boolean flag = false;
			for (String role : roles) {
				if ("管理员".equals(role)) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				throw new MException("权限不足");
			}
			return;
		}
		if (method == 100) {
			log.eLog("权限不足");
			throw new MException("权限不足");
		}
	}

	private Map<String, Object> aggregation(ViewCategory cate) {
		Map<String, Object> asMap = cate.asMapForRest();
		int count = vInfoService.getViewCountByCategory(cate.getId());
		asMap.put("viewCount", count);
		return asMap;
	}
}
