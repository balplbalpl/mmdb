package com.mmdb.rest.info;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.common.MyException;
import com.mmdb.core.utils.MD5;
import com.mmdb.core.utils.ProjectInfo;
import com.mmdb.core.utils.SvgUtil;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.event.Event;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.category.IViewCateService;
import com.mmdb.service.event.IEventService;
import com.mmdb.service.event.IEventViewService;
import com.mmdb.service.icon.IViewIconService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.service.performance.impl.PerformanceService;
import com.mmdb.service.relation.ICiKpiRelService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.service.subscription.ISubscriptionService;
import com.mmdb.util.FileManager;
import com.mmdb.util.HexString;

public class ViewInfoRest extends BaseRest {
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

	private IViewCateService cateService;

	private IViewInfoService infoService;

	private ICiInfoService ciInfoService;

	private ICiViewRelService ciViewRelService;

	private IViewCateService viewCateService;

	private IEventService eventService;

	private IEventViewService eventViewService;

	private IPerformanceService performanceService;

	private IViewIconService iconService;

	// 用于查询ci相关的kpi信息
	private ICiKpiRelService ciKpiRel;

	private IKpiCateService kpiCateService;

	// 订阅管理
	private ISubscriptionService subscription;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(IViewCateService.class);
		infoService = context.getBean(IViewInfoService.class);
		ciInfoService = context.getBean(ICiInfoService.class);
		ciViewRelService = context.getBean(ICiViewRelService.class);
		eventService = context.getBean(IEventService.class);
		eventViewService = context.getBean(IEventViewService.class);
		iconService = context.getBean(IViewIconService.class);
		ciKpiRel = context.getBean(ICiKpiRelService.class);
		kpiCateService = context.getBean(IKpiCateService.class);
		subscription = context.getBean(ISubscriptionService.class);
		performanceService = new PerformanceService();
	}

	@Override
	public Representation getHandler() {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		if (param1 == null || "".equals(param1)) {
			return getAll(null);
		} else if ("private".equals(param1)) {
			return getAll(false);
		} else if ("public".equals(param1)) {
			if (param2 == null || param2.equals("")) {
				return getOpenViewAuthors();
			} else {
				return getOpenViewByAuthor(param2);
			}
		} else if ("threshold".equals(param1)) {
			return getThresholdByView(param2);
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		JSONObject params = parseEntity(entity);
		if ("exportpng".equals(param1)) {
			return exportPng(params);
		} else if ("threshold".equals(param1)) {
			return getThresholdByView(param2, params);
		} else if ("performance".equals(param1)) {
			return getPerformanceByView(param2, params);
		} else if ("exportpdf".equals(param1)) {
			return exportPdf(params);
		} else if ("allpaths".equals(param1)) {
			String source = params.getString("source");
			String target = params.getString("target");
			JSONArray data = params.getJSONArray("data");

			return allpaths(source, target, data);
		} else if ("fuzzybackground".equals(param1)) {
			return queryBackground(params);
		} else if ("copy".equals(param1)) {
			return copy(params);
		} else if ("fuzzysearch".equals(param1)) {
			String keyWord = null;
			try {
				keyWord = params.getString("keyWord");
			} catch (Exception e) {
			}
			return queryViewOrCiFuzzy(keyWord);
		} else if ("getmonitorforview".equals(param1)) {
			return getMonitorForView(params);
		} else if ("gettimemachineforview".equals(param1)) {
			return getTimeMachineForView(params);
		} else if ("getwholeinfo".equals(param1)) {
			return getWholeInfo(params);
		} else if ("softdelete".equals(param1)) {
			return getSoftDeleteView(params);
		} else if ("cikpi".equals(param1)) {
			return getKpiByView(param2, params);
		} else if ("event".equals(param1)) {
			return getEventByCiKpi(param2, params);
		} else if ("subscriptions".equals(param1)) {
			return getSubscriptionView(params);
		} else {
			if (params.containsKey("pageSize") && params.containsKey("cateId")
					&& params.containsKey("all") && params.containsKey("page")) {
				String cateId = params.getString("cateId");
				JSONObject orExp = null;
				try {
					orExp = params.getJSONObject("like");
				} catch (Exception e) {
				}
				boolean all = params.getBoolean("all");
				int page = params.getInt("page");
				int pageSize = params.getInt("pageSize");
				return query(cateId, orExp, all, page, pageSize);
			} else if (params.containsKey("pageSize")
					&& params.containsKey("keyWord")
					&& params.containsKey("page")) {
				String keyWord = params.getString("keyWord");
				int page = params.getInt("page");
				int pageSize = params.getInt("pageSize");
				return queryFuzzy(keyWord, page, pageSize);
			} else if (params.size() == 1 && params.containsKey("viewIds")) {
				JSONArray ids = params.getJSONArray("viewIds");
				return getByIds(ids);
			}
			return save(params);
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		JSONObject params = parseEntity(entity);
		if ("refresh".equals(param1)) {
			return refreshSvg();
		}
		if ("threshold".equals(param1)) {
			return putThresholdByView(param2, params.getJSONArray("data"));
		} else if ("restoresoftview".equals(param1)) {
			return restoreSoftDelete(param2, params);
		}
		return edit(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		if (param1 == null || "".equals(param1)) {
			return deleteAll(null);
		} else if ("private".equals(param1)) {
			return deleteAll(false);
		} else if ("public".equals(param1)) {
			return deleteAll(true);
		} else if ("background".equals(param1)) {
			return deleteBackground(param2);
		} else {
			return deleteById(param1);
		}
	}

	private Representation getById(String id) {
		JSONObject ret = new JSONObject();
		log.dLog("getById");
		try {
			checkAuth(SEE, null);
			ViewInformation info = infoService.getById(id);
			if (info != null) {
				ret.put("data", aggregation(info, null));
				ret.put("message", "获取视图成功");
			} else {
				ret.put("message", "获取视图失败");
				getResponse().setStatus(new Status(404));
			}
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", "获取视图失败");
			getResponse().setStatus(new Status(404));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByIds(List<String> ids) {
		JSONObject ret = new JSONObject();
		log.dLog("getById");
		try {
			checkAuth(SEE, null);
			List<ViewInformation> infos = infoService.getByids(ids);
			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
			User user = getUser();
			for (ViewInformation info : infos) {
				if (info != null) {
					retData.add(aggregation(info, user));
				}
			}
			ret.put("data", retData);
			ret.put("message", "获取视图成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", "获取视图失败");
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getAll(Boolean open) {
		log.dLog("getAll");
		JSONObject ret = new JSONObject();
		try {
			List<ViewInformation> list = null;
			if (open == null || open) {
				ret.put("message", "暂不支持");
				return new JsonRepresentation(ret.toString());
			} else {
				list = infoService.getAllPrivateViewByUser(getUser());
			}
			User user = getUser();
			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
			for (ViewInformation vInfo : list) {
				retData.add(aggregation(vInfo, user));
			}
			ret.put("data", retData);
			ret.put("message", "获取视图分类成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", "获取视图分类失败");
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unchecked")
	private Representation save(JSONObject params) {
		log.dLog("save");
		JSONObject ret = new JSONObject();
		boolean open = false;
		String oldId = null;
		String cateId = null;
		String svg = null;
		String name = null;
		String xml = null;
		String points = null;
		String des = null;
		JSONArray ciIds = null;// 为ciHexid(十六进制)
		try {
			oldId = params.getString("id");
		} catch (Exception e) {
		}
		try {
			cateId = params.getString("categoryId");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[categoryId]不存在");
		}
		try {
			name = params.getString("name");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[name]不存在");
		}
		try {
			svg = params.getString("svg");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[svg]不存在");
		}
		try {
			xml = params.getString("xml");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[xml]不存在");
		}
		try {
			points = params.getString("points");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[points]不存在");
		}
		try {
			des = params.getString("description");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[des]不存在");
		}
		try {
			ciIds = params.getJSONArray("cis");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[cis]不存在");
		}

		if (ret.containsKey("message")) {
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}

		try {
			ViewCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new Exception("视图分类[" + cateId + "]不存在");
			}
			open = nc.getOpen();

			if (nc.getOpen()) {
				checkAuth(ADD, name);
			}

			if (name == null || "".equals(name)) {
				throw new Exception("视图名称不能为空");
			}

			if (infoService.exist(cateId, name)) {
				throw new Exception("视图[" + name + "]已经存在");
			}
			String Hexname = HexString.encode(HexString.json2Str(nc.getName(),
					name));
			String path = infoService.createSvg(Hexname, svg);

			ViewInformation view = new ViewInformation(nc, cateId, null, name,
					xml, svg, points,
					path.substring(path.lastIndexOf("/") + 1), open, des,
					System.currentTimeMillis(), 0);
			User user = getUser();
			String username = user.getLoginName();
			view.setUserName(username);
			view.setOpen(open);
			view.setUpdateTime(view.getCreateTime());
			if (oldId != null) {
				ViewInformation info = infoService.getById(oldId);
				if (info == null) {
					throw new Exception("原视图不存在,另存为失败");
				}
				if (!info.getUserName().equals(username)) {
					copyIcon(view, info.getUserName(), username);
				}
			}

			view = infoService.save(view);

			// ci与视图的关系保存
			String id = view.getId();
			ciViewRelService.save(id, ciIds);

			if (oldId != null) {
				List<Map<String, Object>> thresholdByView = subscription
						.getThresholdByView(oldId, username);
				for (Map<String, Object> map : thresholdByView) {
					map.put("viewId", id);
					map.put("viewAuthor", username);
				}
				subscription.asSave(username, id, thresholdByView);
			} else {
				// 产生订阅
				// 读ci,读kpi读阈值
				subscription.save(username, view);
			}
			ret.put("message", "保存视图[" + name + "]成功");
			ret.put("data", aggregation(view, user));
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	private Representation copy(JSONObject params) {
		JSONObject ret = new JSONObject();
		log.dLog("copy");
		String newName = null;
		String cateId = null;
		String id = null;
		try {
			newName = params.getString("newName");
		} catch (Exception e) {
		}
		try {
			cateId = params.getString("categoryId");
		} catch (Exception e) {
			log.eLog("copy", e);
			ret.put("message", "必须参数[categoryId]不存在");
		}
		try {
			id = params.getString("id");
		} catch (Exception e) {
			log.eLog("copy", e);
			ret.put("message", "必须参数[id]不存在");
		}

		try {
			ViewCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new Exception("目标分类不存在");
			}

			ViewInformation info = infoService.getById(id);
			if (info == null) {
				throw new Exception("视图不存在");
			}
			if (newName == null) {
				newName = info.getName();
			}

			ViewInformation exist = infoService.getByName(nc.getId(),
					info.getName());
			if (exist != null) {
				throw new Exception("视图[" + info.getName() + "]在["
						+ nc.getName() + "]中存在");
			}
			if (info.getOpen()) {
				User user = getUser();
				if (!info.getUserName().equals(user.getLoginName())) {
					copyIcon(info, info.getName(), user.getLoginName());
				}
				info.setUserName(user.getLoginName());
				info.setOpen(false);
				info.setId(null);
				ViewInformation view = infoService.save(info);
				List<String> ciIds = ciViewRelService.getByView(info.getId());
				ciViewRelService.save(info.getId(), ciIds);

				// 产生订阅
				// 读ci,读kpi读阈值
				subscription.save(user.getLoginName(), view);

				ret.put("message", "复制成功");
			} else {
				throw new Exception("私有视图无法复制");
			}
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 复制图片,当图形改变的时候
	 * 
	 * @param info
	 */
	@SuppressWarnings("rawtypes")
	private void copyIcon(ViewInformation info, String oldUser, String tUser) {
		String points = info.getPoints();
		String svg = info.getSvg();
		String xml = info.getXml();
		try {
			Map<String, String> replace = replace(svg, points, xml, oldUser,
					tUser);
			info.setPoints(replace.get("points"));
			info.setSvg(replace.get("svg"));
			info.setXml(replace.get("xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Map<String, String> replace(String svg, String points, String xml,
			String oldName, String tName) throws Exception {
		String t = "static/resource/ownsvg/";
		svg = svg.replaceAll(t + oldName, t + tName);
		points = points.replaceAll(t + oldName, t + tName);
		xml = xml.replaceAll(t + oldName, t + tName);
		String tPe = t + tName;
		int index = 0;
		// 图片名称
		List<String> pNames = new ArrayList<String>();
		while (index >= 0) {
			index = svg.indexOf(tPe, index);
			int end = svg.indexOf("\"", index);
			if (end < 0 || index < 0)
				break;
			String pName = svg.substring(index, end);
			int split = pName.indexOf(";", tPe.length());
			if (split > 0) {
				pName = pName.substring(0, split);
			}
			split = pName.lastIndexOf("/");
			pName = pName.substring(split + 1);
			if (!pNames.contains(pName))
				pNames.add(pName);
			index = end;
		}
		// 拷贝图片
		// String oldPath = Tool.getRealPath() + "resource/ownsvg/" + oldName
		// + "/";
		String tPath = Tool.getRealPath() + "resource/ownsvg/" + tName + "/";
		File tF = new File(tPath);
		if (!tF.exists()) {
			tF.mkdirs();
		}
		Map<String, String> map = new HashMap<String, String>();
		for (String pName : pNames) {
			ViewIcon viewIcon = iconService.getByName(pName, oldName);
			ViewIcon ticon = null;
			String nName = pName;
			int tIndex = 0;
			while (true) {
				ticon = iconService.getByName(nName, tName);
				if (ticon != null) {
					if (ticon.getMd5().equals(viewIcon.getMd5())) {
						break;
					}
					String[] split = pName.split("\\.");
					nName = (split[0] + tIndex) + "\\." + split[1];
					map.put(pName, nName);
					ticon = iconService.getByName(pName, nName);
				} else {
					viewIcon.setUsername(tName);
					viewIcon.setName(nName);
					viewIcon.setId(null);
					iconService.save(viewIcon);
					break;
				}
			}

			FileManager.getInstance().copyFile(new File(tF, nName),
					(byte[]) viewIcon.getContent());
		}
		Set<String> keySet = map.keySet();
		for (String p : keySet) {
			svg = svg.replaceAll(t + tName + "/" + p, t + tName + "/" + p);
			points = points
					.replaceAll(t + tName + "/" + p, t + tName + "/" + p);
			xml = xml.replaceAll(t + tName + "/" + p, t + tName + "/" + p);
		}
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("svg", svg);
		ret.put("points", points);
		ret.put("xml", xml);
		return ret;
	}

	private Representation edit(JSONObject params) {
		JSONObject ret = new JSONObject();
		log.dLog("edit");
		boolean open = false;
		String cateId = null;
		String id = null;
		String xml = null;
		String name = null;
		String svg = null;
		String points = null;
		String des = null;
		JSONArray ciIds = null;

		try {
			open = params.getBoolean("open");
		} catch (Exception e) {
		}

		try {
			cateId = params.getString("categoryId");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[categoryId]不存在");
		}

		try {
			id = params.getString("id");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[id]不存在");
		}

		try {
			xml = params.getString("xml");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[xml]不存在");
		}
		try {
			name = params.getString("name");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[name]不存在");
		}
		try {
			points = params.getString("points");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[points]不存在");
		}
		try {
			svg = params.getString("svg");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[svg]不存在");
		}
		try {
			des = params.getString("description");
		} catch (Exception e) {
			log.eLog("edit", e);
			ret.put("message", "必须参数[description]不存在");
		}

		try {
			ciIds = params.getJSONArray("cis");
		} catch (Exception e) {
			log.eLog("save", e);
			ret.put("message", "必须参数[cis]不存在");
		}

		if (ret.containsKey("message")) {
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}

		try {
			ViewInformation info = infoService.getById(id);
			if (info == null) {
				throw new Exception("视图不存在");
			}

			if (!info.getUserName().equals(getUser().getLoginName())) {
				throw new Exception("权限不足");
			}

			if (id == null || "".equals(id)) {
				throw new Exception("视图id不能为空");
			}
			ViewCategory nc = cateService.getById(cateId);

			if (nc == null) {
				throw new Exception("视图分类[" + cateId + "]不存在");
			}

			open = nc.getOpen();
			if (open) {
				checkAuth(UPDATE, id);
			}

			String Hexname = HexString.encode(HexString.json2Str(nc.getName(),
					name));

			String path = infoService.createSvg(Hexname, svg);

			info.setImageUrl(path.substring(path.lastIndexOf("/") + 1));
			if (xml != null && !xml.equals("")) {
				info.setXml(xml);
			}
			if (name != null && !name.equals("")) {
				info.setName(name);
			}
			if (des != null && !des.equals("")) {
				info.setDescription(des);
			}
			if (svg != null && !svg.equals("")) {
				info.setSvg(svg);
			}
			if (points != null && !points.equals("")) {
				info.setPoints(points);
			}
			User user = getUser();
			info.setOpen(open);
			info.setVersion(info.getVersion() + 1);
			info.setUpdateTime(System.currentTimeMillis());
			info.setUserName(user.getLoginName());
			info.setCategory(nc);
			info.setCategoryId(nc.getId());
			info = infoService.update(info);
			// 更新视图与ci的关系
			List<String> oldRelCiIds = ciViewRelService.getByView(info.getId());

			List<String> addCi = new ArrayList<String>();
			List<String> delCi = new ArrayList<String>();
			for (Object obj : ciIds) {
				String newId = (String) obj;
				if (!oldRelCiIds.contains(newId)) {
					addCi.add(newId);
				}
			}
			for (String oldId : oldRelCiIds) {
				if (!ciIds.contains(oldId)) {
					delCi.add(oldId);
				}
			}
			subscription.addCiByView(info, addCi);
			subscription.delCIByView(info.getId(), delCi);

			ciViewRelService.update(info.getId(), ciIds);
			// TODO 删除视图与kpi的关系

			ret.put("message", "更新视图[" + info.getName() + "]成功");
			ret.put("data", aggregation(info, user));
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll(Boolean open) {
		JSONObject ret = new JSONObject();
		log.dLog("deleteAllViewCategory");
		try {

			if (open == null) {
				checkAuth(DELETE, null);
				// infoService.deleteAll();
			} else if (open) {
				checkAuth(DELETE, null);
				// infoService.deleteAllOpenView();
			} else {
				// infoService.deleteAllPrivateViewByUser(getUser());
			}
			ret.put("message", "不支持此功能");
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteById(String id) {
		log.dLog("deleteById");
		JSONObject ret = new JSONObject();
		try {
			ViewInformation byId = infoService.getById(id);
			if (byId == null) {
				new Exception("视图不存在");
			}
			if (byId.getOpen()) {
				checkAuth(DELETE, byId.getName());
			}
			String status = byId.getStatus();
			if (ViewInformation.NORMAL.equals(status)) {
				infoService.softDelete(id);
			} else if (ViewInformation.SOFT_DELETE.equals(status)) {
				infoService.deleteById(id);
			}
			ret.put("message", "删除视图[" + byId.getName() + "]成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportPng(JSONObject params) throws Exception {
		log.dLog("exportPng");
		JSONObject ret = new JSONObject();
		// String viewId = null;
		String svg = null;
		// try {
		// viewId = params.getString("id");
		// } catch (Exception e) {
		// log.eLog("exportPng", e);
		// ret.put("message", "必须参数[id]不存在");
		// }
		try {
			svg = params.getString("svg");
		} catch (Exception e) {
			log.eLog("exportPng", e);
			ret.put("message", "必须参数[svg]不存在");
		}
		if (ret.containsKey("message")) {
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}

		try {
			// ViewInformation info = infoService.getById(viewId);
			// viewId = "视图" + info.getName();
			// svg = info.getSvg();
			// 注：使用的是svg字符串转pdf的情况可能会出现编码错误的异常，就把字符串里的UTF-8替换为GBK
			String url = SysProperties.get("svg.url");
			if (url.equals("resource")) {
				url = "/" + Tool.getRealPath();
				svg = this.regex(url, svg);
			} else {
				svg = this.regex(url, svg);
			}
			InputStream ins = new ByteArrayInputStream(svg.getBytes("utf-8"));
			File destFile = FileManager.getInstance().createFile("视图", "png");
			SvgUtil.convertSvgFile2Png(ins, destFile, 2560, 1920);
			JSONObject retData = new JSONObject();
			retData.put("url", destFile.getName());
			ret.put("data", retData);
			ret.put("message", "导出图片成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", ret);
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportPdf(JSONObject params) throws Exception {
		log.dLog("exportPdf");
		JSONObject ret = new JSONObject();
		String svg = null;
		try {
			svg = params.getString("svg");
		} catch (Exception e) {
			log.eLog("exportPng", e);
			ret.put("message", "必须参数[svg]不存在");
		}
		if (ret.containsKey("message")) {
			getResponse().setStatus(new Status(600));
			return new JsonRepresentation(ret.toString());
		}
		try {
			// 注：使用的是svg字符串转pdf的情况可能会出现编码错误的异常，就把字符串里的UTF-8替换为GBK
			String url = SysProperties.get("svg.url");
			if (url.equals("resource")) {
				url = "/" + Tool.getRealPath();
				svg = this.regex(url, svg);
			} else {
				svg = this.regex(url, svg);
			}

			InputStream ins = new ByteArrayInputStream(svg.getBytes("utf-8"));
			File destFile = FileManager.getInstance().createFile("视图", "pdf");

			SvgUtil.convertSvgFile2Pdf(ins, destFile);

			JSONObject retData = new JSONObject();
			retData.put("url", destFile.getName());
			ret.put("data", retData);
			ret.put("message", "导出pdf成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", ret);
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation queryViewOrCiFuzzy(String keyWord) {
		JSONObject ret = new JSONObject();
		try {
			Map<String, ViewInformation> vInfoCache = new HashMap<String, ViewInformation>();
			log.dLog("queryViewOrCiFuzzy");
			// UserInformation user = this.getSessionUser();
			User user = getUser();
			Map<String, Object> result = new HashMap<String, Object>();
			List<ViewInformation> list = infoService.qureyFuzzy(keyWord, user);
			List<Object> datas = new ArrayList<Object>();
			for (ViewInformation info : list) {
				datas.add(aggregation(info, user));
				vInfoCache.put(info.getId(), info);
			}
			result.put("view", datas);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (keyWord == null || keyWord.length() <= 0) {
				throw new Exception("查询条件不能为空");
			}
			Map<String, String> or = new HashMap<String, String>();
			or.put("*", "*" + keyWord + "*");
			List<CiInformation> clist = ciInfoService.qureyByFuzzy(null, or);
			// 排序
			// Collections.sort(list, new Comparator<CiInformation>() {
			// public int compare(CiInformation arg0, CiInformation arg1) {
			// return arg0.getNeo4jid().compareTo(arg1.getNeo4jid());
			// }
			// });
			// 构建分页数据输出
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
			for (Map<String, String> s : severityList) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("name", s.get("name"));
				m.put("color", s.get("color"));
				severityMap.put(s.get("id"), m);
			}

			List<Object> cdatas = new ArrayList<Object>();
			for (CiInformation info : clist) {
				Map<String, Object> cdata = new HashMap<String, Object>();
				CiCategory cate = info.getCategory();

				JSONArray ciJs = new JSONArray();
				ciJs.add(info.getCategory().getName());
				String ciName = info.getName();
				ciJs.add(ciName);
				String ciHex = HexString.encode(ciJs.toString());

				Map<String, Object> data = info.asMapForRest();
				String prefix = Tool.findPath("graph", "resource");
				String type = SysProperties.get("svg.base");
				data.put("icon",
						prefix + "/svg/" + type + "/" + cate.getImage());
				// CiCategory cate2 = cate.getParent();
				// if (cate2 != null) {
				// CiCategory cate3 = cate2.getParent();
				// if (cate3 != null) {
				// data.put("一级分类", cate3.getId());
				// data.put("二级分类", cate2.getId());
				// data.put("三级分类", cate.getId());
				// } else {
				// data.put("一级分类", cate2.getId());
				// data.put("二级分类", cate.getId());
				// data.put("三级分类", "");
				// }
				// } else {
				// data.put("一级分类", cate.getId());
				// data.put("二级分类", "");
				// data.put("三级分类", "");
				// }
				cdata.put("config", data);

				Map<Integer, List<PerformanceBean>> perfMap = performanceService
						.getAllPerformanceDatasByInstance("", info.getId(), "",
								"", "", "", "", 0, 300);
				Map<Map<String, String>, Map<String, String>> tmpMap = new HashMap<Map<String, String>, Map<String, String>>();
				if (perfMap != null) {
					Set<Integer> perfSet = perfMap.keySet();
					if (perfSet.size() == 1) {
						for (Integer i : perfSet) {
							if (i > 0) {
								List<PerformanceBean> perfList = perfMap.get(i);
								for (int j = perfList.size() - 1; j >= 0; j--) {
									PerformanceBean pb = perfList.get(j);
									Map<String, String> indi = new HashMap<String, String>();
									indi.put(
											"kpiCate",
											pb.getKpiCate() == null ? "" : pb
													.getKpiCate());
									indi.put(
											"kpi",
											pb.getKpiName() == null ? "" : pb
													.getKpiName());
									indi.put(
											"instance",
											pb.getInstance() == null ? "" : pb
													.getInstance());
									Map<String, String> tmp = tmpMap.get(indi);
									if (tmp == null) {
										tmp = new HashMap<String, String>();
										tmp.put("val", pb.getValue());
										tmp.put("time", pb.getStartTime());
									} else {
										if (sdf.parse(pb.getStartTime())
												.getTime() > sdf.parse(
												tmp.get("time")).getTime()) {
											tmp.put("val", pb.getValue());
											tmp.put("time", pb.getStartTime());
										}
									}
									tmpMap.put(indi, tmp);
								}
							}
							break;
						}
					}
				}
				List<Map<String, String>> perfs = new ArrayList<Map<String, String>>();
				Set<Map<String, String>> tmpSet = tmpMap.keySet();
				for (Map<String, String> indi : tmpSet) {
					Map<String, String> val = tmpMap.get(indi);
					Map<String, String> perf = new HashMap<String, String>();
					perf.put("指标类别", indi.get("kpiCate"));
					perf.put("指标", indi.get("kpi"));
					perf.put("实例", indi.get("instance"));
					perf.put("当前值", val.get("val"));
					perf.put("时间", val.get("time"));
					perfs.add(perf);
				}
				Map<String, Object> p = new HashMap<String, Object>();
				List<String> perfTitle = new ArrayList<String>();
				perfTitle.add("指标类别");
				perfTitle.add("指标");
				perfTitle.add("实例");
				perfTitle.add("当前值");
				perfTitle.add("时间");
				p.put("head", perfTitle);
				p.put("row", perfs);
				cdata.put("perf", p);

				List<String> cis = new ArrayList<String>();
				cis.add(ciHex);
				Long time = System.currentTimeMillis();
				List<Event> events = eventService.getEventForView(cis, "", "",
						time, time);
				List<Map<String, String>> evList = new ArrayList<Map<String, String>>();
				for (Event ev : events) {
					Map<String, String> m = new HashMap<String, String>();
					m.put("CI名称", ciName);
					m.put("级别", severityMap.get(ev.getSeverity()).get("name"));
					m.put("详细信息", ev.getSummary());
					m.put("时间", sdf.format(new Date(ev.getFirstOccurrence())));
					evList.add(m);
				}
				Map<String, Object> e = new HashMap<String, Object>();
				List<String> eventTitle = new ArrayList<String>();
				eventTitle.add("CI名称");
				eventTitle.add("级别");
				eventTitle.add("详细信息");
				eventTitle.add("时间");
				e.put("head", eventTitle);
				e.put("row", evList);
				cdata.put("event", e);

				// 添加ci的相关视图
				List<String> viewIds = this.ciViewRelService.getByCi(ciHex);
				// 处理缓存
				List<String> qIds = new ArrayList<String>();
				List<ViewInformation> ciView = new ArrayList<ViewInformation>();
				for (String viewId : viewIds) {
					ViewInformation viewInformation = vInfoCache.get(viewId);
					if (viewInformation == null) {
						qIds.add(viewId);
					}
				}
				if (qIds.size() != 0) {
					List<ViewInformation> byids = this.infoService
							.getByids(qIds);
					for (ViewInformation vInfo : byids) {
						vInfoCache.put(vInfo.getId(), vInfo);
					}
				}
				for (String viewId : viewIds) {
					ciView.add(vInfoCache.get(viewId));
				}
				cdata.put("ciViews", ciView);

				cdatas.add(cdata);
			}
			result.put("ci", cdatas);

			ret.put("data", result);
			ret.put("message", "查询成功");
			log.dLog("queryViewOrCiFuzzy success");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", ret);
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getMonitorForView(JSONObject params) {
		log.dLog("getMonitorForView");
		JSONObject ret = new JSONObject();
		try {
			JSONArray ids = params.getJSONArray("ids");
			Long startTime = 0l;
			Long endTime = System.currentTimeMillis();
			/*
			 * Long startTime = params.containsKey("startTime") &&
			 * params.getString("startTime").length() > 0 ? Long
			 * .parseLong(params.getString("startTime")) * 1000L : 0L; Long
			 * endTime = params.containsKey("endTime") &&
			 * params.getString("endTime").length() > 0 ? Long
			 * .parseLong(params.getString("endTime")) * 1000L : System
			 * .currentTimeMillis();
			 */
			if (params.containsKey("startTime")
					&& params.getString("startTime").length() > 0
					&& !params.getString("startTime").equals("null")) {
				startTime = Long.parseLong(params.getString("startTime")) * 1000L;
			}

			if (params.containsKey("endTime")
					&& params.getString("endTime").length() > 0
					&& !params.getString("endTime").equals("null")) {
				endTime = Long.parseLong(params.getString("endTime")) * 1000L;
			}

			User user = this.getUser();
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
			for (Map<String, String> s : severityList) {
				severityMap.put(s.get("id"), s);
			}

			Map<String, List<Map<String, Object>>> retMap = new HashMap<String, List<Map<String, Object>>>();
			for (int i = 0; i < ids.size(); i++) {
				ViewInformation info = infoService.getById(ids.getString(i));
				if (info == null) {
					new Exception("视图不存在");
				}
				List<String> ciHexIds = ciViewRelService.getByView(ids
						.getString(i));
				List<Event> eventList = eventService.getEventForView(ciHexIds,
						info.getId(), user.getLoginName(), startTime, endTime);
				Map<String, Map<String, Object>> eventMap = new HashMap<String, Map<String, Object>>();
				for (Event event : eventList) {
					Long severity = Long.parseLong(event.getSeverity());
					if (severity > 0) {
						String hexId = event.getCiHex();
						Map<String, Object> alarmMap = eventMap.get(hexId);
						if (alarmMap == null) {
							alarmMap = new HashMap<String, Object>();
							alarmMap.put("alarm", "0");

							alarmMap.put("count", 0L);
							eventMap.put(hexId, alarmMap);
						}
						if (severity > Long.parseLong(alarmMap.get("alarm")
								+ "")) {
							alarmMap.put("alarm", severity + "");
						}
						alarmMap.put("count", (Long) alarmMap.get("count") + 1L);
					}
				}
				List<Map<String, Object>> rl = new ArrayList<Map<String, Object>>();
				Set<String> eventSet = eventMap.keySet();
				for (String key : eventSet) {
					Map<String, Object> m = eventMap.get(key);
					m.put("alarm", m.get("alarm"));
					m.put("count", m.get("count"));
					m.put("color", severityMap.get(m.get("alarm")).get("color"));
					m.put("alarmName",
							severityMap.get(m.get("alarm")).get("name"));
					m.put("id", key);
					rl.add(m);
				}
				retMap.put(ids.getString(i), rl);
			}

			ret.put("message", "查询监控成功");
			ret.put("data", retMap);
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取所有有共有视图的作者
	 * 
	 * @param username
	 * @return
	 */
	private Representation getOpenViewByAuthor(String username) {
		JSONObject ret = new JSONObject();
		try {
			User user = userService.getUserByLoginName(username);
			if (user == null) {
				throw new Exception("用户不存在");
			}
			// 当前订阅的视图

			List<ViewInformation> list = infoService
					.getOpenViewByUser(username);
			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
			for (ViewInformation viewInformation : list) {
				retData.add(aggregation(viewInformation, user));
			}
			ret.put("message", "获取视图成功");
			ret.put("data", retData);
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取当前视图的指定时间段的告警信息
	 * 
	 * @param params
	 * @return
	 */
	private Representation getTimeMachineForView(JSONObject params) {
		log.dLog("getTimeMachineForView");
		JSONObject ret = new JSONObject();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			User user = this.getUser();
			// Long dotTotal = params.getLong("dotTotal");
			// Long startTime = params.containsKey("startTime")
			// && params.getString("startTime").length() > 0 ?
			// Long.parseLong(params.getString("startTime"))*1000L : 0L;
			// Long endTime = params.containsKey("endTime")
			// && params.getString("endTime").length() > 0 ?
			// Long.parseLong(params.getString("endTime"))*1000L : System
			// .currentTimeMillis();
			JSONArray timeDots = params.getJSONArray("timeDots");
			Long dotTotal = new Long(timeDots.size());
			Long startTime = timeDots.getLong(0) * 1000L;
			Long endTime = timeDots.getLong(timeDots.size() - 1) * 1000L;
			if (endTime <= startTime || dotTotal <= 1) {
				throw new Exception("时间参数错误");
			}
			Long scale = (endTime - startTime) / (dotTotal - 1L);
			startTime = startTime - scale;
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
			for (Map<String, String> s : severityList) {
				severityMap.put(s.get("id"), s);
			}

			JSONArray ids = params.getJSONArray("ids");
			Map<Integer, Long> eventMap = new HashMap<Integer, Long>();
			for (int index = 0; index < ids.size(); index++) {
				String id = ids.getString(index);
				ViewInformation info = infoService.getById(id);
				if (info == null) {
					throw new Exception("视图不存在");
				}
				List<String> ciHexIds = ciViewRelService.getByView(id);
				List<Event> eventList = eventService.getEventForView(ciHexIds,
						info.getId(), user.getLoginName(), startTime, endTime);

				for (Event event : eventList) {
					Long severity = Long.parseLong(event.getSeverity());
					if (severity > 0) {
						for (int i = 0; i < dotTotal; i++) {
							Long st = i == 0 ? startTime : timeDots
									.getLong(i - 1) * 1000L;
							Long et = timeDots.getLong(i) * 1000L;
							if (event.getFirstOccurrence() <= et
									&& (event.getCloseTime() == null || event
											.getCloseTime() > st)) {
								Long emSeverity = eventMap.get(i) == null ? 0L
										: eventMap.get(i);
								if (emSeverity < severity) {
									eventMap.put(i, severity);
								}
							}
						}
					}
				}
			}

			List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < dotTotal; i++) {
				Map<String, Object> dotMap = new HashMap<String, Object>();
				dotMap.put("time", timeDots.getLong(i));
				dotMap.put("alarm",
						eventMap.get(i) == null ? 0L : eventMap.get(i));
				dotMap.put(
						"color",
						(Long) dotMap.get("alarm") > 0 ? severityMap.get(
								dotMap.get("alarm") + "").get("color")
								: "#00FF00");
				retList.add(dotMap);
			}
			ret.put("message", "查询监控成功");
			ret.put("data", retList);
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 取ci的信息配置告警和性能.
	 * 
	 * @param params
	 * @return
	 */
	private Representation getWholeInfo(JSONObject params) {
		log.dLog("getWholeInfo");
		JSONObject ret = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			String hexId = params.getString("id");
			CiInformation info = ciInfoService.getById(hexId);
			if (info == null) {
				new Exception("CI不存在");
			}
			String viewId = params.getString("viewId");
			User user = this.getUser();
			Map<String, Object> cdata = new HashMap<String, Object>();
			CiCategory cate = info.getCategory();
			Map<String, Object> data = info.asMapForRest();
			String prefix = Tool.findPath("graph", "resource");
			String type = SysProperties.get("svg.base");
			data.put("icon", prefix + "/svg/" + type + "/" + cate.getImage());
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
			for (Map<String, String> s : severityList) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("name", s.get("name"));
				m.put("color", s.get("color"));
				severityMap.put(s.get("id"), m);
			}
			// CiCategory cate2 = cate.getParent();
			// if (cate2 != null) {
			// CiCategory cate3 = cate2.getParent();
			// if (cate3 != null) {
			// data.put("一级分类", cate3.getId());
			// data.put("二级分类", cate2.getId());
			// data.put("三级分类", cate.getId());
			// } else {
			// data.put("一级分类", cate2.getId());
			// data.put("二级分类", cate.getId());
			// data.put("三级分类", "");
			// }
			// } else {
			// data.put("一级分类", cate.getId());
			// data.put("二级分类", "");
			// data.put("三级分类", "");
			// }
			cdata.put("config", data);

			Long startTime = 0l;
			Long endTime = System.currentTimeMillis();
			/*
			 * Long startTime = params.containsKey("startTime") &&
			 * params.getString("startTime").length() > 0 ? Long
			 * .parseLong(params.getString("startTime")) * 1000L : 0L; Long
			 * endTime = params.containsKey("endTime") &&
			 * params.getString("endTime").length() > 0 ? Long
			 * .parseLong(params.getString("endTime")) * 1000L : System
			 * .currentTimeMillis();
			 */
			if (params.containsKey("startTime")
					&& params.getString("startTime").length() > 0
					&& !params.getString("startTime").equals("null")) {
				startTime = Long.parseLong(params.getString("startTime")) * 1000L;
			}

			if (params.containsKey("endTime")
					&& params.getString("endTime").length() > 0
					&& !params.getString("endTime").equals("null")) {
				endTime = Long.parseLong(params.getString("endTime")) * 1000L;
			}
			Map<Integer, List<PerformanceBean>> perfMap = performanceService
					.getAllPerformanceDatasByInstance("", info.getId(), "", "",
							"", sdf.format(new Date(startTime)),
							sdf.format(new Date(endTime)), 0, 300);
			Map<Map<String, String>, Map<String, String>> tmpMap = new HashMap<Map<String, String>, Map<String, String>>();
			if (perfMap != null) {
				Set<Integer> perfSet = perfMap.keySet();
				if (perfSet.size() == 1) {
					for (Integer i : perfSet) {
						if (i > 0) {
							List<PerformanceBean> perfList = perfMap.get(i);
							for (int j = perfList.size() - 1; j >= 0; j--) {
								PerformanceBean pb = perfList.get(j);
								Map<String, String> indi = new HashMap<String, String>();
								indi.put(
										"kpiCate",
										pb.getKpiCate() == null ? "" : pb
												.getKpiCate());
								indi.put("kpi", pb.getKpiName() == null ? ""
										: pb.getKpiName());
								indi.put(
										"instance",
										pb.getInstance() == null ? "" : pb
												.getInstance());
								Map<String, String> tmp = tmpMap.get(indi);
								if (tmp == null) {
									tmp = new HashMap<String, String>();
									tmp.put("val", pb.getValue());
									tmp.put("time", pb.getStartTime());
								} else {
									if (sdf.parse(pb.getStartTime()).getTime() > sdf
											.parse(tmp.get("time")).getTime()) {
										tmp.put("val", pb.getValue());
										tmp.put("time", pb.getStartTime());
									}
								}
								tmpMap.put(indi, tmp);
							}
						}
						break;
					}
				}
			}
			List<Map<String, String>> perfs = new ArrayList<Map<String, String>>();
			Set<Map<String, String>> tmpSet = tmpMap.keySet();
			for (Map<String, String> indi : tmpSet) {
				Map<String, String> val = tmpMap.get(indi);
				Map<String, String> perf = new HashMap<String, String>();
				perf.put("指标类别", indi.get("kpiCate"));
				perf.put("指标", indi.get("kpi"));
				perf.put("实例", indi.get("instance"));
				perf.put("当前值", val.get("val"));
				perf.put("时间", val.get("time"));
				perfs.add(perf);
			}
			Map<String, Object> p = new HashMap<String, Object>();
			List<String> perfTitle = new ArrayList<String>();
			perfTitle.add("指标类别");
			perfTitle.add("指标");
			perfTitle.add("实例");
			perfTitle.add("当前值");
			perfTitle.add("时间");
			p.put("head", perfTitle);
			p.put("row", perfs);
			cdata.put("perf", p);

			List<String> cis = new ArrayList<String>();
			cis.add(hexId);
			// Long startTime = params.containsKey("startTime")
			// && params.getString("startTime").length() > 0 ?
			// Long.parseLong(params.getString("startTime"))*1000L : 0L;
			// Long endTime = params.containsKey("endTime")
			// && params.getString("endTime").length() > 0 ?
			// Long.parseLong(params.getString("endTime"))*1000L : System
			// .currentTimeMillis();
			List<Event> events = eventService.getEventForView(cis, viewId,
					user.getLoginName(), startTime, endTime);
			List<Map<String, String>> evList = new ArrayList<Map<String, String>>();
			for (Event ev : events) {
				Map<String, String> m = new HashMap<String, String>();
				String status = ev.getStatus().equals("1") == true ? "打开"
						: "关闭";

				String ciHex = HexString.decode(ev.getCiHex());
				JSONArray cicateci = JSONArray.fromObject(ciHex);
				String ciName = cicateci.getString(1);
				m.put("CI名称", ciName);
				m.put("级别", severityMap.get(ev.getSeverity()).get("name"));
				m.put("详细信息", ev.getSummary());
				m.put("时间", sdf.format(new Date(ev.getFirstOccurrence())));
				m.put("状态", status);
				m.put("color", severityMap.get(ev.getSeverity()).get("color"));
				evList.add(m);
			}
			Map<String, Object> e = new HashMap<String, Object>();
			List<String> eventTitle = new ArrayList<String>();
			eventTitle.add("CI名称");
			eventTitle.add("级别");
			eventTitle.add("详细信息");
			eventTitle.add("时间");
			eventTitle.add("状态");
			e.put("head", eventTitle);
			e.put("row", evList);
			cdata.put("event", e);
			
			List<KpiInformation> kpis = ciKpiRel.getKpiByCi(hexId);
			Map<String, List<Map<String, Object>>> historyPerfs = new HashMap<String, List<Map<String, Object>>>();
			Map<String, Map<String, List<String>>> selectMap = new HashMap<String, Map<String, List<String>>>();
			for(KpiInformation kpi:kpis){
				String kpiCate = kpi.getKpiCategoryName();
				String kpiName = kpi.getName();
				String key = kpiCate + "-" + kpiName;
				Map<String, List<String>> selectMapParam = selectMap.get(kpiCate);
				if(selectMapParam==null){
					selectMapParam = new HashMap<String, List<String>>();
					selectMap.put(kpiCate, selectMapParam);
				}
				List<String> selectMapInst = selectMapParam.get(kpiName);
				if(selectMapInst==null){
					selectMapInst = new ArrayList<String>();
					selectMapParam.put(kpiName, selectMapInst);
				}
				
				Map<Integer, List<PerformanceBean>> pm = performanceService.getAllPerformanceDatasByInstance("", info.getId(), "", kpi.getId(), "", sdf.format(new Date(endTime-86400000L)), sdf.format(new Date(endTime)), 0, 300);
				Map<String, List<Map<String, Object>>> subMap = new HashMap<String, List<Map<String, Object>>>();
				Set<Integer> pSet = pm.keySet();
				if(pSet!=null){
					for(Integer count:pSet){
						List<PerformanceBean> perfList = pm.get(count);
						for(PerformanceBean pb:perfList){
							String inst = pb.getInstance();
							List<Map<String, Object>> subList = subMap.get(inst);
							if(subList==null){
								subList = new ArrayList<Map<String, Object>>();
								subMap.put(inst, subList);
							}
							Map<String, Object> pMap = new HashMap<String, Object>();
							pMap.put("val", pb.getValue());
							pMap.put("time", pb.getStartTime());
							subList.add(pMap);
						}
						break;
					}
				}
				Set<String> s = subMap.keySet();
				for(String subInst:s){
					selectMapInst.add(subInst);
					List<Map<String, Object>> l = subMap.get(subInst);
					Collections.reverse(l);
					historyPerfs.put(key+(subInst.length()>0 ? "-"+subInst:""), l);
				}
			}
			List<String> selectKey = new ArrayList<String>();
			Set<String> kpiCateSet = selectMap.keySet();
			for(String kpiCate:kpiCateSet){
				Map<String, List<String>> selectMapKpi = selectMap.get(kpiCate);
				Set<String> kpiNameSet = selectMapKpi.keySet();
				for(String kpiName:kpiNameSet){
					List<String> selectMapInst = selectMapKpi.get(kpiName);
					for(String kpiInst:selectMapInst){
						selectKey.add(kpiCate+"-"+kpiName+(kpiInst.length()>0 ? "-"+kpiInst:""));
					}
				}
			}
			Map<String, Object> hp = new HashMap<String, Object>();
			hp.put("title", selectKey);
			hp.put("data", historyPerfs);
			cdata.put("historyPerf", hp);

			ret.put("message", "查询全部信息成功");
			ret.put("data", cdata);
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 所有共有视图的作者
	 * 
	 * @return
	 */
	private Representation getOpenViewAuthors() {
		JSONObject ret = new JSONObject();
		try {
			log.dLog("OpenViewAuthors");
			List<String> allOpenViewAuthor = infoService.getAllOpenViewAuthor();
			List<Object> retData = new ArrayList<Object>();
			for (String loginName : allOpenViewAuthor) {
				User user = userService.getUserByLoginName(loginName);
				// user.setPassword("");
				Map<String, Object> asMapForRest = user.asMapForRest();
				asMapForRest.put("name", asMapForRest.get("userName"));
				retData.add(asMapForRest);
			}
			ret.put("data", retData);
			ret.put("message", "获取用户成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation query(String cateId, Map<String, String> orExp,
			Boolean extend, int page, int pageSize) {
		JSONObject ret = new JSONObject();
		try {
			log.dLog("query");
			if (orExp.containsKey("*") && "*".equals(orExp.get("*"))) {
				orExp = null;
			}

			List<ViewInformation> list = new ArrayList<ViewInformation>();
			// Map<String, String> mustExp = new HashMap<String, String>();
			if (cateId == null || cateId.equals("")) {
				list = infoService.qureyByAdvanced(null, null, orExp,
						extend == null ? true : extend, null);
			} else {
				ViewCategory nc = cateService.getById(cateId);
				if (nc == null) {
					throw new Exception("分类[" + cateId + "]不存在");
				}
				List<ViewInformation> ls = infoService.qureyByAdvanced(nc,
						null, orExp, extend == null ? true : extend, null);
				for (ViewInformation viewInformation : ls) {
					if (ViewInformation.NORMAL.equals(viewInformation
							.getStatus()))
						list.add(viewInformation);
				}

				// list.addAll(ls);
			}
			// 排序
			Collections.sort(list, new Comparator<ViewInformation>() {
				@Override
				public int compare(ViewInformation arg0, ViewInformation arg1) {
					return arg0.getId().compareTo(arg1.getId());
				}
			});
			// 构建分页输出
			List<Object> datas = new ArrayList<Object>();
			int count = list.size();
			int start = (page - 1) * pageSize;
			start = start < 0 ? 0 : start;
			int end = page * pageSize;
			start = start > count ? count : start;
			end = end > count ? count : end;
			if (pageSize != -1) {
				list = list.subList(start, end);
			}
			// Map<String, List<String>> all = ciViewRelService.getAll();
			User user = getUser();
			for (ViewInformation info : list) {
				datas.add(aggregation(info, user));
			}
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("count", count);
			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("datas", datas);

			ret.put("message", "查询成功");
			ret.put("data", retMap);
			log.dLog("query");
		} catch (Exception me) {
			log.eLog(me);
			ret.put("message", me.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation queryFuzzy(String keyWord, int page, int pageSize) {
		JSONObject ret = new JSONObject();
		try {
			log.dLog("queryFuzzy");
			User user = getUser();
			List<ViewInformation> list = infoService.qureyFuzzy(keyWord, user);
			List<Object> datas = new ArrayList<Object>();
			int count = list.size();
			int start = (page - 1) * pageSize;
			start = start < 0 ? 0 : start;
			int end = page * pageSize;
			start = start > count ? count : start;
			end = end > count ? count : end;
			if (pageSize != -1) {
				list = list.subList(start, end);
			}
			for (ViewInformation info : list) {
				datas.add(aggregation(info, user));
			}

			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("count", count);
			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("datas", datas);

			ret.put("message", "查询成功");
			ret.put("data", retMap);
			log.dLog("queryFuzzy success");
		} catch (Exception me) {
			log.eLog(me);
			ret.put("message", me.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation refreshSvg() {
		JSONObject ret = new JSONObject();
		try {
			List<ViewInformation> all = infoService.getAll();
			for (ViewInformation viewInformation : all) {
				String svg = viewInformation.getSvg();
				ViewCategory nc = viewInformation.getCategory();
				String Hexname = HexString.encode(HexString.json2Str(
						nc.getName(), viewInformation.getName()));
				infoService.createSvg(Hexname, svg);
			}
			ret.put("message", "更新缩略图成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	// -----------------------------------------------------------------//
	// ---------------------------背景图---------------------------------//
	// -----------------------------------------------------------------//
	private Representation importBackground(Representation entity) {
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
				// if (is.available() > 524288) {// 512kb
				// throw new Exception("文件太大了");
				// }

				User user = getUser();
				filename = fi.getName();

				filename = new String(filename.getBytes("gbk"), "utf-8");

				ViewIcon viewIcon = infoService.getBackgroundByName(filename,
						user.getLoginName());

				if (viewIcon != null) {
					throw new Exception("图片[" + filename + "]已经存在!");
				}
				BufferedImage read = ImageIO.read(is);
				int width = read.getWidth();
				int height = read.getHeight();
				byte[] content = fi.get();
				// byte[] content = new byte[is.available()];
				is.read(content);

				ViewIcon icon = new ViewIcon(filename, user.getLoginName(),
						contentType, content, MD5.md5(content));
				icon.setWidth(width);
				icon.setHeight(height);
				infoService.saveBackground(icon);
				fi.delete();
			}
			ret.put("message", "保存成功");
			log.dLog("save success");
		} catch (Exception e) {
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteBackground(String name) {
		JSONObject ret = new JSONObject();
		log.dLog("删除背景图");
		try {// 前台上没带后缀的图片名称所以这里加个后缀
			infoService.deleteBackgroundByName(name, getUser().getLoginName());
			ret.put("message", "删除成功!");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation queryBackground(JSONObject data) {
		log.dLog("query background");

		JSONObject ret = new JSONObject();
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");
		String name = "";
		try {
			name = data.getString("like");
		} catch (Exception e) {
		}

		try {
			Page<Map<String, Object>> content = infoService
					.fuzzyQueryBackground(name, page, pageSize, getUser()
							.getLoginName());
			List<Map<String, Object>> datas = content.getDatas();
			JSONObject retData = new JSONObject();
			retData.put("page", page);
			retData.put("pageSize", pageSize);
			retData.put("datas", datas);
			retData.put("count", content.getTotalCount());
			ret.put("data", retData);
		} catch (Exception e) {
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}

		ret.put("message", "获取全部数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 求一图()中的全部的路径.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Representation allpaths(String startId, String end, JSONArray data) {
		log.dLog("path");
		JSONObject ret = new JSONObject();
		try {
			Map<String, JSONObject> cache = new HashMap<String, JSONObject>();
			for (Object object : data) {
				JSONObject node = (JSONObject) object;
				cache.put(node.getString("id"), node);
			}

			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
			// 要遍历的路径
			List<Map<String, Object>> paths = new ArrayList<Map<String, Object>>();
			Map<String, Object> start = new HashMap<String, Object>();
			LinkedHashSet<String> p = new LinkedHashSet<String>();
			p.add("1");

			start.put("next", startId);
			start.put("path", p);
			start.put("distance", 0d);

			paths.add(start);

			while (true) {
				List<Map<String, Object>> pathsNew = new ArrayList<Map<String, Object>>();
				for (Map<String, Object> path : paths) {
					JSONObject node = cache.get(path.get("next"));
					int x = (Integer) node.get("x");
					int y = (Integer) node.get("y");

					// JSONArray nextIds = node.getJSONArray("target");
					LinkedHashSet<String> nextIds = new LinkedHashSet<String>();
					JSONArray nextIds1 = node.getJSONArray("target");
					JSONArray nextIds2 = node.getJSONArray("source");
					nextIds.addAll(nextIds1);
					nextIds.addAll(nextIds2);
					double distance = (Double) path.get("distance");
					LinkedHashSet<String> nodeIds = (LinkedHashSet<String>) path
							.get("path");

					for (Object nextId : nextIds) {
						if (!nodeIds.contains(nextId) && !nextId.equals(end)) {
							Map<String, Object> newPath = new HashMap<String, Object>();
							// 新的路径
							LinkedHashSet<String> ss = new LinkedHashSet<String>();
							ss.addAll(nodeIds);
							ss.add((String) nextId);
							JSONObject nextNode = cache.get(nextId);
							// 距离
							distance += distance(x, y, nextNode.getInt("x"),
									nextNode.getInt("y"));

							newPath.put("path", ss);
							newPath.put("next", nextId);
							newPath.put("distance", distance);
							pathsNew.add(newPath);
						} else {
							if (nextId.equals(end)) {
								Map<String, Object> newPath = new HashMap<String, Object>();
								LinkedHashSet<String> ss = new LinkedHashSet<String>();
								ss.addAll(nodeIds);
								ss.add((String) nextId);
								JSONObject nextNode = cache.get(nextId);
								// 距离
								distance += distance(x, y,
										nextNode.getInt("x"),
										nextNode.getInt("y"));
								newPath.put("distance", distance);
								newPath.put("path", ss);
								retData.add(newPath);
							}
						}
					}
				}
				paths = pathsNew;
				if (paths.size() == 0) {
					break;
				}
			}
			ret.put("data", retData);
			ret.put("message", "获取全部数据成功");
		} catch (Exception e) {
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}

		return new JsonRepresentation(ret.toString());
	}

	private double distance(int sx, int sy, int ex, int ey) {
		if (sx == ex) {
			return Math.abs(sy - ey);
		} else if (ey == sy) {
			return Math.abs(sx - ex);
		} else {
			int a = Math.abs(sx - ex);
			int b = Math.abs(sy - ey);
			return Math.sqrt(a * a + b * b);
		}
	}

	/**
	 * 正则去替换svg的路径
	 * 
	 * @param str
	 * @return
	 */
	private String regex(String url, String str) throws IOException {
		String gifUrl = url;
		url = "file:///" + url;
		Pattern pattern = Pattern.compile("xlink:href=\"(.*?)\"");
		Matcher matcher = pattern.matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String st = matcher.group(1);
			if (st.indexOf("resource") != -1) {
				if (st.indexOf("resource/svg/") != -1) {
					st = st.substring(st.indexOf("resource/svg/"));
				} else if (st.indexOf("resource/ownsvg/") != -1) {
					st = st.substring(st.indexOf("resource/ownsvg/"));
				} else if (st.indexOf("resource/background/") != -1) {
					st = st.substring(st.indexOf("resource/background/"));
				}
			} else if (st.indexOf("mxGraph") != -1) {
				st = st.substring(st.indexOf("graph/plugins/"));
			} else if (st.indexOf("graph/images") != -1) {
				st = st.substring(st.indexOf("graph/"));
			}
			String fileurl;
			if (st.substring(st.length() - 3).toLowerCase().equals("gif")) {
				String filePath = ProjectInfo.getProjectRealPathConvert()
						+ "resource/thumbnail/" + System.currentTimeMillis()
						+ ".png";
				ImageIO.write(ImageIO.read(new File(gifUrl + st)), "png",
						new File(filePath));
				fileurl = "file:////" + filePath;
			} else {
				fileurl = url + st;
			}
			fileurl = fileurl.replace("resourceresource", "resource");
			matcher.appendReplacement(sb, "xlink:href=\"" + fileurl + "\"");
		}
		matcher.appendTail(sb);
		return sb.toString();
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
		if (method == ADD || method == UPDATE || method == DELETE) {
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
				throw new Exception("权限不足");
			}
			return;
		}
		if (method == 100) {
			log.eLog("权限不足");
			throw new Exception("权限不足");
		}
	}

	// -------------------------------------------------------------------------------
	// ----------------------------------视图阈值---------------------------------------
	// -------------------------------------------------------------------------------
	/**
	 * 不带分页的,视图阈值查看.
	 * 
	 * @param viewId
	 * @return
	 */
	private Representation getThresholdByView(String viewId) {
		JSONObject ret = new JSONObject();
		try {
			ViewInformation vInfo = infoService.getById(viewId);
			if (vInfo == null) {
				throw new Exception("视图不存在");
			}
			User user = getUser();
			List<Map<String, Object>> cikpi = subscription.getThresholdByView(
					viewId, user.getLoginName());
			List<String> ciIds = new ArrayList<String>();
			for (Map<String, Object> map : cikpi) {
				String kpiHex = (String) map.get("kpiId");
				String ciHex = (String) map.get("ciId");
				ciIds.add(ciHex);
				KpiInformation kpi = kpiCateService.getKpiByHex(kpiHex);
				Map<String, String> kpiData = new HashMap<String, String>();
				kpiData.put("name", kpi.getName());
				kpiData.put("id", kpi.getId());
				kpiData.put("kpiHex", kpi.getKpiHex());
				kpiData.put("kpiCategoryName", kpi.getKpiCategoryName());
				map.put("kpi", kpiData);
			}

			Map<String, CiInformation> ciCache = new HashMap<String, CiInformation>();
			List<CiInformation> ciInfo = ciInfoService.getByIds(ciIds);
			for (CiInformation ciInformation : ciInfo) {
				ciCache.put(ciInformation.getCiHex(), ciInformation);
			}
			for (Map<String, Object> map : cikpi) {
				String ciHex = (String) map.get("ciId");
				CiInformation ci = ciCache.get(ciHex);
				Map<String, String> asMap = new HashMap<String, String>();
				asMap.put("id", ci.getCiHex());
				asMap.put("name", ci.getName());
				asMap.put("categoryName", ci.getCategoryName());
				map.put("ci", asMap);
			}
			ret.put("data", cikpi);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 带分页的阈值查看
	 * 
	 * @param viewId
	 * @param parmas
	 * @return
	 */
	private Representation getThresholdByView(String viewId, JSONObject data) {
		JSONObject ret = new JSONObject();
		try {
			ViewInformation vInfo = infoService.getById(viewId);
			if (vInfo == null) {
				throw new Exception("视图不存在");
			}
			User user = getUser();

			int page = 0, pageSize = 0;
			page = data.getInt("page");
			pageSize = data.getInt("pageSize");
			// 默认查看当前用户的阈值,如果该用户没有订阅者查看作者的阈值
			String username = user.getLoginName();

			if (!username.equals(vInfo.getUserName())) {
				List<String> subscribers = subscription
						.getSubscriberByView(viewId);
				if (!subscribers.contains(username)) {
					username = vInfo.getUserName();
				}
			}

			Page<Map<String, Object>> content = subscription
					.getThresholdByView(viewId, username, page, pageSize);

			List<Map<String, Object>> cikpi = content.getDatas();
			if (cikpi != null && cikpi.size() > 0) {
				List<String> ciIds = new ArrayList<String>();
				for (Map<String, Object> map : cikpi) {
					String kpiHex = (String) map.get("kpiId");
					String ciHex = (String) map.get("ciId");
					ciIds.add(ciHex);
					KpiInformation kpi = kpiCateService.getKpiByHex(kpiHex);
					Map<String, String> kpiData = new HashMap<String, String>();
					kpiData.put("name", kpi.getName());
					kpiData.put("id", kpi.getId());
					kpiData.put("kpiHex", kpi.getKpiHex());
					kpiData.put("kpiCategoryName", kpi.getKpiCategoryName());
					map.put("kpi", kpiData);
				}

				Map<String, CiInformation> ciCache = new HashMap<String, CiInformation>();
				List<CiInformation> ciInfo = ciInfoService.getByIds(ciIds);
				for (CiInformation ciInformation : ciInfo) {
					ciCache.put(ciInformation.getCiHex(), ciInformation);
				}

				for (Map<String, Object> map : cikpi) {
					String ciHex = (String) map.get("ciId");
					CiInformation ci = ciCache.get(ciHex);
					Map<String, String> asMap = new HashMap<String, String>();
					asMap.put("id", ci.getCiHex());
					asMap.put("name", ci.getName());
					asMap.put("categoryName", ci.getCategoryName());
					map.put("ci", asMap);
				}
			}

			JSONObject retData = new JSONObject();
			retData.put("page", page);
			retData.put("pageSize", pageSize);
			retData.put("datas", cikpi);
			retData.put("count", content.getTotalCount());

			ret.put("data", retData);
			ret.put("message", "获取全部数据成功");
		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation putThresholdByView(String viewId, JSONArray data) {
		JSONObject ret = new JSONObject();
		try {
			ViewInformation vInfo = infoService.getById(viewId);
			if (vInfo == null) {
				throw new Exception("视图不存在");
			}
			User user = getUser();
			String username = user.getLoginName();
			// 权限验证
			if (!username.equals(vInfo.getUserName())) {
				List<String> subscribers = subscription
						.getSubscriberByView(viewId);
				if (!subscribers.contains(username)) {
					throw new Exception("请先订阅视图");
				}
			}

			subscription.incUpdate(user.getLoginName(), viewId,
					vInfo.getUserName(), data);
			ret.put("message", "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			getResponse().setStatus(new Status(600));
		}

		return new JsonRepresentation(ret.toString());
	}

	private Representation getPerformanceByView(String viewId, JSONObject data) {
		JSONObject ret = new JSONObject();
		try {
			ViewInformation vInfo = infoService.getById(viewId);
			if (vInfo == null) {
				throw new Exception("视图不存在");
			}

			int page = 0, pageSize = 0;
			long time = System.currentTimeMillis() / 1000;
			page = data.getInt("page");
			pageSize = data.getInt("pageSize");
			try {
				time = data.getLong("time");
			} catch (Exception e) {
			}

			List<String> cis = ciViewRelService.getByView(viewId);
			// subscription.getThresholdByView(viewId, userName);
			List<String[]> ciKpis = new ArrayList<String[]>();
			Map<String, List<KpiInformation>> cikpiRel = ciKpiRel
					.getKpiByCi(cis);
			Set<String> keySet = cikpiRel.keySet();
			for (String ci : keySet) {
				List<KpiInformation> list = cikpiRel.get(ci);
				for (KpiInformation kpiInformation : list) {
					String[] ck = new String[2];
					ck[0] = ci;
					ck[1] = kpiInformation.getKpiHex();
					ciKpis.add(ck);
				}
			}

			Page<Map<String, Object>> performacePage = performanceService
					.getPerformaceDatasByView(ciKpis, time, page, pageSize);

			JSONObject retData = new JSONObject();
			retData.put("page", page);
			retData.put("pageSize", pageSize);
			retData.put("datas", performacePage.getDatas());
			retData.put("count", performacePage.getTotalCount());
			ret.put("message", "获取数据成功");
			ret.put("data", retData);
		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取一张视图上的全部的cikpi,带分页的
	 * 
	 * @param viewId
	 * @param data
	 * @return
	 */
	private Representation getKpiByView(String viewId, JSONObject data) {
		JSONObject ret = new JSONObject();
		try {
			ViewInformation vInfo = infoService.getById(viewId);
			if (vInfo == null) {
				throw new Exception("视图不存在");
			}
			int page = 1, pageSize = 10;
			page = data.getInt("page");
			pageSize = data.getInt("pageSize");
			Long startTime = 0l;
			Long endTime = System.currentTimeMillis();
			try {
				startTime = data.getLong("startTime") * 1000;
				endTime = data.getLong("endTime") * 1000;
			} catch (Exception e) {
				//e.printStackTrace();
			}

			List<String> cis = ciViewRelService.getByView(viewId);

			int count = ciKpiRel.getCountByCiHexs(cis);
			int t = count / pageSize;
			if (count % pageSize != 0) {
				t++;
			}
			if (page > t) {
				page = t;
			}
			if (page < 1) {
				page = 1;
			}

			String username = null;
			List<String> subscripers = vInfo.getSubscripers();
			User user = getUser();
			if (subscripers.contains(user.getLoginName())) {
				username = user.getLoginName();
			} else {
				username = vInfo.getUserName();
			}

			List<Map<String, Object>> ciKpiRelbyCiHexs = ciKpiRel
					.getCiKpiRelbyCiHexs(cis, page, pageSize);
			if (ciKpiRelbyCiHexs.size() != 0) {
				//System.out.println(viewId+"　　　"+ciKpiRelbyCiHexs+"   " +startTime);
				Map<String, Map<String, String>> maxSeverityForView = eventService
						.getMaxSeverityForView(viewId, username,
								ciKpiRelbyCiHexs, startTime, endTime);

				// 获取级别对应的颜色
				Map<String, Map<String, String>> severityMap = null;
				List<Map<String, String>> severityList = eventViewService
						.getSeverityMap();
				severityMap = new HashMap<String, Map<String, String>>();
				for (Map<String, String> s : severityList) {
					severityMap.put(s.get("id"), s);
				}

				HashSet<String> nId = new HashSet<String>();
				for (Map<String, Object> map : ciKpiRelbyCiHexs) {
					nId.add((String) map.get("ciId"));
				}
				cis.clear();
				cis.addAll(nId);
				List<CiInformation> infos = ciInfoService.getByIds(cis);
				Map<String, CiInformation> ciCache = new HashMap<String, CiInformation>();
				for (CiInformation ciInformation : infos) {
					ciCache.put(ciInformation.getCiHex(), ciInformation);
				}
				for (Map<String, Object> map : ciKpiRelbyCiHexs) {
					String ciId = (String) map.get("ciId");
					String kpiHex = (String) map.get("kpiHex");
					CiInformation ciInfo = ciCache.get(ciId);
					if (ciInfo != null) {
						map.put("ciCategoryName", ciInfo.getCategoryName());
						map.put("ciName", ciInfo.getName());
						map.put("kpiName", map.get("name"));
						map.remove("name");
						map.remove("source");
						map.remove("unit");
						map.remove("threshold");
						// 通过VIEW-CI-KPI获取一定时间段内告警级别最高的颜色和级别
						if (startTime != null && endTime != null) {
							Map<String, String> kpiMap = maxSeverityForView
									.get(ciId);
							int severity = 0;
							try {
								severity = Integer.parseInt(kpiMap.get(kpiHex));
							} catch (Exception e) {
							}
							map.put("color",
									severity > 0 ? severityMap.get(
											severity + "").get("color")
											: "#00FF00");
							map.put("severity", severityMap.get(severity+"").get("name"));
						}
					}
				}
			}
			JSONObject retData = new JSONObject();

			retData.put("page", page);
			retData.put("pageSize", pageSize);
			retData.put("datas", ciKpiRelbyCiHexs);
			retData.put("count", count);
			ret.put("message", "获取数据成功");
			ret.put("data", retData);

		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getEventByCiKpi(String viewId, JSONObject data) {
		JSONObject ret = new JSONObject();
		int page = 1, pageSize = 10;
		page = data.getInt("page");
		pageSize = data.getInt("pageSize");
		String ciId = data.getString("ciId");
		String kpiHex = data.getString("kpiHex");
		long startTime = data.getLong("startTime") * 1000;
		long endTime = data.getLong("endTime") * 1000;
		try {
			User user = getUser();

			int count = eventService.getEventCountForView(ciId, kpiHex, viewId,
					user.getLoginName(), startTime, endTime);
			int t = count / pageSize;
			if (count % pageSize != 0) {
				t++;
			}

			if (page > t)
				page = t;
			if (page < 1)
				page = 1;

			List<Event> events = eventService.getEventForView(ciId, kpiHex,
					viewId, user.getLoginName(), startTime, endTime, (page - 1)
							* pageSize, pageSize);

			Map<String, Map<String, String>> severityMap = null;
			if (events != null && events.size() > 0) {
				List<Map<String, String>> severityList = eventViewService
						.getSeverityMap();
				severityMap = new HashMap<String, Map<String, String>>();
				for (Map<String, String> s : severityList) {
					severityMap.put(s.get("id"), s);
				}
			}
			List<Map<String, String>> statusList = eventViewService.getStatusMap();
			Map<String, String> statusMap = new HashMap<String, String>();
			for(Map<String, String> m:statusList){
				statusMap.put(m.get("id"), m.get("name"));
			}

			JSONArray retDate = new JSONArray();
			for (Event event : events) {
				JSONObject eventMap = JSONObject.fromObject(event);
				// 匹配颜色
				int severity = eventMap.getInt("severity");
				eventMap.put(
						"color",
						severity > 0 ? severityMap.get(severity + "").get(
								"color") : "#00FF00");
				eventMap.put("severity", severityMap.get(severity + "").get("name"));
				eventMap.put("status", statusMap.get(eventMap.getString("status")));
				retDate.add(eventMap);
			}
			Map<String, Object> pageRet = new HashMap<String, Object>();
			pageRet.put("count", count);
			pageRet.put("datas", retDate);
			pageRet.put("page", page);
			pageRet.put("pageSize", pageSize);
			ret.put("message", "获取视图成功");
			ret.put("data", pageRet);
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
			e.printStackTrace();
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取全部订阅的共有视图
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getSubscriptionView(JSONObject params)
			throws Exception {
		JSONObject ret = new JSONObject();
		User user = getUser();
		int page = 1, pageSize = 10;
		try {
			page = params.getInt("page");
			pageSize = params.getInt("pageSize");
			List<String> views = subscription.getViewBySubscriber(user
					.getLoginName());
			Page<ViewInformation> vPage = null;
			if (views == null || views.size() == 0) {
				vPage = new Page<ViewInformation>();
			} else {

				vPage = infoService.getSubscriptionByIds(views,
						user.getLoginName(), page, pageSize, false);
			}
			List<ViewInformation> subView = vPage.getDatas();
			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();

			if (subView != null && subView.size() > 0) {
				for (ViewInformation info : subView) {
					retData.add(aggregation(info, user));
				}
			}
			Map<String, Object> pageRet = new HashMap<String, Object>();
			pageRet.put("count", vPage.getTotalCount());
			pageRet.put("datas", retData);
			pageRet.put("page", page);
			pageRet.put("pageSize", pageSize);
			ret.put("message", "获取视图成功");
			ret.put("data", pageRet);
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
			e.printStackTrace();
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 将视图和其他前台需要的数据关联
	 * 
	 * @return
	 * @throws Exception
	 */
	private Map<String, Object> aggregation(ViewInformation vInfo, User user)
			throws Exception {
		if (vInfo == null)
			return new HashMap<String, Object>();
		if (user == null) {
			user = getUser();
		}
		Map<String, Object> asMap = vInfo.asMapForRest();

		List<String> ciIds = ciViewRelService.getByView(vInfo.getId());
		if (ciIds == null) {
			asMap.put("ciSize", 0);
		} else {
			asMap.put("ciSize", ciIds.size());
		}

		// 获取作者信息
		User author = userService.getUserByLoginName((String) asMap
				.get("userName"));
		asMap.put("author", author.asMapForRest());

		List<String> subscripers = vInfo.getSubscripers();
		if (subscripers.contains(user.getLoginName())) {
			asMap.put("isSubscription", true);
		} else {
			asMap.put("isSubscription", false);
		}
		return asMap;
	}

	// TODO 增加一个恢复视图的功能
	private Representation restoreSoftDelete(String viewId, JSONObject params)
			throws Exception {
		JSONObject ret = new JSONObject();
		String cateId = null;
		String name = null;
		try {
			cateId = params.getString("categoryId");
		} catch (Exception e) {
		}
		try {
			name = params.getString("name");
		} catch (Exception e) {
		}
		User user = getUser();
		try {
			ViewInformation view = infoService.getById(viewId);
			if (view == null) {
				throw new MyException("视图不存在");
			}
			ViewCategory vCate = null;
			if (cateId == null) {
				cateId = view.getCategoryId();
				vCate = view.getCategory();
			} else {
				ViewCategory tCate = viewCateService.getById(cateId);
				if (!user.getLoginName().equals(tCate.getUserId())) {
					throw new MyException("视图分类不存在");
				} else {
					vCate = tCate;
				}
			}
			if (vCate == null) {
				throw new MyException("视图分类不存在");
			}

			if (name == null) {
				name = view.getName();
			}
			boolean exist = infoService.exist(cateId, name);
			if (exist) {
				throw new MyException("视图在分类下已经存在,请修改名称");
			}

			view.setCategory(vCate);
			view.setCategoryId(vCate.getId());
			view.setName(name);
			view.setStatus(ViewInformation.NORMAL);

			ViewInformation update2 = infoService.update(view);
			// 恢复订阅.
			subscription.save(user.getLoginName(), update2);

			ret.put("message", "视图恢复成功");
		} catch (Exception e) {
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}

		return new JsonRepresentation(ret.toString());
	}

	// TODO 增加一个查询软删除后的视图
	private Representation getSoftDeleteView(JSONObject pageMap) {
		JSONObject ret = new JSONObject();

		try {
			int page = pageMap.getInt("page");
			int pageSize = pageMap.getInt("pageSize");
			User user = getUser();
			int count = infoService.getSoftDeleteCountByUser(user
					.getLoginName());
			int t = count / pageSize;
			if (count % pageSize != 0) {
				t++;
			}

			if (page > t)
				page = t;
			if (page < 1)
				page = 1;

			List<ViewInformation> softDeleteViews = infoService
					.getSoftDeleteByUser(user.getLoginName(), page, pageSize);

			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
			for (ViewInformation viewInformation : softDeleteViews) {
				retData.add(aggregation(viewInformation, user));
			}
			Map<String, Object> pageRet = new HashMap<String, Object>();
			pageRet.put("count", count);
			pageRet.put("datas", retData);
			pageRet.put("page", page);
			pageRet.put("pageSize", pageSize);
			ret.put("data", pageRet);
			ret.put("message", "获取成功");
		} catch (Exception e) {
			log.eLog(e);
			e.printStackTrace();
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return new JsonRepresentation(ret.toString());
	}
}
