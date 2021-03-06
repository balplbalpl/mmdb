package com.mmdb.service.ruleEngine;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.rule.LinkRule;
import com.mmdb.model.rule.RuleUtil;
import com.mmdb.ruleEngine.perf.RuleDao;
import com.mmdb.service.notify.impl.NotifyService;


/**
 * 规则的管理服务类
 * 
 * @author yuhao.guan
 * @version 1.0 2015-6-18
 */
@Component("linkRuleService")
public class LinkRuleServiceImpl implements ILinkRuleService{
	private Log logger = LogFactory.getLogger("LinkRuleServiceImpl");
	
	@Autowired
	private RuleDao ruleDao;
	
	@Autowired
	private NotifyService notifyService;
	
	/**
	 * 数据库中的CI数据，除了基础字段都加上了"data$"这个字符来表示是数据信息
	 * 因此这里使用Map记录Ci的基础属性，除去这个Map中包含属性
	 * 其它属性在建立匹配规则时都需要加上"data$"
	 */
	private HashMap<String,String> ciAttributeMap = new HashMap<String,String>();
	
	public LinkRuleServiceImpl(){
		
		ciAttributeMap.put("name", "name");
		ciAttributeMap.put("id", "id");
		ciAttributeMap.put("categoryId", "categoryId");
		ciAttributeMap.put("source", "source");
		ciAttributeMap.put("createTime", "createTime");
		ciAttributeMap.put("updateTime", "updateTime");
		ciAttributeMap.put("jsonId", "jsonId");
	}

	/**
	 * 通过类型获取到匹配规则
	 * 
	 * @return List匹配规则列表
	 */
	@Override
	public List<LinkRule> getRulesByType(String ruleType) throws Exception{
		return ruleDao.getRulesByType(ruleType);
	}
	
	/**
	 * 通过类型获取到匹配规则
	 * 
	 * @return List匹配规则列表
	 */
	@Override
	public List<LinkRule> getRulesByUser(String ruleType,String userName) throws Exception{
		return ruleDao.getRulesByUser(ruleType, userName);
	}
	
	/**
	 * 通过名称和类型获取到匹配规则
	 * 
	 * @return List匹配规则列表
	 */
	@Override
	public List<LinkRule> getRulesByName(String name,String type) throws Exception{
		return ruleDao.getRulesByName(name, type);
	}
	
	/**
	 * 通过唯一标识ID获取匹配规则
	 * 
	 * @param id
	 * @return
	 */
	@Override	
	public LinkRule getRuleById(String id) throws Exception{
		return ruleDao.getRulesById(id);
	}
	
	/**
	 * 保存匹配规则
	 * 
	 * @param rule
	 * @throws Exception
	 */
	@Override
	public void saveLinkRule(LinkRule rule) throws Exception{
		try{
			UUID uuid = UUID.randomUUID();
			rule.setId(uuid.toString());
			JSONObject obj = rule.getConditionJson();
			
			if(RuleUtil.PERF_LINK_CI.equalsIgnoreCase(rule.getRuleType())){
				rule.setCondition(genLink2CiRuleCondition(obj,""));
			}else{
				rule.setCondition(genLink2KpiRuleCondition(obj,""));
			}
			logger.iLog(rule.getCondition());
			ruleDao.saveLinkRule(rule);
			
/*			//发送更新缓存的信息到MQ
			JSONObject sendObj = new JSONObject();
			sendObj.put("tableName", "Rule");
			sendObj.put("action", "DEL");
			sendObj.put("key", rule.getId());
			sendObj.put("map", rule.rule2Map());
			MQTopicSender tSender = new MQTopicSender();
			tSender.send(sendObj.toString());
			tSender.destory();*/
			
			notifyService.refreshCache("Rule", "ADD", rule.getId(), rule.rule2Map());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			logger.eLog("保存匹配规则出错"+e.getMessage(),e);
			throw e;
		}
	}
	
	/**
	 * 编辑匹配规则
	 * 
	 * @param rule
	 * @throws Exception
	 */
	@Override
	public void editLinkRule(LinkRule rule) throws Exception{
		try{
			JSONObject obj = rule.getConditionJson();
			if(RuleUtil.PERF_LINK_CI.equalsIgnoreCase(rule.getRuleType())){
				rule.setCondition(genLink2CiRuleCondition(obj,""));
			}else{
				rule.setCondition(genLink2KpiRuleCondition(obj,""));
			}
			logger.iLog(rule.getCondition());
			ruleDao.updateLinkRule(rule);
			
			notifyService.refreshCache("Rule", "UPD", rule.getId(), rule.rule2Map());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			logger.eLog("更新匹配规则出错"+e.getMessage(),e);
			throw e;
		}
	}
	
	
	/**
	 * 删除匹配规则
	 * 
	 * @param id
	 * @throws Exception
	 */
	@Override
	public void deleteLinkRule(String id) throws Exception{
		try{
			//获取到rule对象
			LinkRule rule = this.getRuleById(id);
			
			ruleDao.deleteLinkRule(id);
			
			//更新缓存信息
			notifyService.refreshCache("Rule", "DEL", id, rule.rule2Map());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			//logger.error("更新匹配规则出错"+e.getMessage(),e);
			throw e;
		}
	}
	
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
	@Override
	public String genLink2CiRuleCondition(JSONObject condtionJson,String perfValKey) throws Exception{
		StringBuffer sb = new StringBuffer();
		try{
			sb.append(parsePerfAction(condtionJson,perfValKey)); //eventAttribute-value条件
			sb.append("@@");
			//ciAttribute-eventAttribute条件
			String perf2CiAction = parsePerf2CiAction(condtionJson);
			sb.append(perf2CiAction); 
			//ciAttribue-value条件
			String ciAction = parseCiAction(condtionJson);
			sb.append(ciAction); 
			if("".equals(perf2CiAction) && "".equals(ciAction)){
				sb.append("@@");
			}
			sb.append("");
		}catch(Exception e){
			logger.eLog("将页面字段转换成匹配到CI规则的condition时出错："+e.getMessage(),e);
			throw e;
		}
		return sb.toString();
	}
	
	
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
	@Override
	public String genLink2KpiRuleCondition(JSONObject condtionJson,String perfValKey) throws Exception{
		StringBuffer sb = new StringBuffer();
		try{
			sb.append(parsePerfAction(condtionJson,perfValKey));	//eventAttribute-value条件
			sb.append("@@");
			//kpiAttribute-eventAttribute条件
			String perf2KpiAction = parsePerf2KpiAction(condtionJson);
			sb.append(perf2KpiAction); 
			//kpiAttribue-value条件
			String kpiAction = parseKpiAction(condtionJson);
			sb.append(kpiAction);	
			if("".equals(perf2KpiAction) && "".equals(kpiAction)){
				sb.append("@@");
			}
			sb.append("");
		}catch(Exception e){
			logger.eLog("将页面字段转换成匹配到KPI规则的condition时出错："+e.getMessage(),e);
			throw e;
		}
		return sb.toString();
	}
	
	/**
	 * 解析匹配规则中关于性能数据【attribute-value】对应表的条件
	 * 
	 * @param JSONObject
	 * @param perfValKey Json信息中保存，性能数据的attribute-value条件的Key
	 * @return String 拼装好的bsh语句
	 * 				eg:"10.1.1.100".equals(o.get("ip") || "logicServer".equals(o.get("ciName")
	 */
	private String parsePerfAction(JSONObject condtionJson,String perfValKey) throws Exception{
		
		JSONArray perfValues = null;
		if("".equals(perfValKey)){
			perfValKey = "perfValues";
		}
		//如果没有设置性能数据的attribute-value条件 直接返回""
		try{
			perfValues = condtionJson.getJSONArray(perfValKey);
		}catch(Exception e){
			logger.iLog("Performace-value条件为空");
			return "";
		}
		
		String relationinfo = "";
		//组装事件的 属性-值 的筛选条件
		StringBuffer perfCondition = new StringBuffer();
		
		for(Object perfObj : perfValues){
			JSONObject perfVal = (JSONObject) perfObj;
			
			if(perfVal.getString("lineConnector").equals("and")){
				relationinfo = "&&";
			}else if(perfVal.getString("lineConnector").equals("or")){
				relationinfo = "||";
			}else{
				relationinfo ="";
			}
			perfCondition.append(relationinfo);
			perfCondition.append(perfVal.getString("leftBrackets"));
			perfCondition.append(" ");
			perfCondition.append(
					toPerfBsh(perfVal.getString("perfAttr"),perfVal.getString("operator"),
							perfVal.getString("perfVal")));
			perfCondition.append(" ");
			perfCondition.append(perfVal.getString("rightBrackets"));
		}
		
		return perfCondition.toString();
	}
	
	/**
	 * 将指定的事件属性-值转换为为Bsh语句
	 * 
	 * @param perfAttribute
	 * @param operator
	 * @param perfValue
	 * @return String 拼接好的事件表达式
	 */
	private String toPerfBsh(String perfAttribute,String operator,String perfValue) throws Exception{
		String returnStr = "";
		if("=".equals(operator.trim())){
			returnStr = "\""+perfValue+"\".equals(\"o.get(\""+perfAttribute+"\")\")";
		}else if("!=".equals(operator.trim())){
			returnStr = "!\""+perfValue+"\".equals(\"o.get(\""+perfAttribute+"\")\")";
		}else if("~=".equals(operator.trim())){
			returnStr = "(\"o.get(\""+perfAttribute+"\")\".contains(\""+perfValue+"\"))";
		}else if("!~=".equals(operator.trim())){
			returnStr = "!(\"o.get(\""+perfAttribute+"\")\".contains(\""+perfValue+"\"))";
		}
		
		return returnStr;
	}
	
	
	/**
	 * 解析匹配规则中关于性能数据和CI关联【CiAttribute-PerfAttribute】对应表的条件
	 * 
	 * @param JSONObject
	 * 
	 * @return String 拼装好的条件语句
	 * 				eg:this.ip=='o.get("ip")' || !(this.app~='o.get("app")' && this.categoryId='刀片'))
	 */
	private String parsePerf2CiAction(JSONObject condtionJson) throws Exception{
		
		String relationinfo = "";

		JSONArray linkPerf2Ci = null;
		//如果没有设置性能数据的attribute-value条件 直接返回""
		try{
			linkPerf2Ci = condtionJson.getJSONArray("perf2CiLink");
		}catch(Exception e){
			logger.iLog("CiAttribute-PerfAttribute条件为空");
			return "";
		}
		//组装事件的 属性-值 的筛选条件
		StringBuffer condition = new StringBuffer();
		
		for(int i=0;i<linkPerf2Ci.size();i++){
			JSONObject link = (JSONObject) linkPerf2Ci.get(i);
			
			if(link.getString("lineConnector").equals("and")){
				relationinfo = " and ";
			}else if(link.getString("lineConnector").equals("or")){
				relationinfo = " or ";
			}else{
				//如果没有选择行连接符 默认置为 or
				if(i>0){
					relationinfo =" or ";
				}
			}
			
			String linkCiAttribute = link.getString("selectedCiAttr");
			//如果不是Ci的基础属性需要加上“data$”
			if(!ciAttributeMap.containsKey(linkCiAttribute)){
				linkCiAttribute = "data$"+linkCiAttribute;
			}
		
			condition.append(relationinfo);
			if(i==0){ //分离出第一个连接符
				condition.append("@@");
			}
			condition.append(link.getString("leftBrackets"));
			condition.append(" ");
			condition.append(
					toLink2CiAciton(link.getString("selectedCate"),linkCiAttribute,
							link.getString("operator"),
							link.getString("perfAttr")));
			condition.append(" ");
			condition.append(link.getString("rightBrackets"));
		}
		
		return condition.toString();
	}
	
	/**
	 * 将指定的 [CI属性-表达式-perf属性] 转换为对应的SQL查询条件
	 * 
	 * @param category Ci 分类
	 * @param ciAttribute Ci 属性 [Ci分类+属性值 确定唯一Ci]
	 * @param operator 条件符
	 * @param perfAttribute 事件属性
	 * @return String 查询CI的SQL语句
	 */	
	private String toLink2CiAciton(String category, String ciAttribute,
			String operator, String perfAttribute) throws Exception {
		String returnStr = "";
		if("=".equals(operator.trim())){
			returnStr =  "(`"+ciAttribute+"` = 'o.get(\""+perfAttribute+"\")' and categoryId = '"+category+"') ";
		}else if("!=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` != 'o.get(\""+perfAttribute+"\")' and categoryId = '"+category+"')";
		}else if("~=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` like 'o.get(\""+perfAttribute+"\")' and categoryId = '"+category+"')";
		}else if("!~=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` not like 'o.get(\""+perfAttribute+"\")' and categoryId = '"+category+"')";
		}
		return returnStr;
	}
	
	/**
	 * 解析匹配规则中关于CI属性和CI值关联【CiAttribute-CiValue】对应表的条件
	 * 
	 * @param condtionJson
	 * @return String 拼装好的条件语句
	 * 				eg:this.ip='10.1.1.110' && !(this.app=~'.*logicServer*' && this.categroyId='logicSever')
	 */
	private String parseCiAction(JSONObject condtionJson) throws Exception{
		
		
		JSONArray ciValues = null;
		try{		
			ciValues = condtionJson.getJSONArray("ciValues");
		}catch(Exception e){
			logger.iLog("CiAttribute-CiValue条件为空");
			return "";
		}
		//获取到ci-performance的条件
		String perf2Link = condtionJson.getString("perf2CiLink").trim();
		//组装事件的 属性-值 的筛选条件
		StringBuffer condition = new StringBuffer();
		String relationinfo = "";
		for (int i=0;i<ciValues.size();i++) {
			JSONObject ci = (JSONObject) ciValues.get(i);
			//如果ci-performance不为空，所有ci-value的条件将作为一个过滤条件，
			//因此和ci-performance条件的连接符定为and
			if(i==0){
				if("".equals(perf2Link)){
					relationinfo = "";
				}else{
					//如果ci-performance的条件不为空时
					relationinfo = "and";
				}
			}else{
				if (ci.getString("lineConnector").equals("and")) {
					relationinfo = " and ";
				} else if (ci.getString("lineConnector").equals("or")) {
					relationinfo = " or ";
				} else {
					relationinfo = " or ";
				}
			}
			String ciAttribute = ci.getString("selectedCiAttr");
			// 如果不是Ci的基础属性需要加上“data$”
			if (!ciAttributeMap.containsKey(ciAttribute)) {
				ciAttribute = "data$" + ciAttribute;
			}

			condition.append(relationinfo);
			//当ci-performance条件为空时，加入@@分离出第一个连接符
			//否则用()将ci-value条件包起来
			if(i==0){
				if("".equals(perf2Link)){
					condition.append("@@");
				}else{
					condition.append(" (");
				}
			}
			condition.append(ci.getString("leftBrackets"));
			condition.append(" ");
			condition.append(toCiAction(ci.getString("selectedCate"),
					ciAttribute, ci.getString("operator"),
					ci.getString("ciVal")));
			condition.append(" ");
			condition.append(ci.getString("rightBrackets"));
			//如果ci-performance不为空需要补充“)”
			if(i==(ciValues.size()-1)){
				if(!("".equals(perf2Link))){
					condition.append(")");
				}
			}
			
		}
		return condition.toString();
	}
	
	
	/**
	 *  将指定的 [CI属性-表达式-ci值] 转换为对应的条件表达式
	 *  
	 * @param category Ci 分类
	 * @param ciAttribute Ci 属性 [Ci分类+属性值 确定唯一Ci]
	 * @param operator
	 * @param ciValue
	 * 
	 * @return String 条件表达式
	 */
	private String toCiAction(String category, String ciAttribute,
			String operator, String ciValue) throws Exception {
		String returnStr = "";
		if("=".equals(operator.trim())){
			returnStr =  "(`"+ciAttribute+"` = '"+ciValue+"' and categoryId = '"+category+"')";
		}else if("!=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` != '"+ciValue+"' and categoryId = '"+category+"')";
		}else if("~=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` like '"+ciValue+"' and categoryId = '"+category+"')";
		}else if("!~=".equals(operator.trim())){
			returnStr = "(`"+ciAttribute+"` not like '"+ciValue+"' and categoryId = '"+category+"')";
		}
		
		return returnStr;
	}
	
	/**
	 * 解析匹配规则中关于事件和KPI关联【KpiAttribute-EventAttribute】对应表的条件
	 * 
	 * @param JSONObject
	 * @return String 拼装好的SQL语句
	 * 				eg:kpi.id='o.get("id")' and not(kpi.name=~'.*o.get("KPINAME")*')
	 * @throws JSONException 
	 */
	private String parsePerf2KpiAction(JSONObject condtionJson) throws Exception{
		
		String relationinfo = "";

		JSONArray linkPerf2Kpi = null;
		//如果没有设置性能数据的attribute-value条件 直接返回""
		try{
			linkPerf2Kpi = condtionJson.getJSONArray("perf2KpiLink");
		}catch(Exception e){
			logger.iLog("KpiAttribute-PerfAttribute条件为空");
			return "";
		}
		//组装事件的 属性-值 的筛选条件
		StringBuffer condition = new StringBuffer();
		
		for(int i=0;i<linkPerf2Kpi.size();i++){
			JSONObject link = (JSONObject) linkPerf2Kpi.get(i);
			
			if(link.getString("lineConnector").equals("and")){
				relationinfo = " and ";
			}else if(link.getString("lineConnector").equals("or")){
				relationinfo = " or ";
			}else{
				//如果没有选择行连接符 默认置为 or
				if(i>0){
					relationinfo =" or ";
				}
			}
			
			condition.append(relationinfo);
			if(i==0){ //分离出第一个连接符
				condition.append("@@");
			}
			condition.append(link.getString("leftBrackets"));
			condition.append(" ");
			condition.append(
					toLink2KpiAction(
							//link.getString("selectedCate"),
							link.getString("selectedKpiAttr"),
							link.getString("operator"),
							link.getString("perfAttr")));
			condition.append(" ");
			condition.append(link.getString("rightBrackets"));
		}
		
		return condition.toString();		
	}
	
	/**
	 * 将指定的 [KPI属性-表达式-perf属性] 转换为对应的SQL查询条件
	 * 
	 * @param kpiAttribute kpi 属性 
	 * @param operator 条件符
	 * @param perfAttribute 事件属性
	 * @return String 查询KPI的SQL语句
	 */	
	private String toLink2KpiAction(String kpiAttribute,
			String operator, String perfAttribute) throws Exception {
		String returnStr = "";
		if("=".equals(operator.trim())){
			returnStr =  "`"+kpiAttribute+"` = 'o.get(\""+perfAttribute+"\")'";
		}else if("!=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` != 'o.get(\""+perfAttribute+"\")'";
		}else if("~=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` like 'o.get(\""+perfAttribute+"\")'";
		}else if("!~=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` not like 'o.get(\""+perfAttribute+"\")'";
		}
		return returnStr;
	}
	
	/**
	 * 解析匹配规则中关于事件和KPI关联【KpiAttribute-EventAttribute】对应表的条件
	 * 
	 * @param ruleLinkModel
	 * @return String 拼装好的查询语句语句
	 * 				eg:kpi.ip='10.1.1.110' and (kpi.app!='.*logicServer*')
	 * @throws Exception 
	 */
	private String parseKpiAction(JSONObject condtionJson) throws Exception{
		
		JSONArray kpiValues = null;
		try{		
			kpiValues = condtionJson.getJSONArray("kpiValues");
		}catch(Exception e){
			logger.iLog("KpiAttribute-KpiValue条件为空");
			return "";
		}
		//获取到kpi-performance的条件
		String perf2Link = condtionJson.getString("perf2KpiLink").trim();
		//组装事件的 属性-值 的筛选条件
		StringBuffer condition = new StringBuffer();
		String relationinfo = "";
		for (int i=0;i<kpiValues.size();i++) {
			JSONObject kpi = (JSONObject) kpiValues.get(i);
			
			//如果kpi-performance不为空，所有kpi-value的条件将作为一个过滤条件，
			//因此和kpi-performance条件的连接符定为and
			if(i==0){
				if("".equals(perf2Link)){
					relationinfo = "";
				}else{
					//如果ci-performance的条件不为空时
					relationinfo = "and";
				}
			}else{
				if (kpi.getString("lineConnector").equals("and")) {
					relationinfo = " and ";
				} else if (kpi.getString("lineConnector").equals("or")) {
					relationinfo = " or ";
				} else {
					relationinfo = "or";
				}
			}

			condition.append(relationinfo);
			
			//当kpi-performance条件为空时，加入@@分离出第一个连接符
			//否则用()将kpi-value条件包起来
			if(i==0){
				if("".equals(perf2Link)){
					condition.append("@@");
				}else{
					condition.append(" (");
				}
			}
			condition.append(kpi.getString("leftBrackets"));
			condition.append(" ");
			condition.append(toKpiAction(
					//kpi.getString("selectedCate"),
					kpi.getString("selectedKpiAttr"), kpi.getString("operator"),
					kpi.getString("kpiVal")));
			condition.append(" ");
			condition.append(kpi.getString("rightBrackets"));
			
			//如果kpi-performance不为空需要补充“)”
			if(i==(kpiValues.size()-1)){
				if(!("".equals(perf2Link))){
					condition.append(")");
				}
			}
		}
		return condition.toString();
	}
	
	
	/**
	 *  将指定的 [kpi属性-表达式-kpi值] 转换为对应的条件表达式
	 *  
	 * 
	 * @param kpiAttribute kpi 属性 [kpi分类+属性值 确定唯一kpi]
	 * @param operator
	 * @param kpiValue
	 * 
	 * @return String 条件表达式
	 */
	private String toKpiAction( String kpiAttribute,
			String operator, String kpiValue) throws Exception {
		String returnStr = "";
		if("=".equals(operator.trim())){
			returnStr =  "`"+kpiAttribute+"` = '"+kpiValue+"'";
		}else if("!=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` != '"+kpiValue+"'";
		}else if("~=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` like '"+kpiValue+"'";
		}else if("!~=".equals(operator.trim())){
			returnStr = "`"+kpiAttribute+"` not like '"+kpiValue+"'";
		}
		
		return returnStr;
	}
	
	
}
