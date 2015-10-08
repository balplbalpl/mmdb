package com.mmdb.rest.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.mapping.IPerfDbMapService;
import com.mmdb.service.task.ITaskService;

/**
 * DB数据集映射性能数据Rest
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-19
 */
public class PerfDbMappingRest extends BaseRest{
	
	private Log log = LogFactory.getLogger("PerfDbMappingRest");
	
	private IPerfDbMapService perfDbMapService;
	private IDataSourceService dataSourceService;
	private ITaskService taskService;
	
	@Override
	public void ioc(ApplicationContext context) {
		perfDbMapService = (IPerfDbMapService) SpringContextUtil
				.getApplicationContext().getBean("perfDbMapService");
		dataSourceService = (IDataSourceService)SpringContextUtil
				.getApplicationContext().getBean("dataSourceService");
		taskService = (ITaskService)SpringContextUtil
				.getApplicationContext().getBean("taskService");
	}

	@Override
	public Representation getHandler() throws Exception{
		String param1 = getValue("param1");

		if (param1 == null || "".equals(param1)) {
			return getAll();
		}else if ("owner".equals(param1)) {
			String param2 = getValue("param2");
			return getByUser(param2);
		}  else {
			//获取指定ID的映射
			return getById(param1);
		}
	}
	
	@Override
	public Representation postHandler(Representation entity)  throws Exception {
		String param1 = getValue("param1");

		if ("import".equals(param1)) {
			//return new JsonRepresentation(importData(entity));
		}
		JSONObject params = parseEntity(entity);
		if ("run".equals(param1)) {
			return run(params); //运行一次
		}else if("preview".equals(param1)){
			//预览映射结果
			return previewPerfToDbMapping(params);
		}else{
			//保存映射
			return savePerfToDbMapping(params);
		}
			
	}
	
	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return editPerfToDbMapping(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception{
		String param1 = getValue("param1");

		return deleteById(param1);
	}
	
	/**
	 * 添加映射
	 * 
	 * @param data
	 * @return
	 */
	private Representation savePerfToDbMapping(JSONObject data) throws Exception{
		
		JSONObject ret = new JSONObject();
		//判断映射名称是否已经存在
		List<PerfToDbMapping> mappingList = 
				perfDbMapService.getByName(data.getString("ruleName"));
		if(mappingList.size()>0){
			log.eLog("映射名称已经存在");
			throw new MException("映射名称已经存在");
		}
		
		PerfToDbMapping mapping = new PerfToDbMapping();
		try {
			//映射名称
			mapping.setName(data.getString("ruleName"));
			
			//是否激活，默认为激活
			mapping.setActive("1"); //预留变量，暂时未使用
			
			mapping.setDataSourceId(data.getString("dataSourceId"));
			
			//映射条件
			JSONObject ciCondJson = new JSONObject(); 
			ciCondJson.put("perfValuesInCi", data.get("perfValuesInCi"));
			ciCondJson.put("perf2CiLink", data.get("perf2CiLink"));
			ciCondJson.put("ciValues", data.get("ciValues"));
			mapping.setCiConditionJson(ciCondJson);
			
			JSONObject kpiCondJson = new JSONObject(); 
			kpiCondJson.put("perfValuesInKpi", data.get("perfValuesInKpi"));
			kpiCondJson.put("perf2KpiLink", data.get("perf2KpiLink"));
			kpiCondJson.put("kpiValues", data.get("kpiValues"));
			mapping.setKpiConditionJson(kpiCondJson);		
			
			//页面中%替换为了@@@此处转回为%
			String filed = data.getString("fieldMap");
			JSONObject fieldMap = JSONObject.fromObject(filed.replace("@@@", "%"));
			mapping.setFieldMap(fieldMap);
			
			JSONObject customFieldsMap = data.getJSONObject("customFieldsMap");
			mapping.setCustomFieldsMap(customFieldsMap);
			
			String valExp = (data.containsKey("valExp") ? data.getString("valExp"):"").trim();
			if(valExp.length()>0){
				if(Tool.checkPerfValExp(valExp)){
					mapping.setValExp(valExp);
				}else{
					mapping.setValExp("");
				}
			}else{
				mapping.setValExp("");
			}
			
			if(data.containsKey("ciHex")){
				mapping.setCiHex(data.getString("ciHex"));
			}
			if(data.containsKey("kpiHex")){
				mapping.setKpiHex(data.getString("kpiHex"));
			}
			//创建者
			String userName = getUsername();
			mapping.setOwner(userName);
			if(data.containsKey("isAddSync")){
				mapping.setIsAddSync(data.getString("isAddSync"));
			}
			//保存映射
			perfDbMapService.save(mapping);
			ret.put("message", "保存映射[" + mapping.getName() + "]成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "保存映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		
		return new JsonRepresentation(ret.toString());
	}
	
	
	/**
	 * 修改映射
	 * 
	 * @param data
	 * @return
	 */
	private Representation editPerfToDbMapping(JSONObject data) throws Exception{
		
		JSONObject ret = new JSONObject();
		try {
			String ruleId = data.getString("id");
			
			//获取到要更新的
			PerfToDbMapping mapping = perfDbMapService.getMappingById(ruleId);
			
			//映射数据集ID
			mapping.setDataSourceId(data.getString("dataSourceId"));
			
			//映射条件
			JSONObject ciCondJson = new JSONObject(); 
			ciCondJson.put("perfValuesInCi", data.get("perfValuesInCi"));
			ciCondJson.put("perf2CiLink", data.get("perf2CiLink"));
			ciCondJson.put("ciValues", data.get("ciValues"));
			mapping.setCiConditionJson(ciCondJson);
			
			JSONObject kpiCondJson = new JSONObject(); 
			kpiCondJson.put("perfValuesInKpi", data.get("perfValuesInKpi"));
			kpiCondJson.put("perf2KpiLink", data.get("perf2KpiLink"));
			kpiCondJson.put("kpiValues", data.get("kpiValues"));
			mapping.setKpiConditionJson(kpiCondJson);	
			//页面中%替换为了@@@此处转回为%
			String filed = data.getString("fieldMap");
			JSONObject fieldMap = JSONObject.fromObject(filed.replace("@@@", "%"));
			mapping.setFieldMap(fieldMap);
			
			JSONObject customFieldsMap = data.getJSONObject("customFieldsMap");
			mapping.setCustomFieldsMap(customFieldsMap);
			
			String valExp = (data.containsKey("valExp") ? data.getString("valExp"):"").trim();
			if(valExp.length()>0){
				if(Tool.checkPerfValExp(valExp)){
					mapping.setValExp(valExp);
				}else{
					mapping.setValExp("");
				}
			}else{
				mapping.setValExp("");
			}
			if(data.containsKey("ciHex")){
				mapping.setCiHex(data.getString("ciHex"));
			}
			if(data.containsKey("kpiHex")){
				mapping.setKpiHex(data.getString("kpiHex"));
			}
			if(data.containsKey("isAddSync")){
				mapping.setIsAddSync(data.getString("isAddSync"));
			}
			//修改映射
			perfDbMapService.update(mapping);
			
			ret.put("message", "修改映射[" + mapping.getName() + "]成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "修改映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		
		return new JsonRepresentation(ret.toString());
	}
	
	
	/**
	 * 预览映射
	 * 
	 * @param data
	 * @return
	 */
	private Representation previewPerfToDbMapping(JSONObject data) throws Exception{
		
		JSONObject ret = new JSONObject();
		
		PerfToDbMapping mapping = new PerfToDbMapping();
		try {
			//映射名称
			mapping.setName(data.getString("ruleName"));
			
			//是否激活，默认为激活
			mapping.setActive("1"); //预留变量，暂时未使用
			
			mapping.setDataSourceId(data.getString("dataSourceId"));
			
			//映射条件
			JSONObject ciCondJson = new JSONObject(); 
			ciCondJson.put("perfValuesInCi", data.get("perfValuesInCi"));
			ciCondJson.put("perf2CiLink", data.get("perf2CiLink"));
			ciCondJson.put("ciValues", data.get("ciValues"));
			mapping.setCiConditionJson(ciCondJson);
			
			JSONObject kpiCondJson = new JSONObject(); 
			kpiCondJson.put("perfValuesInKpi", data.get("perfValuesInKpi"));
			kpiCondJson.put("perf2KpiLink", data.get("perf2KpiLink"));
			kpiCondJson.put("kpiValues", data.get("kpiValues"));
			mapping.setKpiConditionJson(kpiCondJson);		
			
			//获取到所有的数据集配置列表
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			//获取到映射对应的数据集配置
			mapping.setDataSource(dpMap.get(mapping.getDataSourceId()));
			
			//页面中%替换为了@@@此处转回为%
			String filed = data.getString("fieldMap");
			JSONObject fieldMap = JSONObject.fromObject(filed.replace("@@@", "%"));
			mapping.setFieldMap(fieldMap);
			
			JSONObject customFieldsMap = data.getJSONObject("customFieldsMap");
			mapping.setCustomFieldsMap(customFieldsMap);
			String valExp = (data.containsKey("valExp") ? data.getString("valExp"):"").trim();
			if(valExp.length()>0){
				if(Tool.checkPerfValExp(valExp)){
					mapping.setValExp(valExp);
				}else{
					mapping.setValExp("");
				}
			}else{
				mapping.setValExp("");
			}
			
			if(data.containsKey("ciHex")){
				mapping.setCiHex(data.getString("ciHex"));
			}
			if(data.containsKey("kpiHex")){
				mapping.setKpiHex(data.getString("kpiHex"));
			}
			if(data.containsKey("isAddSync")){
				mapping.setIsAddSync(data.getString("isAddSync"));
			}
			//保存映射
			Map<String, List<?>> retMap = perfDbMapService.preView(mapping);
			List<?> matchedList = retMap.get("matchedList");
			List<?> matchedSourceList = retMap.get("matchedSourceList");
			List<?> unMatchedList = retMap.get("unMatchedList");
			
			ret.put("matchedList", matchedList);
			ret.put("unMatchedList", unMatchedList);
			ret.put("matchedSourceList", matchedSourceList);
			
			//ret.put("message", "预览映射[" + mapping.getName() + "]成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "预览映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 删除映射
	 * 
	 * @param data{id:ruleId}
	 * @return
	 */
	private Representation deleteById(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			PerfToDbMapping mapping = perfDbMapService.getMappingById(id);
			String userName = this.getUsername();
			//是否为管理员用户
			boolean isAdmin = this.isAdmin();
			//非管理员用户不能删除其他用户的映射信息
			if(!isAdmin){
				String owner = mapping.getOwner();
				if(!owner.equals(userName)){
					throw new MException("没有权限删除其他用户的映射规则!");
				}
			}
			
			List<String> tasks = taskService.getTaskNamesByMapId(id);
			if(tasks.size()>0){
				//ret.put("message", "删除失败,有任务正在使用此映射!");
				//getResponse().setStatus(new Status(600));
				throw new MException("删除失败,有任务正在使用此映射!");
			}else{
				perfDbMapService.deleteById(id);
				ret.put("message", "删除映射成功");
			}
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "删除映射失败!");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 返回一个json格式的映射列表
	 * 
	 * @return Representation 
	 * @throws Exception
	 */
	private Representation getAll() throws Exception{
		JSONObject ret = new JSONObject();
		try {
			List<PerfToDbMapping> mappingList = perfDbMapService.getAll();
			JSONArray list = new JSONArray();
			//获取到所有的数据集列表
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			
			//获取所有的任务列表
			List<Task> ts = taskService.getAll();

			for (PerfToDbMapping mapping : mappingList) {
				//获取到映射对应的数据集配置
				mapping.setDataSource(dpMap.get(mapping.getDataSourceId()));
				
				List<String> retList = new ArrayList<String>();
				//循环任务列表，找出使用此映射的任务
				for (Task t : ts) {
					List<String> mapIds = t.getPerfDbMapIds();
					if (mapIds.contains(mapping.getId())) {
						String name = t.getName();
						if (!retList.contains(name))
							retList.add(name);
					}
				}
				if(retList.size()>0){
					mapping.setTaskNames(retList.toString());
				}
				
				Map<String, Object> ruleMap = mapping.toMap();
				list.add(ruleMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部映射成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取全部映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 获取到某个用户的所有映射规则
	 * 
	 * @param username
	 * @return
	 * @throws Exception
	 */
	private Representation getByUser(String username) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			List<PerfToDbMapping> mappingList = perfDbMapService.getByOwner(username);
			JSONArray list = new JSONArray();
			//获取到所有的数据集列表
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			
			//获取所有的任务列表
			List<Task> ts = taskService.getAll();

			for (PerfToDbMapping mapping : mappingList) {
				//获取到映射对应的数据集配置
				mapping.setDataSource(dpMap.get(mapping.getDataSourceId()));
				
				List<String> retList = new ArrayList<String>();
				//循环任务列表，找出使用此映射的任务
				for (Task t : ts) {
					List<String> mapIds = t.getPerfDbMapIds();
					if (mapIds.contains(mapping.getId())) {
						String name = t.getName();
						if (!retList.contains(name))
							retList.add(name);
					}
				}
				if(retList.size()>0){
					mapping.setTaskNames(retList.toString());
				}
				
				Map<String, Object> ruleMap = mapping.toMap();
				list.add(ruleMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部映射成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取全部映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	
	/**
	 * 通过唯一id获取映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			PerfToDbMapping mapping = perfDbMapService.getMappingById(id);
			if (mapping != null) {
				//获取到所有的数据集列表
				Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
				//获取到映射对应的数据集配置
				mapping.setDataSource(dpMap.get(mapping.getDataSourceId()));
				
				Map<String, Object> asMap = mapping.toMap();
/*				if(mapping.getCiHex() != null && !"".equals(mapping.getCiHex())){
					String ciHex = HexString.decode(mapping.getCiHex());
					JSONArray cis = JSONArray.fromObject(ciHex);
					asMap.put("ciName", cis.getString(1));
					asMap.put("ciCategoryName", cis.getString(0));
				}
				if(mapping.getKpiHex() != null && !"".equals(mapping.getKpiHex())){
					String kpiHex = HexString.decode(mapping.getKpiHex());
					JSONArray kpis = JSONArray.fromObject(kpiHex);
					asMap.put("kpiName", kpis.getString(1));
					asMap.put("kpiCategoryName", kpis.getString(0));
				}*/
				ret.put("data", asMap);
				ret.put("message", "获取映射[" + mapping.getName() + "]成功");
			} else {
				//ret.put("message", "获取映射失败");
				//getResponse().setStatus(new Status(600));
				throw new MException("获取映射失败");
			}
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取映射失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	
	/**
	 * 执行一次映射规则
	 * 
	 * @param perf2DbObj
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation run(JSONObject cdMap) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.dLog("立即执行内部映射");
			String id = "";
			if(cdMap.containsKey("id")){
				id = cdMap.getString("id");
			}
			if (id == null || id.equals("")) {
				throw new Exception("映射ID不能为空");
			}
			PerfToDbMapping mapping =  perfDbMapService.getMappingById(id);
			if (mapping == null) {
				throw new Exception("映射不存在");
			}
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			mapping.setDataSource(dpMap.get(mapping.getDataSourceId()));
			Map<String, Integer> rm = perfDbMapService.runNow(mapping);
			ret.put("message", "性能数据同步完成,共同步(" + rm.get("send") + ")条性能数据");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 获取到所有的数据库配置
	 * 
	 * @return Map
	 * @throws Exception
	 */
	private Map<String, DataSourcePool> getDataSourcePoolMap() throws Exception{
		List<DataSourcePool> dps = dataSourceService.getAll();
		Map<String, DataSourcePool> dpMap = new HashMap<String, DataSourcePool>();
		for(DataSourcePool dp:dps){
			dpMap.put(dp.getId(), dp);
		}
		return dpMap;
	}
	
}
