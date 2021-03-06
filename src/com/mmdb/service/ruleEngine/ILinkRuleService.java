package com.mmdb.service.ruleEngine;

import java.util.List;

import net.sf.json.JSONObject;

import com.mmdb.model.rule.LinkRule;

/**
 * 匹配 规则的管理服务类接口
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-7
 */
public interface ILinkRuleService {
	
	/**
	 * 保存匹配规则到数据库中
	 * 
	 * @param rule
	 * @throws Exception
	 */
	public void saveLinkRule(LinkRule rule) throws Exception;
	
	/**
	 * 通过类型获取到匹配规则
	 * 
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getRulesByType(String ruleType) throws Exception;
	
	/**
	 * 通过用户获取到匹配规则
	 * 
	 * @return List<LinkRule>
	 */
	public List<LinkRule> getRulesByUser(String ruleType,String userName) throws Exception;
	
	/**
	 * 通过名称和类型获取到匹配规则
	 * 
	 * @return List匹配规则列表
	 */
	public List<LinkRule> getRulesByName(String name,String type) throws Exception;
	
	/**
	 * 通过唯一标识ID获取匹配规则
	 * 
	 * @param id
	 * @return LinkRule
	 */
	public LinkRule getRuleById(String id) throws Exception;
	
	
	/**
	 * 编辑匹配规则
	 * 
	 * @param rule
	 * @throws Exception
	 */
	public void editLinkRule(LinkRule rule) throws Exception;
	
	/**
	 * 删除匹配规则
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void deleteLinkRule(String id) throws Exception;
	
	/**
	 * 拼装匹配到CI规则的Action字段
	 * 
	 * @param JSONObject 包含规则信息的JSONObject对象
	 * @param perfValKey Json信息中保存，性能数据的attribute-value条件的Key
	 * 
	 * @return String
	 * 			eg："10.1.1.1".equals(o.get("name"))
	 * 				||"logicServer".equals(o.get("ciName")
	 * 				@@and@@ ci.ip='o.get("ip")' and ci.app like 'o.get("app")') 
	 * 				and ci.ip='10.1.1.110' "
	 */
	public String genLink2CiRuleCondition(JSONObject condtionJson,String perfValKey) throws Exception;
	
	/**
	 * 拼装匹配到KPI规则的Action字段
	 * 
	 * @param JSONObject 包含规则信息的JSONObject对象
	 * @param perfValKey Json信息中保存，性能数据的attribute-value条件的Key
	 * @return String
	 * 			eg："10.1.1.1".equals(o.get("name"))
	 * 				||"logicServer".equals(o.get("ciName")
	 * 				@@and@@ kpi.ip='o.get("ip")' and kpi.app like 'o.get("app")') 
	 * 				and kpi.ip='10.1.1.110' "
	 */
	public String genLink2KpiRuleCondition(JSONObject condtionJson,String perfValKey) throws Exception;
}
