package com.mmdb.rest.rule;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.rule.LinkRule;
import com.mmdb.model.rule.RuleUtil;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.ruleEngine.ILinkRuleService;

/**
 * 性能数据匹配CI的Rest
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-7
 *
 */
public class LinkRuleRest extends BaseRest  {
	private Log log = LogFactory.getLogger("Link2CiRuleRest");
	
	private ILinkRuleService linkRuleService;
	
	@Override
	public void  ioc(ApplicationContext context) {
		linkRuleService = (ILinkRuleService) SpringContextUtil
				.getApplicationContext().getBean("linkRuleService");
	}
	
	@Override
	public Representation getHandler() throws Exception{
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		if (param1 == null || "".equals(param1)) {
			return getRulesByType(RuleUtil.PERF_LINK_CI);
		} else if (RuleUtil.PERF_LINK_CI.equals(param1)) {
			//获取CI匹配规则
			return getRulesByType(RuleUtil.PERF_LINK_CI);
		} else if (RuleUtil.PERF_LINK_KPI.equals(param1)) {
			//获取KPI匹配规则
			return getRulesByType(RuleUtil.PERF_LINK_KPI);
		} else if ("owner".equals(param1)) {
			if (RuleUtil.PERF_LINK_CI.equals(param2)) {
				//获取当前用户的CI匹配规则
				return getRulesByUser(RuleUtil.PERF_LINK_CI);
			} else {
				//获取当前用户的KPI匹配规则
				return getRulesByUser(RuleUtil.PERF_LINK_KPI);
			}
		} else {
			//获取指定ID的匹配规则
			return getRuleById(param1);
		}
	}
	
	@Override
	public Representation postHandler(Representation entity)  throws Exception{
		String param1 = getValue("param1");

		if ("import".equals(param1)) {
			//return new JsonRepresentation(importData(entity));
		}

		JSONObject params = parseEntity(entity);
		return saveLinkRule(params);
	}
	
	@Override
	public Representation putHandler(Representation entity) throws Exception{
		JSONObject params = parseEntity(entity);
		return editLinkRule(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception{
		String param1 = getValue("param1");
		return deleteLinkRuleById(param1);
	}
	
	/**
	 * 添加匹配规则
	 * 
	 * @param data
	 * @return
	 */
	private Representation saveLinkRule(JSONObject data) throws Exception{
		
		JSONObject ret = new JSONObject();
		//log.iLog("###:"+rule.getConditionJson().toString());
		//判断规则名称是否已经存在
		List<LinkRule> ruleList = 
				linkRuleService.getRulesByName(data.getString("ruleName"),
						data.getString("ruleType"));
		if(ruleList.size()>0){
			log.eLog("规则名称已经存在");
			//ret.put("message", "规则名称已经存在");
			//getResponse().setStatus(new Status(600));
			//return new JsonRepresentation(ret.toString());
			throw new MException("规则名称已经存在");
		}
		String userName = this.getUsername();
		LinkRule rule = new LinkRule();
		try {
			//规则名称
			rule.setName(data.getString("ruleName"));
			//规则类型
			rule.setRuleType(data.getString("ruleType"));
			//是否激活，默认为激活
			rule.setActive("1");
			//优先级
			rule.setPriority("1");
			//规则条件
			JSONObject condJson = new JSONObject(); 
			//根据规则类型，获取规则条件参数
			if(RuleUtil.PERF_LINK_CI.equalsIgnoreCase(rule.getRuleType())){
				condJson.put("perfValues", data.get("perfValues"));
				condJson.put("perf2CiLink", data.get("perf2CiLink"));
				condJson.put("ciValues", data.get("ciValues"));
			}else{
				condJson.put("perfValues", data.get("perfValues"));
				condJson.put("perf2KpiLink", data.get("perf2KpiLink"));
				condJson.put("kpiValues", data.get("kpiValues"));
			}
			rule.setConditionJson(condJson);
			
			rule.setOwner(userName);
			
			//保存规则
			linkRuleService.saveLinkRule(rule);
			
			ret.put("message", "保存匹配规则[" + rule.getName() + "]成功");
		} catch (Exception e) {
			log.eLog(e);
			//ret.put("message", "保存匹配规则失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		
		return new JsonRepresentation(ret.toString());
	}
	
	
	/**
	 * 修改匹配规则
	 * 
	 * @param data
	 * @return
	 */
	private Representation editLinkRule(JSONObject data)  throws Exception{
		
		JSONObject ret = new JSONObject();
		String ruleId = data.getString("id");
		//获取到要更新的rule
		LinkRule rule = linkRuleService.getRuleById(ruleId);
		
		//规则名称
		//rule.setName(data.getString("ruleName"));
		//规则类型
		rule.setRuleType(data.getString("ruleType"));
		
		//规则条件
		JSONObject condJson = new JSONObject(); 
		//根据规则类型，获取规则条件参数
		if(RuleUtil.PERF_LINK_CI.equalsIgnoreCase(rule.getRuleType())){
			condJson.put("perfValues", data.get("perfValues"));
			condJson.put("perf2CiLink", data.get("perf2CiLink"));
			condJson.put("ciValues", data.get("ciValues"));
		}else{
			condJson.put("perfValues", data.get("perfValues"));
			condJson.put("perf2KpiLink", data.get("perf2KpiLink"));
			condJson.put("kpiValues", data.get("kpiValues"));
		}
		rule.setConditionJson(condJson);
		
		//修改规则
		linkRuleService.editLinkRule(rule);
		ret.put("message", "修改匹配规则[" + rule.getName() + "]成功");
		
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 删除匹配规则
	 * 
	 * @param data{id:ruleId}
	 * @return
	 */
	private Representation deleteLinkRuleById(String ruleId) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			linkRuleService.deleteLinkRule(ruleId);
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "删除匹配规则失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 返回一个json格式的匹配规则列表
	 * 
	 * @param ruleType 规则类型
	 * @return Representation 
	 * @throws Exception
	 */
	private Representation getRulesByType(String ruleType)  throws Exception{
		JSONObject ret = new JSONObject();
		try {
			List<LinkRule> ruleList = linkRuleService.getRulesByType(ruleType);
			JSONArray list = new JSONArray();
			for (LinkRule rule : ruleList) {
				Map<String, Object> ruleMap = rule.rule2Map();
				list.add(ruleMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部["+ruleType+"]匹配规则成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取全部["+ruleType+"]匹配规则失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 获取到当前用户下的匹配规则列表
	 * 
	 * @param ruleType 规则类型
	 * @return Representation 
	 * @throws Exception
	 */
	private Representation getRulesByUser(String ruleType)  throws Exception{
		JSONObject ret = new JSONObject();
		try {
			String userName = this.getUsername();
			List<LinkRule> ruleList = linkRuleService.getRulesByUser(ruleType, userName);
			JSONArray list = new JSONArray();
			for (LinkRule rule : ruleList) {
				Map<String, Object> ruleMap = rule.rule2Map();
				list.add(ruleMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部["+ruleType+"]匹配规则成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取全部["+ruleType+"]匹配规则失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 通过唯一id获取匹配规则
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getRuleById(String id)  throws Exception{
		JSONObject ret = new JSONObject();
		try {
			LinkRule linkRule = linkRuleService.getRuleById(id);
			if (linkRule != null) {
				Map<String, Object> asMap = linkRule.rule2Map();

				ret.put("data", asMap);
				ret.put("message", "获取匹配规则[" + id + "]成功");
			} else {
				//ret.put("message", "获取匹配规则[" + id + "]失败");
				//getResponse().setStatus(new Status(600));
				throw new MException("匹配规则[" + id + "]不存在");
			}
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", "获取匹配规则[" + id + "]失败");
			//getResponse().setStatus(new Status(600));
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
}
