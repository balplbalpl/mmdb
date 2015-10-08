package com.mmdb.rest.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.KpiSyncMapping;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.mapping.IKpiSyncMappingService;
import com.mmdb.service.mapping.IPerfDbMapService;
import com.mmdb.service.mapping.ISourceCategoryMapService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.mq.SendToMQ;
import com.mmdb.service.task.ITaskService;

/**
 * 
 * @author xj
 * 
 */
public class TaskRest extends BaseRest {
	private Log log = LogFactory.getLogger("TaskRest");
	private ITaskService taskService;
	private IInCiCateMapService inCiCateMapService;
	private ISourceCategoryMapService sourceCateMapService;
	private ISourceRelationMapService sourceRelMapService;
	private IPerfDbMapService perfDbMapService;
	private IKpiSyncMappingService kpiSyncMappingService;
	private String url = "tcp://localhost:61616";
	private SendToMQ mqService = new SendToMQ();

	@Override
	public void ioc(ApplicationContext context) {
		taskService = context.getBean(ITaskService.class);
		inCiCateMapService = context.getBean(IInCiCateMapService.class);
		sourceCateMapService = context.getBean(ISourceCategoryMapService.class);
		sourceRelMapService = context.getBean(ISourceRelationMapService.class);
		perfDbMapService = context.getBean(IPerfDbMapService.class);
		kpiSyncMappingService = context.getBean(IKpiSyncMappingService.class);
		setExisting(true);
		ResourceBundle init = ResourceBundle
				.getBundle("config.demo.demo-global");
		url = init.getString("amq.url");
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		if ("run".equals(param1)) {
			return run(params);
		} else if ("setstatus".equals(param1)) {
			return setStatus(params);
		} else {
			return save(params);
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return update(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll();
		} else {
			return deleteById(param1);
		}
	}

	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<Task> tasks = taskService.getAll();
		Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
		Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
		Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
		Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
		Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
		for (Task task : tasks) {
			task = addMultiList(sourceToCategoryMappingMap,
					sourceToRelationMappingMap, inCiCateMapMap,
					perfToDbMappingMap, kpiSyncMappingMap, task);
			list.add(task.asMap());
		}
		ret.put("data", list);
		ret.put("message", "获取所有任务成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取task
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		Task task = taskService.getById(id);
		if (task != null) {
			Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
			Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
			Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
			Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
			Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
			task = addMultiList(sourceToCategoryMappingMap,
					sourceToRelationMappingMap, inCiCateMapMap,
					perfToDbMappingMap, kpiSyncMappingMap, task);
			Map<String, Object> asMap = task.asMap();

			ret.put("data", asMap);
			ret.put("message", "获取任务成功");
		} else {
			throw new MException("获取任务失败");
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation save(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		
		String owner = getUsername();
		log.dLog("save Task");
		if (data == null || data.size() == 0) {
			throw new Exception("Task参数不能空");
		}
		String name = (String) data.get("name");
		if (name == null || name.equals("")) {
			throw new MException("名称不能空");
		}
		Task task = taskService.getByName(name);
		if (task != null) {
			throw new MException("任务[" + name + "]已存在");
		}

		Task t = new Task();
		t.setName(name);
		t.setOpen(data.getBoolean("open"));
		t.setTimeOut(data.getBoolean("timeOut"));
		JSONObject timing;
		try {
			timing = data.getJSONObject("timing");
		} catch (Exception e) {
			timing = new JSONObject();
		}
		t.setTiming(timing);
		JSONArray cateList = null;
		try {
			cateList = data.getJSONArray("dbCiCateMapIds");
			JSONArray cateListNew = new JSONArray();
			for (int i = 0; i < cateList.size(); i++) {
				JSONObject cate = cateList.getJSONObject(i);
				cateListNew.add(cate.getString("id"));
			}
			cateList = cateListNew;
		} catch (Exception e) {
			cateList = new JSONArray();
		}
		t.setDbCiCateMapIds(cateList);

		JSONArray relList = null;
		try {
			relList = data.getJSONArray("outCiCateMapIds");
			JSONArray relListNew = new JSONArray();
			for (int i = 0; i < relList.size(); i++) {
				JSONObject rel = relList.getJSONObject(i);
				relListNew.add(rel.getString("id"));
			}
			relList = relListNew;
		} catch (Exception e) {
			relList = new JSONArray();
		}
		t.setOutCiCateMapIds(relList);

		JSONArray ciList = null;
		try {
			ciList = data.getJSONArray("inCiCateMapIds");
			JSONArray ciListNew = new JSONArray();
			for (int i = 0; i < ciList.size(); i++) {
				JSONObject ci = ciList.getJSONObject(i);
				ciListNew.add(ci.getString("id"));
			}
			ciList = ciListNew;
		} catch (Exception e) {
			ciList = new JSONArray();
		}
		t.setInCiCateMapIds(ciList);

		JSONArray perfList = null;
		try {
			perfList = data.getJSONArray("perfDbMapIds");
			JSONArray perfListNew = new JSONArray();
			for (int i = 0; i < perfList.size(); i++) {
				JSONObject perf = perfList.getJSONObject(i);
				perfListNew.add(perf.getString("id"));
			}
			perfList = perfListNew;
		} catch (Exception e) {
			perfList = new JSONArray();
		}
		t.setPerfDbMapIds(perfList);

		JSONArray kpiList = null;
		try {
			kpiList = data.getJSONArray("kpiMapIds");
			JSONArray kpiListNew = new JSONArray();
			for (int i = 0; i < kpiList.size(); i++) {
				JSONObject kpi = kpiList.getJSONObject(i);
				kpiListNew.add(kpi.getString("id"));
			}
			kpiList = kpiListNew;
		} catch (Exception e) {
			kpiList = new JSONArray();
		}
		t.setKpiSyncMapIds(kpiList);
		t.setOwner(owner);

		t = taskService.save(t);
		
		Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
		Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
		Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
		Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
		Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
		t = addMultiList(sourceToCategoryMappingMap,
				sourceToRelationMappingMap, inCiCateMapMap, perfToDbMappingMap,
				kpiSyncMappingMap, t);
		Map<String, Object> asMap = t.asMap();
		ret.put("data", asMap);
		ret.put("message", "保存成功");
		data.put("owner", owner);
		mqService.sendMessage(data.toString(), url, "task_add");

		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation update(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("update Task");
		if (data == null || data.size() == 0) {
			throw new Exception("Task参数不能空");
		}
		String id = data.getString("id");
		Task t = taskService.getById(id);
		if (t == null) {
			throw new Exception("Task不存在");
		}
		t.setName(data.containsKey("name") ? data.getString("name") : "");
		t.setOpen(data.getBoolean("open"));
		t.setTimeOut(data.getBoolean("timeOut"));
		JSONObject timing;
		try {
			timing = data.getJSONObject("timing");
		} catch (Exception e) {
			timing = new JSONObject();
		}
		t.setTiming(timing);
		JSONArray cateList = null;
		try {
			cateList = data.getJSONArray("dbCiCateMapIds");
			JSONArray cateListNew = new JSONArray();
			for (int i = 0; i < cateList.size(); i++) {
				JSONObject cate = cateList.getJSONObject(i);
				cateListNew.add(cate.getString("id"));
			}
			cateList = cateListNew;
		} catch (Exception e) {
			cateList = new JSONArray();
		}
		t.setDbCiCateMapIds(cateList);
		JSONArray relList = null;
		try {
			relList = data.getJSONArray("outCiCateMapIds");
			JSONArray relListNew = new JSONArray();
			for (int i = 0; i < relList.size(); i++) {
				JSONObject rel = relList.getJSONObject(i);
				relListNew.add(rel.getString("id"));
			}
			relList = relListNew;
		} catch (Exception e) {
			relList = new JSONArray();
		}
		t.setOutCiCateMapIds(relList);
		JSONArray ciList = null;
		try {
			ciList = data.getJSONArray("inCiCateMapIds");
			JSONArray ciListNew = new JSONArray();
			for (int i = 0; i < ciList.size(); i++) {
				JSONObject ci = ciList.getJSONObject(i);
				ciListNew.add(ci.getString("id"));
			}
			ciList = ciListNew;
		} catch (Exception e) {
			ciList = new JSONArray();
		}
		t.setInCiCateMapIds(ciList);
		JSONArray perfList = null;
		try {
			perfList = data.getJSONArray("perfDbMapIds");
			JSONArray perfListNew = new JSONArray();
			for (int i = 0; i < perfList.size(); i++) {
				JSONObject perf = perfList.getJSONObject(i);
				perfListNew.add(perf.getString("id"));
			}
			perfList = perfListNew;
		} catch (Exception e) {
			perfList = new JSONArray();
		}
		t.setPerfDbMapIds(perfList);

		JSONArray kpiList = null;
		try {
			kpiList = data.getJSONArray("kpiMapIds");
			JSONArray kpiListNew = new JSONArray();
			for (int i = 0; i < kpiList.size(); i++) {
				JSONObject kpi = kpiList.getJSONObject(i);
				kpiListNew.add(kpi.getString("id"));
			}
			kpiList = kpiListNew;
		} catch (Exception e) {
			kpiList = new JSONArray();
		}
		t.setKpiSyncMapIds(kpiList);

		t = taskService.update(t);
		Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
		Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
		Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
		Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
		Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
		t = addMultiList(sourceToCategoryMappingMap,
				sourceToRelationMappingMap, inCiCateMapMap, perfToDbMappingMap,
				kpiSyncMappingMap, t);
		Map<String, Object> asMap = t.asMap();
		ret.put("data", asMap);
		ret.put("message", "修改成功");
		mqService.sendMessage(data.toString(), url, "task_update");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除全部的ciCate和ci
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteAll() throws Exception {
		JSONObject ret = new JSONObject();
		taskService.deleteAll();
		ret.put("message", "删除全部成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除ciCate和ciCate下的全部ci
	 * 
	 * @param cateId
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("delete");
		if (id == null || id.equals("")) {
			throw new Exception("任务ID不能为空");
		}
		Task task = taskService.getById(id);
		if (task == null) {
			throw new MException("要删除的任务不存在");
		}
		// Long id = irm.getNeo4jid();
		taskService.delete(task);
		mqService.sendMessage(JSONObject.fromObject(task).toString(), url,
				"task_delete");
		ret.put("message", "删除内部映射[" + id + "]成功");
		ret.put("message", "删除[" + task.getName() + "]成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation run(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String id = "";
		id = data.getString("id");
		Task task = taskService.getById(id);
		if (task != null) {
			Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
			Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
			Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
			Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
			Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
			task = addMultiList(sourceToCategoryMappingMap,
					sourceToRelationMappingMap, inCiCateMapMap,
					perfToDbMappingMap, kpiSyncMappingMap, task);
			taskService.runNow(task);
			ret.put("message", "执行任务成功");
		} else {
			throw new MException("获取任务[" + id + "]失败");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation setStatus(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("setTasksStatus");
		String name = data.getString("name");
		if (name == null || name.equals("")) {
			throw new Exception("任务ID不能为空");
		}
		Task task = taskService.getByName(name);
		if (task == null) {
			throw new MException("任务[" + name + "]不存在");
		}
		Map<String, InCiCateMap> inCiCateMapMap = getInCiCateMapMap();
		Map<String, SourceToRelationMapping> sourceToRelationMappingMap = getSourceToRelationMappingMap();
		Map<String, SourceToCategoryMapping> sourceToCategoryMappingMap = getSourceToCategoryMappingMap();
		Map<String, PerfToDbMapping> perfToDbMappingMap = getPerfToDbMappingMap();
		Map<String, KpiSyncMapping> kpiSyncMappingMap = getKpiSyncMappingMap();
		task = addMultiList(sourceToCategoryMappingMap,
				sourceToRelationMappingMap, inCiCateMapMap, perfToDbMappingMap,
				kpiSyncMappingMap, task);
		boolean timeOut = false;
		Boolean status = data.getBoolean("status");
		if (status) {
			Map<String, String> tm = task.getTiming();
			if (task.getOpen()) {
				String type = tm.get("触发频率");
				if (type.equals("one")) {
					String time = tm.get("time");
					SimpleDateFormat sd = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					Date d1 = sd.parse(time);
					Date d2 = new Date();
					if (d1.getTime() > d2.getTime()) {
						/*
						 * QuartzManager.addJob(task.getName(),
						 * TaskJob.class.getName(), tm.get("runtime"));
						 */
					} else {
						timeOut = true;
						log.dLog("任务已过期");
					}
				} else {
					/*
					 * QuartzManager.addJob(task.getName(),
					 * TaskJob.class.getName(), tm.get("runtime"));
					 */
				}

			} else {
				String type = tm.get("触发频率");
				if (type.equals("one")) {
					String time = tm.get("time");
					SimpleDateFormat sd = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					Date d1 = sd.parse(time);
					Date d2 = new Date();
					if (d1.getTime() > d2.getTime()) {
						// QuartzManager.addJob(task.getName(),
						// TaskJob.class.getName(), tm.get("runtime"));
					} else {
						timeOut = true;
					}
				} else {
					// QuartzManager.addJob(task.getName(),
					// TaskJob.class.getName(), tm.get("runtime"));
				}
			}
		} else {
			// QuartzManager.removeJob(task.getName());
		}
		task = taskService.setStatus(task, status, timeOut);
		ret.put("data", task.asMap());
		ret.put("message", "任务[" + name + (status ? "]开启" : "]关闭") + "成功");
		log.dLog("setTasksStatus success");
		return new JsonRepresentation(ret.toString());
	}

	private Map<String, SourceToCategoryMapping> getSourceToCategoryMappingMap()
			throws Exception {
		List<SourceToCategoryMapping> list = sourceCateMapService.getAll();
		Map<String, SourceToCategoryMapping> map = new HashMap<String, SourceToCategoryMapping>();
		for (SourceToCategoryMapping m : list) {
			map.put(m.getId(), m);
		}
		return map;
	}

	private Map<String, SourceToRelationMapping> getSourceToRelationMappingMap()
			throws Exception {
		List<SourceToRelationMapping> list = sourceRelMapService.getAll();
		Map<String, SourceToRelationMapping> map = new HashMap<String, SourceToRelationMapping>();
		for (SourceToRelationMapping m : list) {
			map.put(m.getId(), m);
		}
		return map;
	}

	private Map<String, InCiCateMap> getInCiCateMapMap() throws Exception {
		List<InCiCateMap> list = inCiCateMapService.getAll();
		Map<String, InCiCateMap> map = new HashMap<String, InCiCateMap>();
		for (InCiCateMap m : list) {
			map.put(m.getId(), m);
		}
		return map;
	}

	private Map<String, PerfToDbMapping> getPerfToDbMappingMap()
			throws Exception {
		List<PerfToDbMapping> list = perfDbMapService.getAll();
		Map<String, PerfToDbMapping> map = new HashMap<String, PerfToDbMapping>();
		for (PerfToDbMapping m : list) {
			map.put(m.getId(), m);
		}
		return map;
	}

	private Map<String, KpiSyncMapping> getKpiSyncMappingMap() throws Exception {
		List<KpiSyncMapping> all = kpiSyncMappingService.getAll();
		Map<String, KpiSyncMapping> map = new HashMap<String, KpiSyncMapping>();
		for (KpiSyncMapping kpiSyncMapping : all) {
			map.put(kpiSyncMapping.getId(), kpiSyncMapping);
		}
		return map;
	}

	private Task addMultiList(Map<String, SourceToCategoryMapping> cateMap,
			Map<String, SourceToRelationMapping> relMap,
			Map<String, InCiCateMap> ciMap,
			Map<String, PerfToDbMapping> perfMap,
			Map<String, KpiSyncMapping> kpiMap, Task task) throws Exception {
		List<SourceToCategoryMapping> cateList = new ArrayList<SourceToCategoryMapping>();
		List<String> cateIds = task.getDbCiCateMapIds();
		for (String id : cateIds) {
			SourceToCategoryMapping cate = cateMap.get(id);
			if (cate != null) {
				cateList.add(cate);
			}
		}
		task.setDbCiCateMap(cateList);
		List<SourceToRelationMapping> relList = new ArrayList<SourceToRelationMapping>();
		List<String> relIds = task.getOutCiCateMapIds();
		for (String id : relIds) {
			SourceToRelationMapping rel = relMap.get(id);
			if (rel != null) {
				relList.add(rel);
			}
		}
		task.setOutCiCateMap(relList);
		List<InCiCateMap> ciList = new ArrayList<InCiCateMap>();
		List<String> ciIds = task.getInCiCateMapIds();
		for (String id : ciIds) {
			InCiCateMap ci = ciMap.get(id);
			if (ci != null) {
				ciList.add(ci);
			}
		}
		task.setInCiCateMap(ciList);
		List<PerfToDbMapping> perfList = new ArrayList<PerfToDbMapping>();
		List<String> perfIds = task.getPerfDbMapIds();
		for (String id : perfIds) {
			PerfToDbMapping perf = perfMap.get(id);
			if (perf != null) {
				perfList.add(perf);
			}
		}
		task.setPerfDbMap(perfList);

		List<KpiSyncMapping> kpiList = new ArrayList<KpiSyncMapping>();
		List<String> kpiSyncMapIds = task.getKpiSyncMapIds();
		for (String id : kpiSyncMapIds) {
			KpiSyncMapping kpiSyncMap = kpiMap.get(id);
			if (kpiSyncMap != null) {
				kpiList.add(kpiSyncMap);
			}
		}
		task.setKpiSyncMap(kpiList);
		return task;
	}
}
