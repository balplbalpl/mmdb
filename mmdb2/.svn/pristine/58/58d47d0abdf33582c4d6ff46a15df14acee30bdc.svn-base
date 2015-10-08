package com.mmdb.rest.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.model.event.Event;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.event.IEventService;
import com.mmdb.service.event.IEventViewService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.info.IViewInfoService;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.service.performance.impl.PerformanceService;
import com.mmdb.service.relation.ICiViewRelService;
import com.mmdb.util.HexString;

public class EventRest extends BaseRest {
	private IEventService eventService;
	private IEventViewService eventViewService;
	private ICiInfoService ciInfoService;
	private ICiViewRelService ciViewRelServiceImpl;
	private IPerformanceService performanceService;
	private IViewInfoService viewInfoService;

	@Override
	public void ioc(ApplicationContext context) {
		eventService = context.getBean(IEventService.class);
		eventViewService = context.getBean(IEventViewService.class);
		ciInfoService = context.getBean(ICiInfoService.class);
		ciViewRelServiceImpl = context.getBean(ICiViewRelService.class);
		viewInfoService = context.getBean(IViewInfoService.class);
		performanceService = new PerformanceService();
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 != null && !"".equals(param1)) {
			return getById(param1);
		}
		return notFindMethod(null);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if("removeallevent".equals(param1)){
			return removeAllEvent();
		}
		JSONObject params = parseEntity(entity);
		if ("ackevent".equals(param1)) {
			return ackEvent(params);
		} else if ("closeevent".equals(param1)) {
			return closeEvent(params);
		} else if ("getviewsbyevent".equals(param1)) {
			return getViewsByEvent(params);
		} else if ("getduplicateevent".equals(param1)) {
			return getDuplicateEvent(params);
		} else if ("getoperation".equals(param1)) {
			return getOperation(params);
		} else if ("getconfig".equals(param1)) {
			return getConfig(params);
		} else if ("getperf".equals(param1)) {
			return getPerf(params);
		}   
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	private Representation getById(String serial) throws Exception {
		JSONObject ret = new JSONObject();
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		Map<String, String> titles = eventViewService
				.getTitleMap("EVENT", null);
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		Event event = eventService.getEventBySerial(serial, titles,
				severityMap, statusMap);
		if (event == null) {
			throw new MException("事件[" + serial + "]已不存在");
		}

		ret.put("data", event.getData());
		ret.put("message", "获取事件成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getViewsByEvent(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String serial = data.containsKey("serial") ? data.getString("serial")
				: "";
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		Map<String, String> titles = eventViewService
				.getTitleMap("EVENT", null);
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		Event event = eventService.getEventBySerial(serial, titles,
				severityMap, statusMap);
		if (event == null) {
			throw new MException("事件[" + serial + "]已不存在");
		}
		ret.put("data", "");
		JSONArray retData = new JSONArray();
		// JSONObject r1 = new JSONObject();
		// r1.put("name", "haha");
		// r1.put("url", "http://www.sina.com.cn");
		// retData.add(r1);
		if (event.getViewId() != null && event.getViewId().length() > 0) {
			ViewInformation viewInfo = viewInfoService.getById(event
					.getViewId());
			if (viewInfo != null) {
				JSONObject vJs = new JSONObject();
				vJs.put("name", viewInfo.getName());
				vJs.put("url", viewInfo.getId());
				retData.add(vJs);
			}
		} else {
			if (event.getCiHex() != null && event.getCiHex().length() > 0) {
				List<String> viewIds = ciViewRelServiceImpl.getByCi(event
						.getCiHex());
				for (String viewId : viewIds) {
					ViewInformation view = viewInfoService.getById(viewId);
					JSONObject vJs = new JSONObject();
					vJs.put("name", view.getName());
					vJs.put("url", viewId);
					retData.add(vJs);
				}
			}
		}

		ret.put("data", retData);
		ret.put("message", "获取操作成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getDuplicateEvent(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String serial = data.containsKey("serial") ? data.getString("serial")
				: "";
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		List<Event> events = eventService.getEventByDuplicateSerial(serial);
		JSONArray retData = new JSONArray();
		for (Event event : events) {
			JSONObject ev = new JSONObject();
			ev.put("事件编号", event.getSerial());
			ev.put("标题", event.getEventTitle());
			ev.put("内容", event.getSummary());
			ev.put("级别", severityMap.get(event.getSeverity()).get("name"));
			ev.put("状态", statusMap.get(event.getStatus()));
			ev.put("发生时间", event.getFirstOccurrence());
			ev.put("重复次数", event.getTally());
			ev.put("color", severityMap.get(event.getSeverity()).get("color"));
			retData.add(ev);
		}
		JSONArray title = new JSONArray();
		title.add("事件编号");
		title.add("标题");
		title.add("内容");
		title.add("级别");
		title.add("状态");
		title.add("发生时间");
		title.add("重复次数");
		JSONObject d = new JSONObject();
		d.put("title", title);
		d.put("data", retData);

		ret.put("data", d);
		ret.put("message", "获取从事件成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getOperation(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String serial = data.containsKey("serial") ? data.getString("serial")
				: "";
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		Map<String, String> titles = eventViewService
				.getTitleMap("EVENT", null);
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		Event event = eventService.getEventBySerial(serial, titles,
				severityMap, statusMap);
		if (event == null) {
			throw new MException("事件[" + serial + "]已不存在");
		}
		JSONObject operation = new JSONObject();
		operation.put("ackInfo",
				event.getAckInfo() == null ? "" : event.getAckInfo());
		operation.put("ackUid",
				event.getAckUid() == null ? "" : event.getAckUid());
		operation.put("ackTime",
				event.getAckTime() == null ? 0L : event.getAckTime());
		operation.put("closeInfo",
				event.getCloseInfo() == null ? "" : event.getCloseInfo());
		operation.put("closeUid",
				event.getCloseUid() == null ? "" : event.getCloseUid());
		operation.put("closeTime",
				event.getCloseTime() == null ? 0L : event.getCloseTime());

		ret.put("data", operation);
		ret.put("message", "获取操作成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getConfig(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String serial = data.containsKey("serial") ? data.getString("serial")
				: "";
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		Map<String, String> titles = eventViewService
				.getTitleMap("EVENT", null);
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		Event event = eventService.getEventBySerial(serial, titles,
				severityMap, statusMap);
		if (event == null) {
			throw new MException("事件[" + serial + "]已不存在");
		}
		ret.put("data", "");
		if (event.getCiid() != null && event.getCiid().length() > 0) {
			List<CiInformation> cis = ciInfoService.getByProperty("_id",
					event.getCiid());
			if (cis != null && cis.size() == 1) {
				ret.put("data", cis.get(0).getData());
			}
		}
		ret.put("message", "获取配置数据成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation getPerf(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String serial = data.containsKey("serial") ? data.getString("serial")
				: "";
		if (serial.equals("")) {
			throw new MException("事件编号不能为空");
		}
		Map<String, String> titles = eventViewService
				.getTitleMap("EVENT", null);
		List<Map<String, String>> severityList = eventViewService
				.getSeverityMap();
		Map<String, Map<String, String>> severityMap = new HashMap<String, Map<String, String>>();
		for (Map<String, String> s : severityList) {
			Map<String, String> m = new HashMap<String, String>();
			m.put("name", s.get("name"));
			m.put("color", s.get("color"));
			severityMap.put(s.get("id"), m);
		}
		List<Map<String, String>> statusList = eventViewService.getStatusMap();
		Map<String, String> statusMap = new HashMap<String, String>();
		for (Map<String, String> s : statusList) {
			statusMap.put(s.get("id"), s.get("name"));
		}
		Event event = eventService.getEventBySerial(serial, titles,
				severityMap, statusMap);
		if (event == null) {
			throw new MException("事件[" + serial + "]已不存在");
		}
		ret.put("data", "");
		if (event.getCiid() != null && event.getCiid().length() > 0
				&& event.getKpi() != null && event.getKpi().length() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String kpiInstance = event.getKpiInstance() == null ? "" : event
					.getKpiInstance();
			String ciid = event.getCiid();
			String kpiid = event.getKpi();
			Long time = event.getFirstOccurrence() == null ? System
					.currentTimeMillis() : event.getFirstOccurrence();
			String startTime = sdf.format(new Date(time - 7200000L));
			String endTime = sdf.format(new Date(time + 7200000L));
			Map<Integer, List<PerformanceBean>> perfs = performanceService
					.getAllPerformanceDatasByInstance("", ciid, "", kpiid,
							kpiInstance, startTime, endTime, 0, 1000);
			if (perfs != null) {
				Set<Integer> perfSet = perfs.keySet();
				if (perfSet.size() == 1) {
					for (Integer i : perfSet) {
						if (i > 0) {
							JSONObject jsObj = new JSONObject();
							SimpleDateFormat sdf_hm = new SimpleDateFormat(
									"HH:mm");
							List<PerformanceBean> perfList = perfs.get(i);
							JSONArray perfRet = new JSONArray();
							for (int j = perfList.size() - 1; j >= 0; j--) {
								PerformanceBean pb = perfList.get(j);
								JSONObject pJs = new JSONObject();
								pJs.put("label", sdf_hm.format(sdf.parse(pb
										.getStartTime())));
								pJs.put("value",
										Double.parseDouble(pb.getValue()));
								perfRet.add(pJs);
							}
							jsObj.put("data", perfRet);
							String ciHexId = event.getCiHex();
							JSONArray cols = new JSONArray();
							String kpiHexId = event.getKpiHex();
							JSONArray kpiHex = JSONArray.fromObject(HexString
									.decode(kpiHexId));
							JSONObject col1 = new JSONObject();
							col1.put("id", "kpiCate");
							col1.put("name", "KPI分类");
							col1.put("val", kpiHex.getString(0));
							cols.add(col1);
							JSONObject col2 = new JSONObject();
							col2.put("id", "kpi");
							col2.put("name", "KPI");
							col2.put("val", kpiHex.getString(1));
							cols.add(col2);
							JSONObject col3 = new JSONObject();
							col3.put("id", "kpiInstance");
							col3.put("name", "实例");
							col3.put("val", kpiInstance);
							cols.add(col3);
							// JSONArray ciHex =
							// JSONArray.fromObject(HexString.decode(ciHexId));
							// JSONObject col4 = new JSONObject();
							// col4.put("id", "ciCate");
							// col4.put("name", "CI分类");
							// col4.put("val", ciHex.getString(0));
							// cols.add(col4);
							// JSONObject col5 = new JSONObject();
							// col5.put("id", "ci");
							// col5.put("name", "CI");
							// col5.put("val", ciHex.getString(1));
							// cols.add(col5);

							ret.put("col", cols);
							ret.put("data", jsObj);
						}
						break;
					}
				}
			}
		}
		ret.put("message", "获取性能数据成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation ackEvent(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		if (!data.containsKey("serial") || data.getString("serial").equals("")) {
			throw new MException("事件编号不能为空");
		}
		String serial = data.getString("serial");
		String ackInfo = data.containsKey("ackInfo") ? data
				.getString("ackInfo") : "";
		String ackUid = getUser().getLoginName();
		Long ackTime = System.currentTimeMillis();
		eventService.ackEvent(serial, ackInfo, ackUid, ackTime);
		ret.put("message", "确认事件成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation closeEvent(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		if (!data.containsKey("serial") || data.getString("serial").equals("")) {
			throw new MException("事件编号不能为空");
		}
		String serial = data.getString("serial");
		String closeInfo = data.containsKey("closeInfo") ? data
				.getString("closeInfo") : "";
		String closeUid = getUser().getLoginName();
		Long closeTime = System.currentTimeMillis();
		List<Map<String, String>> statusMap = eventViewService.getStatusMap();
		Integer status = null;
		for (Map<String, String> m : statusMap) {
			if (m.get("name").equals("关闭")) {
				status = Integer.parseInt(m.get("id"));
			}
		}
		if (status == null) {
			throw new MException("status默认值错误");
		}
		eventService.closeEvent(serial, closeInfo, closeUid, closeTime, status);
		ret.put("message", "关闭事件成功");

		return new JsonRepresentation(ret.toString());
	}

	private Representation removeAllEvent() throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("removeAllEvent");
		eventService.removeAllEvent();

		ret.put("message", "删除事件成功");
		return new JsonRepresentation(ret.toString());
	}

}
