package com.mmdb.rest.event;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
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
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.event.Event;
import com.mmdb.model.event.EventView;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.event.IEventService;
import com.mmdb.service.event.IEventViewService;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.des.Des;

public class EventViewRest extends BaseRest {
	private IEventViewService eventViewService;
	private IEventService eventService;
	private ICiCateService ciCateService;
	private IKpiCateService kpiCateService;

	@Override
	public void ioc(ApplicationContext context) {
		eventViewService = context.getBean(IEventViewService.class);
		eventService = context.getBean(IEventService.class);
		ciCateService = context.getBean(ICiCateService.class);
		kpiCateService = context.getBean(IKpiCateService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		User user = getUser();
		if (param1 == null || "".equals(param1)) {
			return getAll(user);
		} else {
			return getById(param1, user);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("getseverity".equals(param1)) {
			return getSeverity();
		} else if ("gettitle".equals(param1)) {
			return getTitle();
		} else if ("geteventbyview".equals(param1)) {
			JSONObject params = parseEntity(entity);
			return getEventByView(params, getUser());
		} else {
			JSONObject params = parseEntity(entity);
			return save(params, getUser());
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		User user = getUser();
		return update(params, user);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll();
		} else {
			return delete(param1);
		}
	}

	private Representation getAll(User user) throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<JSONObject> list = new ArrayList<JSONObject>();
		List<EventView> evs = eventViewService.getAll(user);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, KpiCategory> kpiCateMap = getKpiCateMap();
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, String> severityMap = new HashMap<String, String>();
		for (Map<String, String> s : severityList) {
			severityMap.put(s.get("id"), s.get("name"));
		}
		for (EventView ev : evs) {
			list.add(ev.asMap(ciCateMap, kpiCateMap, severityMap));
		}
		ret.put("data", list);
		ret.put("message", "获取所有事件视图成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getById(String id, User user) throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getById");
		EventView ev = eventViewService.getById(id, user);
		if (ev != null) {
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, KpiCategory> kpiCateMap = getKpiCateMap();
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, String> severityMap = new HashMap<String, String>();
			for (Map<String, String> s : severityList) {
				severityMap.put(s.get("id"), s.get("name"));
			}
			JSONObject asMap = ev.asMap(ciCateMap, kpiCateMap, severityMap);
			ret.put("data", asMap);
			ret.put("message", "获取事件视图成功");
		} else {
			throw new MException("事件视图不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation save(JSONObject data, User user)
			throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("save EventView");
		if (data == null || data.size() == 0) {
			throw new MException("EventView参数不能空");
		}
		String name = (String) data.get("name");
		if (name == null || name.equals("")) {
			throw new MException("名称不能空");
		}
		EventView view = eventViewService.getByName(name, user);
		if (view != null) {
			throw new MException("事件视图[" + name + "]已存在");
		}

		EventView ev = new EventView();
		ev.setName(name);
		JSONArray titleMap;
		try {
			titleMap = data.getJSONArray("titleMap");
		} catch (Exception e) {
			titleMap = new JSONArray();
		}
		ev.setTitleMap(titleMap);
		JSONArray severities = new JSONArray();
		try {
			severities = data.getJSONArray("severities");
			JSONArray severitiesNew = new JSONArray();
			for (int i = 0; i < severities.size(); i++) {
				JSONObject severity = severities.getJSONObject(i);
				severitiesNew.add(severity.getString("id"));
			}
			severities = severitiesNew;
		} catch (Exception e) {
			severities = new JSONArray();
		}
		ev.setSeverities(severities);
		JSONArray ciCates = new JSONArray();
		try {
			ciCates = data.getJSONArray("ciConf");
			// JSONArray ciCatesNew = new JSONArray();
			// for(int i=0;i<ciCates.size();i++){
			// JSONObject ciCate = ciCates.getJSONObject(i);
			// ciCatesNew.add(ciCate.getString("id"));
			// }
			// ciCates = ciCatesNew;
		} catch (Exception e) {
			ciCates = new JSONArray();
		}
		ev.setCiConf(ciCates);
		JSONArray kpiConf = new JSONArray();
		try {
			kpiConf = data.getJSONArray("kpiConf");
		} catch (Exception e) {
			kpiConf = new JSONArray();
		}
		ev.setKpiConf(kpiConf);
		ev.setLastTime(data.containsKey("lastTime") ? data.getLong("lastTime")
				: -1L);
		ev = eventViewService.save(ev, user);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, KpiCategory> kpiCateMap = getKpiCateMap();
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, String> severityMap = new HashMap<String, String>();
		for (Map<String, String> s : severityList) {
			severityMap.put(s.get("id"), s.get("name"));
		}
		JSONObject asMap = ev.asMap(ciCateMap, kpiCateMap, severityMap);
		ret.put("data", asMap);
		ret.put("message", "保存成功");

		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation update(JSONObject data, User user)
			throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("update EventView");
		if (data == null || data.size() == 0) {
			throw new MException("EventView参数不能空");
		}
		String id = data.getString("id");
		if (id == null || id.equals("")) {
			throw new MException("ID不能空");
		}
		EventView view = eventViewService.getById(id, user);
		if (view == null) {
			throw new MException("事件视图[" + id + "]不存在");
		}

		view.setName(data.containsKey("name") ? data.getString("name") : "");
		JSONArray titleMap;
		try {
			titleMap = data.getJSONArray("titleMap");
		} catch (Exception e) {
			titleMap = new JSONArray();
		}
		view.setTitleMap(titleMap);
		JSONArray severities = new JSONArray();
		try {
			severities = data.getJSONArray("severities");
			JSONArray severitiesNew = new JSONArray();
			for (int i = 0; i < severities.size(); i++) {
				JSONObject severity = severities.getJSONObject(i);
				severitiesNew.add(severity.getString("id"));
			}
			severities = severitiesNew;
		} catch (Exception e) {
			severities = new JSONArray();
		}
		view.setSeverities(severities);
		JSONArray ciCates = new JSONArray();
		try {
			ciCates = data.getJSONArray("ciCates");
			// JSONArray ciCatesNew = new JSONArray();
			// for(int i=0;i<ciCates.size();i++){
			// JSONObject ciCate = ciCates.getJSONObject(i);
			// ciCatesNew.add(ciCate.getString("id"));
			// }
			// ciCates = ciCatesNew;
		} catch (Exception e) {
			ciCates = new JSONArray();
		}
		view.setCiConf(ciCates);
		JSONArray kpiConf = new JSONArray();
		try {
			kpiConf = data.getJSONArray("kpiConf");
		} catch (Exception e) {
			kpiConf = new JSONArray();
		}
		view.setKpiConf(kpiConf);
		view.setLastTime(data.containsKey("lastTime") ? data
				.getLong("lastTime") : -1L);
		view = eventViewService.update(view, user);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, KpiCategory> kpiCateMap = getKpiCateMap();
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, String> severityMap = new HashMap<String, String>();
		for (Map<String, String> s : severityList) {
			severityMap.put(s.get("id"), s.get("name"));
		}
		JSONObject asMap = view.asMap(ciCateMap, kpiCateMap, severityMap);
		ret.put("data", asMap);
		ret.put("message", "修改成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll() throws Exception {
		JSONObject ret = new JSONObject();
		User user = getUser();
		eventViewService.deleteAll(user);
		ret.put("message", "删除全部成功");
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation delete(String id) throws Exception {
		JSONObject ret = new JSONObject();
		User user = getUser();
		log.dLog("delete EventView");
		if (id == null || id.equals("")) {
			throw new Exception("参数不能空");
		}
		EventView view = eventViewService.getById(id, user);
		if (view == null) {
			throw new MException("事件视图不存在");
		}
		List<EventView> evs = new ArrayList<EventView>();
		evs.add(view);
		eventViewService.delete(evs);
		ret.put("message", "删除成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getSeverity() throws Exception {
		JSONObject ret = new JSONObject();
		List<Map<String, String>> severityMap = eventViewService
				.getSeverityMap();
		ret.put("data", severityMap);
		ret.put("message", "获取事件级别成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getTitle() throws Exception {
		JSONObject ret = new JSONObject();
		Map<String, String> titleMap = eventViewService.getTitleMap(null, null);
		JSONArray data = new JSONArray();
		Set<String> set = titleMap.keySet();
		for (String key : set) {
			JSONObject d = new JSONObject();
			d.put("id", titleMap.get(key));
			d.put("name", key);
			data.add(d);
		}
		ret.put("data", data);
		ret.put("message", "获取事件表头成功");

		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation getEventByView(JSONObject data, User user) throws Exception {
		JSONObject ret = new JSONObject();
			log.dLog("getEventByView");
			if (data == null || data.size() == 0) {
				throw new Exception("EventView参数不能空");
			}
			String id = data.getString("id");
			EventView view = null;
			if (id != null && id.length() > 0) {
				view = eventViewService.getById(id, user);
			}

			String input = null;
			if (data.containsKey("input")
					&& data.getString("input").length() > 0) {
				input = data.getString("input");
			}
			List<String> ciConf = view == null ? null : view.getCiConf();
			List<Map<String, Object>> kpiConf = view == null ? null : view
					.getKpiConf();
			String severityFront = data.containsKey("severity") ? data
					.getString("severity") : null;
			List<String> severities = view == null ? null : view
					.getSeverities();
			List<Integer> severityListFinal = new ArrayList<Integer>();
			if (severities != null && severities.size() > 0) {
				Set<Integer> severitySet = new HashSet<Integer>();
				for (String severity : severities) {
					severitySet.add(Integer.parseInt(severity));
				}
				if (severityFront != null && severityFront.length() > 0) {
					if (severitySet.contains(Integer.parseInt(severityFront))) {
						severityListFinal.add(Integer.parseInt(severityFront));
					} else {
						severityListFinal.add(1000);
					}
				} else {
					for (String severity : severities) {
						severityListFinal.add(Integer.parseInt(severity));
					}
				}
			} else {
				if (severityFront != null && severityFront.length() > 0) {
					severityListFinal.add(Integer.parseInt(severityFront));
				}
			}
			Integer status = data.containsKey("status") ? (data.getString(
					"status").equals("") ? null : Integer.parseInt(data
					.getString("status"))) : null;
			Long lastTime = view == null ? -1L : view.getLastTime();
			Long startTime = null;
			Long endTime = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (lastTime < 0) {
				startTime = data.containsKey("startTime") ? (data.getString(
						"startTime").equals("") ? null : sdf.parse(
						data.getString("startTime")).getTime()) : null;
				endTime = data.containsKey("endTime") ? (data.getString(
						"endTime").equals("") ? null : sdf.parse(
						data.getString("endTime")).getTime()) : null;
			} else {
				endTime = System.currentTimeMillis();
				startTime = endTime - lastTime;
				Long startTimeFront = data.containsKey("startTime") ? (data
						.getString("startTime").equals("") ? null : sdf.parse(
						data.getString("startTime")).getTime()) : null;
				Long endTimeFront = data.containsKey("endTime") ? (data
						.getString("endTime").equals("") ? null : sdf.parse(
						data.getString("endTime")).getTime()) : null;
				if (startTimeFront != null && startTimeFront > startTime) {
					startTime = startTimeFront;
				}
				if (endTimeFront != null && endTimeFront < endTime) {
					endTime = endTimeFront;
				}
			}
			Boolean isHistory = data.getBoolean("isHistory");
			Map<String, String> titleMapDefault = eventViewService.getTitleMap(
					"EVENT", null);
			Map<String, String> varcharTitles = eventViewService.getTitleMap(
					null, "varchar");
			List<Map<String, String>> titleListDefault = new ArrayList<Map<String, String>>();
			Set<String> titleSetDefault = titleMapDefault.keySet();
			for (String key : titleSetDefault) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("id", titleMapDefault.get(key));
				m.put("name", key);
				titleListDefault.add(m);
			}
			List<Map<String, String>> titleMap = view == null ? titleListDefault
					: view.getTitleMap();
			Map<String, String> titles = new HashMap<String, String>();
			for (Map<String, String> title : titleMap) {
				titles.put(title.get("name"), title.get("id"));
			}
			List<Map<String, String>> severityList = eventViewService
					.getSeverityMap();
			Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
			for (Map<String, String> s : severityList) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("name", s.get("name"));
				m.put("color", s.get("color"));
				m.put("lineColor", s.get("lineColor"));
				severityMap.put(s.get("id"), m);
			}
			List<Map<String, String>> statusList = eventViewService
					.getStatusMap();
			Map<String, String> statusMap = new HashMap<String, String>();
			for (Map<String, String> s : statusList) {
				statusMap.put(s.get("id"), s.get("name"));
			}
			JSONObject order = null;
			try {
				order = data.getJSONObject("order");
				if (order.size() == 0) {
					order = null;
				}
			} catch (Exception e) {
			}
			List<Event> eventList = eventService.getEvents(input, ciConf,
					kpiConf, severityListFinal, status, startTime, endTime,
					isHistory, titles, varcharTitles, severityMap, statusMap,
					order, user.getLoginName());
			Map<String, Long> severityCountMap = eventService.getEventSeverity(
					view, isHistory, user.getLoginName());

			JSONObject retData = new JSONObject();
			Integer page = data.getInt("page");
			Integer pageSize = data.getInt("pageSize");
			JSONArray retList = new JSONArray();
			for (int i = (page - 1) * pageSize; i < page * pageSize
					&& i < eventList.size(); i++) {
				Event event = eventList.get(i);
				JSONObject ev = new JSONObject();
				ev.put("serial",
						event.getSerial() == null ? "" : event.getSerial());
				ev.put("duplicateSerial",
						event.getDuplicateSerial() == null ? "" : event
								.getDuplicateSerial());
				ev.put("ciCategory",
						event.getCiCategory() == null ? "" : event
								.getCiCategory());
				ev.put("ciid", event.getCiid() == null ? "" : event.getCiid());
				ev.put("kpiCategory", event.getKpiCategory() == null ? ""
						: event.getKpiCategory());
				ev.put("kpi", event.getKpi() == null ? "" : event.getKpi());
				ev.put("kpiInstance", event.getKpiInstance() == null ? ""
						: event.getKpiInstance());
				ev.put("eventTitle",
						event.getEventTitle() == null ? "" : event
								.getEventTitle());
				ev.put("summary",
						event.getSummary() == null ? "" : event.getSummary());
				ev.put("status",
						event.getStatus() == null ? "" : event.getStatus());
				String s = event.getSeverity() == null ? "" : event
						.getSeverity();
				ev.put("severity", s);
				Map<String, String> m = severityMap.get(s);
				if (m != null) {
					ev.put("color", m.get("lineColor"));
				} else {
					ev.put("color", "");
				}
				ev.put("firstOccurrence",
						event.getFirstOccurrence() == null ? 0L : event
								.getFirstOccurrence());
				ev.put("lastOccurrence", event.getLastOccurrence() == null ? 0L
						: event.getLastOccurrence());
				ev.put("closeTime",
						event.getCloseTime() == null ? 0L : event
								.getCloseTime());
				ev.put("closeInfo",
						event.getCloseInfo() == null ? "" : event
								.getCloseInfo());
				ev.put("closeUid",
						event.getCloseUid() == null ? "" : event.getCloseUid());
				ev.put("ackTime",
						event.getAckTime() == null ? 0L : event.getAckTime());
				ev.put("ackInfo",
						event.getAckInfo() == null ? "" : event.getAckInfo());
				ev.put("ackUid",
						event.getAckUid() == null ? "" : event.getAckUid());
				ev.put("tally",
						event.getTally() == null ? 0L : event.getTally());
				ev.put("data", event.getData());
				retList.add(ev);
			}

			List<Map<String, Object>> severityCountList = new ArrayList<Map<String, Object>>();
			Long allCount = 0L;
			for (Map<String, String> s : severityList) {
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", s.get("id"));
				m.put("name", s.get("name"));
				m.put("color", s.get("color"));
				m.put("count",
						severityCountMap.containsKey(s.get("id")) ? severityCountMap
								.get(s.get("id")) : 0L);
				severityCountList.add(m);
				allCount = allCount + (Long) m.get("count");
			}
			retData.put("title", titleMap);
			retData.put("data", retList);
			retData.put("page", page);
			retData.put("pageSize", pageSize);
			retData.put(
					"pageTotal",
					eventList.size() % pageSize == 0 ? (eventList.size() / pageSize)
							: (eventList.size() / pageSize + 1));
			retData.put("allCount", allCount);
			retData.put("severityCount", severityCountList);

			ret.put("data", retData);
			ret.put("message", "查询成功");
		return new JsonRepresentation(ret.toString());
	}

	private Map<String, CiCategory> getCiCateMap() throws Exception {
		List<CiCategory> cates = ciCateService.getAll();
		Map<String, CiCategory> cateMap = new HashMap<String, CiCategory>();
		for (CiCategory cate : cates) {
			cateMap.put(cate.getId(), cate);
		}
		return cateMap;
	}

	private Map<String, KpiCategory> getKpiCateMap() throws Exception {
		List<KpiCategory> cates = kpiCateService.getAll();
		Map<String, KpiCategory> cateMap = new HashMap<String, KpiCategory>();
		for (KpiCategory cate : cates) {
			cateMap.put(cate.getId(), cate);
		}
		return cateMap;
	}
}
