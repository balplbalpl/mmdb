package com.mmdb.service.mapping.impl;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;

import jdbc.JdbcConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import bsh.Interpreter;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.mapping.PerfToDbMapping;
import com.mmdb.model.mapping.storage.PerfToDbMapStorage;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.mapping.IPerfDbMapService;
import com.mmdb.service.notify.INotifyService;
import com.mmdb.service.ruleEngine.ILinkRuleService;
import com.mmdb.service.sync.PerfDbMapSync;
import com.mmdb.util.HexString;
import com.mmdb.util.JdbcOtherTools;

/**
 * DB数据集和性能数据映射Service实现类
 * 
 * @author yuhao.guan
 * @version 1.0 2015-7-19
 */
@Component("perfDbMapService")
public class PerfDbMapServiceImpl implements IPerfDbMapService {
	
	private Log logger = LogFactory.getLogger("PerfDbMapServiceImpl");
	
	@Autowired
	private PerfToDbMapStorage perfToDbMapStorage;
	
	@Autowired
	private ILinkRuleService linkRuleService;
	
	@Autowired
	private PerfDbMapSync perfDbMapSync;
	
	@Autowired
	private ICiInfoService ciInfoService;
	
	@Autowired
	private IKpiCateService kpicateService;
	
	@Autowired
	private IDataBaseConfigService dbConfigService;
	
	@Autowired
	private INotifyService notifyService;

	@Override
	public List<PerfToDbMapping> getAll() throws Exception {
		return perfToDbMapStorage.getAllMapping();
	}

	/**
	 * 通过名称查询映射
	 * 
	 * @param name
	 *            映射名称
	 * @return List<PerfToDbMapping>
	 * @throws Exception
	 */
	@Override
	public List<PerfToDbMapping> getByName(String name) throws Exception{
		return perfToDbMapStorage.getByName(name);
	}
	
	@Override
	public List<PerfToDbMapping> getByOwner(String username) throws Exception {
		return perfToDbMapStorage.getByOwner(username);
	}
	
	@Override
	public PerfToDbMapping getMappingById(String id) throws Exception {
		
		PerfToDbMapping mapping = perfToDbMapStorage.getById(id);
		if(mapping.getCiHex() != null && !"".equals(mapping.getCiHex())){
			CiInformation ci = ciInfoService.getById(mapping.getCiHex());
			if(ci!=null){
				mapping.setCiCategoryId(ci.getCategoryId());
				mapping.setCiCategoryName(ci.getCategoryName());
				mapping.setCiId(ci.getCiHex());
				mapping.setCiName(ci.getName());
			}
		}
		if(mapping.getKpiHex() != null && !"".equals(mapping.getKpiHex())){
			KpiInformation kpi = kpicateService.getKpiByHex(mapping.getKpiHex());
			if(kpi!=null){
				mapping.setKpiCategoryId(kpi.getKpiCategoryId());
				mapping.setKpiCategoryName(kpi.getKpiCategoryName());
				mapping.setKpiId(kpi.getId());
				mapping.setKpiName(kpi.getName());
			}
		}
		return mapping;
	}


	@Override
	public void save(PerfToDbMapping mapping) throws Exception {
		
		try{
			String ciCondition = linkRuleService.genLink2CiRuleCondition(
					mapping.getCiConditionJson(), "perfValuesInCi");
			String[] rules = ciCondition.split("@@@@");
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				String exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Ci")){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			mapping.setCiCondition(ciCondition);
		}catch(Exception e){
			logger.eLog("映射中的匹配CI规则转换成条件表达式时出错",e);
			throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
		}
		try{
			String kpiCondition = linkRuleService.genLink2KpiRuleCondition(
					mapping.getKpiConditionJson(), "perfValuesInKpi");
			String[] rules = kpiCondition.split("@@@@");
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				String exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Kpi")){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			mapping.setKpiCondition(kpiCondition);
		}catch(Exception e){
			logger.eLog("映射中的匹配KPI规则转换成条件表达式时出错",e);
			throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
		}
		
		try{
			perfToDbMapStorage.save(mapping);
			
			List<PerfToDbMapping> mappingList = this.getByName(mapping.getName());
			PerfToDbMapping savedMapping = mappingList.get(0);
			
			//通知缓存更新
			notifyService.refreshCache("PerfToDbMapping", "ADD",
					savedMapping.getId(), savedMapping.toMap());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			
			throw e;
		}
	}
	

	@Override
	public void deleteById(String id) throws Exception {
		try{
			PerfToDbMapping delMapping = this.getMappingById(id);
			
			perfToDbMapStorage.deleteById(id);
			
			//更新TP中缓存信息
			notifyService.refreshCache("PerfToDbMapping", "DEL",
					id, delMapping.toMap());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			throw e;
		}	
	}


	@Override
	public void update(PerfToDbMapping mapping) throws Exception {
		
		try{
			String ciCondition = linkRuleService.genLink2CiRuleCondition(
					mapping.getCiConditionJson(), "perfValuesInCi");
			String[] rules = ciCondition.split("@@@@");
			String exp = "";
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Ci")){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			mapping.setCiCondition(ciCondition);
		}catch(Exception e){
			logger.eLog("映射中的匹配CI规则转换成条件表达式时出错",e);
			throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
		}
		try{
			String kpiCondition = linkRuleService.genLink2KpiRuleCondition(
					mapping.getKpiConditionJson(), "perfValuesInKpi");
			String[] rules = kpiCondition.split("@@@@");
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				String exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Kpi")){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			mapping.setKpiCondition(kpiCondition);
		}catch(Exception e){
			logger.eLog("映射中的匹配KPI规则转换成条件表达式时出错",e);
			throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
		}
		
		try{
			perfToDbMapStorage.update(mapping);
			
			//更新TP中缓存信息
			notifyService.refreshCache("PerfToDbMapping", "UPD",
					mapping.getId(), mapping.toMap());
		}catch(JMSException je){
			logger.eLog("更新缓存信息异常,原因:"+je.getMessage(), je);
			throw je;
		}catch(Exception e){
			throw e;
		}	
	}

	@Override
	public Map<String, Integer> runNow(PerfToDbMapping mapping)
			throws Exception {
		return perfDbMapSync.run(mapping);
	}
	
	
	/**
	 * 为接收到的性能数据匹配CI信息
	 * 
	 * @param sourcePerf 接收到的性能数据
	 * @param condition 匹配条件
	 * @param matchedPerf 匹配完成后的性能
	 * @return boolean 是否匹配成功
	 */
	public boolean linkCi2Perf(JSONObject sourcePerf,String condition,
				Map<String,Object> matchedPerf) throws Exception{
		
		boolean matchedCi = false; //记录是否匹配到了CI信息
		//@@代表条件为空,拼装condition时提示
		if (condition == null || "@@@@".equals(condition.trim())){
			return false;
		}
		try {

			// 使用正则表达式 将condition条件中的o.get("ip")转换为具体的值
			// eg：ci.ip=o.get("ip")解析完之后应该为ci.ip='10.1.110'
			Pattern p = Pattern.compile("o.get\\(\"(.+?)\"\\)");
			Matcher m = p.matcher(condition);
			StringBuffer sb = new StringBuffer();
			while (m.find()) {
				// logger.info("正则匹配到的子串内容为： "+m.group(1));
				if (sourcePerf.has(m.group(1))) {
					m.appendReplacement(sb, sourcePerf.getString(m.group(1)));
				} else {
					// 如果是事件对象中没有的字段,去掉条件中的双引号
					// eg：kpi.ip=performance.get("ip")解析完之后应该为kpi.ip=performance.get(ip)
					m.appendReplacement(sb, "o.get(" + m.group(1) + ")");
				}
			}
			m.appendTail(sb);
			condition = sb.toString();

			// 匹配规则中关于perf value的配置条件
			String perfValCondition = condition.split("@@")[0];
			// 获取到连接ci-performance的第一个操作符
			//String operator = condition.split("@@")[1].trim();
			// 匹配规则中关于ci-performance和ci-value的配置条件
			String perf_ciCondition = condition.split("@@")[2];

			// 判断接收的事件是否符合performance-value条件
			Interpreter itp = new Interpreter();
			boolean matchEventValFilter;
			if (perfValCondition.trim().length() > 0) {
				matchEventValFilter = (Boolean) itp.eval(perfValCondition);
			} else {
				// 如果没有指定performance-value条件则所有事件都需要去匹配
				matchEventValFilter = true;
			}

			if (matchEventValFilter){
				/* 
				 * 这段逻辑已经废弃=========================
					如果事件符合指定的val-performance条件并且ci-performance条件的第一个连接符不是"and"
					则按ci-performance和ci-value条件进行ci匹配
					如果连接符是"and"则就没有必要再去验证ci-performance条件和ci-value条件
					//|| ("or".equals(operator) || "".equals(operator))) {
				 * =======================================
				 */

				logger.iLog("性能数据符合Rule的performance-value条件,"
						+ "开始进行CI匹配,SQL语句为:" + perf_ciCondition);

				if (perf_ciCondition.length() > 0) {
					// 连接mongodb，并执行查询，进行CI匹配
					List<CiInformation> ciList 
						= ciInfoService.qureyByWhereSQL(perf_ciCondition);

					if (ciList.size() > 0) {
						// 取匹配到的第一个CI
						CiInformation ci = ciList.get(0);
						logger.iLog("成功匹配到CI：" + ci.getName());
						// 将CI信息填充到性能数据中
						matchedPerf.put("ciName", ci.getName());
						matchedPerf.put("ciCategory", ci.getCategory().getName());

						// perfMap.setCiData(CiInformation);
						matchedCi = true;
					}

				}
			}
		} catch (Exception e) {
			logger.eLog("匹配事件的CI信息时发生错误:" + e.getMessage(), e);
		}
		if (matchedCi == false) {
			logger.iLog("未能匹配到CI信息!");
		}
		return matchedCi;
	}	
	
	
	/**
	 * 为接收到的性能数据匹配KPI信息
	 * 
	 * @param sourcePerf 接收到的性能数据
	 * @param condition 匹配条件
	 * @param matchedPerf 匹配完成后的性能
	 * @return boolean 是否匹配成功
	 */
	public boolean linkKpi2Perf(JSONObject sourcePerf,String condition,
				Map<String,Object> matchedPerf) throws Exception{
		
		boolean matched = false; //记录是否匹配到了KPI信息
		if (condition == null || "@@@@".equals(condition.trim())){
			return false;
		}			
		try {
			//使用正则表达式 将condition条件中的o.get("ip")转换为具体的值
			//eg：kpi.ip=o.get("ip")解析完之后应该为kpi.ip='10.1.110'
			Pattern p = Pattern.compile("o.get\\(\"(.+?)\"\\)");        
	        Matcher m = p.matcher(condition); 
	        StringBuffer sb = new StringBuffer(); 
	        while(m.find()){
	             //logger.info("正则匹配到的子串内容为： "+m.group(1)); 
	             if(sourcePerf.has(m.group(1))){
	            	 m.appendReplacement(sb, sourcePerf.getString(m.group(1)));
	             }else{ 
	            	    //如果是事件对象中没有的字段,去掉条件中的双引号
		            	//eg：kpi.ip=performance.get("ip")解析完之后应该为kpi.ip=performance.get(ip)
		            	 m.appendReplacement(sb, "o.get("+m.group(1)+")");
		         }
	        }
	        m.appendTail(sb); 
	        condition = sb.toString();
	        
			//匹配规则中关于perf value的配置条件
			String perfValCondition = condition.split("@@")[0];
			//获取到连接kpi-performance的第一个操作符
			//String operator = condition.split("@@")[1].trim();
			//匹配规则中关于kpi-performance和kpi-value的配置条件
			String perf_kpiCondition = condition.split("@@")[2];
			
			//判断接收的事件是否符合performance-value条件
			Interpreter itp = new Interpreter();
			boolean matchEventValFilter;
			if(perfValCondition.trim().length()>0){
				matchEventValFilter = (Boolean)itp.eval(perfValCondition); 
			}else{
				//如果没有指定performance-value条件则所有事件都需要去匹配
				matchEventValFilter = true;
			}
			
			if(matchEventValFilter){
				//此段逻辑已经废弃=======================
				//如果事件符合指定的val-performance条件并且kpi-performance条件的第一个连接符不是"and"
				//则按kpi-performance和kpi-value条件进行kpi匹配
				//如果连接符是"and"则就没有必要再去验证kpi-performance条件和kpi-value条件				
				//|| ("or".equals(operator)||"".equals(operator))){
				//======================================
				logger.iLog("性能数据符合Rule的performance-value条件," +
						"开始进行KPI匹配,SQL语句为:"+perf_kpiCondition);
				if(perf_kpiCondition.length() > 0){
			       //连接mongodb，并执行查询，进行kpi匹配
			       List<KpiInformation> kpiList = 
			    		   kpicateService.findBySql(perf_kpiCondition);
			        
			        if(kpiList.size()>0){
			        	//取匹配到的第一个KPI
			        	KpiInformation kpi = kpiList.get(0);
			        	logger.iLog("成功匹配到KPI："+kpi.getName());
			        	
						String kpiHex = HexString.decode(kpi.getKpiHex());
						JSONArray kpiNames =  JSONArray.fromObject(kpiHex);
			        	
						matchedPerf.put("kpiName", kpi.getName());
						matchedPerf.put("kpiCategory", kpiNames.getString(0));
						matchedPerf.put("kpiId", kpi.getId());
						matchedPerf.put("kpiCategoryId", kpi.getKpiCategoryId());
						matchedPerf.put("unit", kpi.getUnit());
						
						matched = true; 
			        }
				}
			}
		}catch(Exception e){
			logger.eLog("匹配事件的KPI信息时发生错误:"+e.getMessage(),e);
		}
		if(matched==false){
			logger.iLog("未能匹配到KPI信息!");
		}
		return matched;
	}
	
	/**
	 * 预览映射匹配结果
	 * 
	 * @param mapping 映射对象
	 * @return Map<String, List<?>> 匹配到数据List及未匹配到数据List
	 * @throws Exception
	 */
    @Override
	public Map<String, List<?>> preView(PerfToDbMapping mapping) throws Exception {
    	Map <String,List<?>>  retMap = new HashMap<String,List<?>>();
		try{
			//将ci匹配条件转换成规则表达式
			String ciCondition = linkRuleService.genLink2CiRuleCondition(
					mapping.getCiConditionJson(), "perfValuesInCi");
			String[] rules = ciCondition.split("@@@@");
			String exp = "";
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Ci")){
					throw new Exception("映射中的匹配CI规则转换成条件表达式时出错");
				}
			}
			mapping.setCiCondition(ciCondition);
			//将kpi匹配条件转换成规则表达式
			String kpiCondition = linkRuleService.genLink2KpiRuleCondition(
					mapping.getKpiConditionJson(), "perfValuesInKpi");
			rules = kpiCondition.split("@@@@");
			if(rules.length>0){
				if(!Tool.matchPerfMapping1(rules[0])){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			if(rules.length>1){
				exp = Tool.matchPerfMapping2(rules[1]);
				if(!perfToDbMapStorage.matchPerfMapping2(exp, "Kpi")){
					throw new Exception("映射中的匹配KPI规则转换成条件表达式时出错");
				}
			}
			mapping.setKpiCondition(kpiCondition);
		}catch(Exception e){
			logger.eLog("映射中的匹配规则转换成条件表达式时出错",e);
			//如果转换匹配规则条件出错，肯定匹配不上了
			throw e;
		}
		//匹配成功列表
    	List<Map<String,Object>> matchedList = new LinkedList<Map<String,Object>>();
    	//匹配成功信息的原始列表
    	List<Map<String,Object>> matchedSourceList = new LinkedList<Map<String,Object>>();
		//匹配失败列表
    	List<Map<String,Object>> unMatchedList = new LinkedList<Map<String,Object>>();
    	
    	//匹配成功记录数
    	int matchedCount = 0;
    	//未匹配成功记录数
    	int unMatchedCount = 0;
    	
        Connection connection = null;
        try {
            DataBaseConfig dc = dbConfigService.getById(mapping.getDataSource().getDatabaseConfigId());
            DataSourcePool ds = mapping.getDataSource();
            
            //获取字段映射关系
            JSONObject fieldMap = mapping.getFieldMap();
            String valExp = mapping.getValExp();
            //获取自定义字段
            JSONObject customFieldsMap = mapping.getCustomFieldsMap();
            Map<String, String> am = dc.asMap();
            if (dc.getRac()) {
                am.put("url", dc.getRacAddress());
            }
            connection = JdbcOtherTools.getConnection(dc.getRac(), am);
            if (connection == null) {
                throw new Exception("获取数据库连接失败");
            }
            
            //-----------------------------------------
            //是否是增量更新
            boolean isAddSync = true;//pdMapping.getActive();
            String flag = mapping.getIsAddSync();
            if(flag!=null && !"".equals(flag)){
            	isAddSync= Boolean.parseBoolean(flag);
            }
            
            String timeColumnName = "";
            String newSql = "";
            if(isAddSync){
	        	Iterator<?> fieldKey= fieldMap.keys();
	        	while(fieldKey.hasNext()){
	        		 //MMDB的性能数据字段
	        		 String key = fieldKey.next().toString();
	        		 //获取到时间字段的字段名
	        		 if("time".equalsIgnoreCase(key)){
	        			 timeColumnName = fieldMap.getString(key);;
	        		 }
	        	}
	        	//如果是增量更新,为自定义SQL增加上时间段信息
	        	newSql = this.genTimeAreaSql(isAddSync, timeColumnName, connection, ds);
            }
        	if("".equals(newSql)){
        		newSql = ds.getCustomSql();
        	}
        	logger.dLog("The query sql is:"+newSql);
        	//-------------------------------------------
            
            logger.dLog("同步DB[" + ds.getTableName() + "]表数据");
            int count = JdbcConnection.getCountSize(connection, ds.getSchema(), 
            		ds.getTableName(),newSql);
            logger.dLog("COUNT:" + count);
            int pageSize = 1000;
            int page = count % pageSize == 0 ? count / pageSize : (count / pageSize) + 1;
            logger.dLog("页数:" + page);
            
            //以CIName为主键 存放所有CI信息
            Map<String,CiInformation> ciNameMap = new HashMap<String,CiInformation>();
            //以CIHex为主键 存放所有CI信息
            Map<String,CiInformation> ciHexMap = new HashMap<String,CiInformation>();
            
            //以KPIName为主键 存放所有CI信息
            Map<String,KpiInformation> kpiNameMap = new HashMap<String,KpiInformation>();
            //以KPIHex为主键 存放所有CI信息
            Map<String,KpiInformation> kpiHexMap = new HashMap<String,KpiInformation>(); 
            
            if(count>0){
	            //获取到所有的CI，将CI缓存到Map中
	            List<CiInformation> ciList = ciInfoService.getAll();
	            for(CiInformation ci:ciList){
	            	ciNameMap.put(ci.getName(), ci);
	            	ciHexMap.put(ci.getCiHex(), ci);
	            }
	            
	            List<KpiInformation> kpiList = kpicateService.getAllKpi();
	            for(KpiInformation kpi:kpiList){
	            	kpiNameMap.put(kpi.getName(), kpi);
	            	kpiHexMap.put(kpi.getKpiHex(), kpi);
	            }
            }
            
            for (int i = 0; i < page; i++) {
                List<Map<String, Object>> data;
                int startCount = i * pageSize + 1, endCount = (i + 1) * pageSize;
                logger.iLog("startCount:" + startCount + ",endCount:" + endCount);
                if (ds.isSelf()) {
                    data = JdbcConnection.getDataByTable(connection, "", "", newSql, startCount, endCount);
                } else {
                    data = JdbcConnection.getDataByTable(connection, ds.getSchema(), ds.getTableName(), "", startCount, endCount);
                }
                if (data == null) {
                    throw new Exception("获取数据库数据失败");
                }
                for (Map<String, Object> m : data) {
                	try{
	                	Map<String,Object> matchedPerf = new HashMap<String,Object>();
	                	//源性能数据
	                	JSONObject sourcePerf = JSONObject.fromObject(m);
	                	
	                	Iterator<?> objkey= fieldMap.keys();
	                	while(objkey.hasNext()){
	                		 //MMDB的性能数据字段
	                		 String key = objkey.next().toString();
	                		 //获取到被映射外部数据库的字段
	                		 String outDbKey = fieldMap.getString(key);
	                		 
	                   		 //如果是val需要转成double
	                		 if("val".equalsIgnoreCase(key)){
	                			 if(customFieldsMap.containsKey(key)){
	                				 //如果是自定义字段，outDbkey就是这个字段的值
	                    			 double val = 
	                    					 Tool.executePerfVal(outDbKey, valExp);
	                    			 sourcePerf.put(key, val);
	                			 }else{
	                    			 double val = 
	                    					 Tool.executePerfVal((String)m.get(outDbKey), valExp);
	                    			 sourcePerf.put(key, val);
	                			 }
	                		 }else if("time".equalsIgnoreCase(key)){
	                			 Long time = -1L;
	                			 if(customFieldsMap.containsKey(key)){
	                				 //如果是自定义字段，outDbkey就是这个字段的值
		                			 time = Tool.transPerfValDate(outDbKey);
	                			 }else{
		                			 time = Tool.transPerfValDate(m.get(outDbKey));
	                			 }
	                			 if(time != -1L){
	                				 sourcePerf.put(key, time);
	                			 }else{ //如果时间没有能成功转换，存入当前时间
	                				 sourcePerf.put(key,System.currentTimeMillis());
	                			 }
	                		 }else if("ci".equalsIgnoreCase(key)){
	                			 CiInformation ciInfo = null;
	                			 //ci如果是自定义值，用户应该选择分类和名称，所以使用HexId获取CI
	                			 if(customFieldsMap.containsKey(key)){
	                				 //ciInfo = ciNameMap.get(outDbKey);
	                				 ciInfo = ciHexMap.get(mapping.getCiHex());
	                			 }else{
	                				 ciInfo = ciNameMap.get(m.get(outDbKey));
	                			 }
                				 if(ciInfo != null){
                    				 sourcePerf.put("ciName", ciInfo.getName());
                    				 sourcePerf.put("ciId", ciInfo.getId());
                    				 sourcePerf.put("ciCategoryId", ciInfo.getCategoryId());
                    				 sourcePerf.put("ciCategoryName", ciInfo.getCategoryName());
                				 }
	                		 }else if("kpi".equalsIgnoreCase(key)){
	                			 KpiInformation kpiInfo = null;
	                			 //kpi如果是自定义值，用户应该选择分类和名称，所以使用HexId获取KPI
	                			 if(customFieldsMap.containsKey(key)){
	                				// kpiInfo = kpiNameMap.get(outDbKey);
	                				 kpiInfo = kpiHexMap.get(mapping.getKpiHex());
	                			 }else{
	                				 kpiInfo = kpiNameMap.get(m.get(outDbKey));
	                			 }
                				 if(kpiInfo != null){
                    				 sourcePerf.put("kpiName", kpiInfo.getName());
                    				 sourcePerf.put("kpiId", kpiInfo.getId());
                    				 sourcePerf.put("kpiCategoryId", kpiInfo.getKpiCategoryId());
                    				 sourcePerf.put("kpiCategoryName", kpiInfo.getKpiCategoryName());
                				 }
	                		 }else{
	                			 if(customFieldsMap.containsKey(key)){
	                				 //如果是自定义字段，outDbkey就是这个字段的值
	                				 sourcePerf.put(key,outDbKey);
	                			 }else{
	                				 sourcePerf.put(key, m.get(outDbKey));
	                			 }
	                		 }
	                	}
	                	
	                	//完善固定信息
	                	matchedPerf.put("value", sourcePerf.getString("val"));
	                	if(sourcePerf.containsKey("instance")){
	                		matchedPerf.put("instance", sourcePerf.getString("instance"));
	                	}
	                	if(sourcePerf.containsKey("time")){
		    				String time = sourcePerf.getString("time");
		    				//SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    				//Date perfDate = sdf.parse(time);
		    				//long longTime = perfDate.getTime();
		    				//matchedPerf.put("time", longTime);
		    				matchedPerf.put("time", time);
	                	}else{	//如果映射中没有指定时间字段，以当前时间作为采集时间
	                		matchedPerf.put("time", System.currentTimeMillis());
	                	}
	                	
	                	boolean isMatchedCi = false;
	                	//如果filedMap中包含ci字段，证明是通过数据库字段匹配CI而不是通过匹配规则去匹配CI
	                	if(fieldMap.containsKey("ci")){
	                		//如果这两个字段不为空，证明匹配到了CI
	                		if(sourcePerf.containsKey("ciId")&&sourcePerf.containsKey("ciCategoryId")){
	                			matchedPerf.put("ciName", sourcePerf.get("ciName"));
	                			matchedPerf.put("ciId", sourcePerf.get("ciId"));
	                			matchedPerf.put("ciCategoryId", sourcePerf.get("ciCategoryId"));
	                			matchedPerf.put("ciCategory", sourcePerf.get("ciCategoryName"));
	                			isMatchedCi = true;
	                			//如果没有匹配到，再通过匹配规则去匹配
	                			if(isMatchedCi == false){
	                				isMatchedCi = linkCi2Perf(sourcePerf, mapping.getCiCondition(),matchedPerf);
	                			}
	                		}
	                	}else{
		    				//通过匹配规则去匹配CI
		                	isMatchedCi = 
		                			linkCi2Perf(sourcePerf, mapping.getCiCondition(),matchedPerf);
	                	}
	                	//是否匹配到了KPI
	                	boolean isMatchedKpi = false;
	                	//如果filedMap中包含kpi字段，证明是通过数据库字段匹配KPI而不是通过匹配规则去匹配KPI
	                	if(fieldMap.containsKey("kpi")){
	                		if(sourcePerf.containsKey("kpiId")&&sourcePerf.containsKey("kpiCategoryId")){
	                			matchedPerf.put("kpiName", sourcePerf.get("kpiName"));
	                			matchedPerf.put("kpiId", sourcePerf.get("kpiId"));
	                			matchedPerf.put("kpiCategoryId", sourcePerf.get("kpiCategoryId"));
	                			matchedPerf.put("kpiCategory", sourcePerf.get("kpiCategoryName"));
	                			isMatchedKpi = true;
	                		}
	                		//如果自定义没有匹配到，再通过匹配规则去匹配
	                		if(isMatchedKpi == false){
		                		isMatchedKpi = linkKpi2Perf(sourcePerf, mapping.getKpiCondition(),matchedPerf);
	                		}
	                	}else{
		    				//通过匹配规则去匹配KPI
	                		isMatchedKpi = linkKpi2Perf(sourcePerf, mapping.getKpiCondition(),matchedPerf);
	                	}
	                	
	                	if(isMatchedCi&&isMatchedKpi){
	                		//modify at 2015-9-19，匹配成功列表加上限制，最多存放5000条数据
	                		//TODO 记录条数，将匹配成功的总数和未匹配成功的总数显示到前台
	                		if(matchedList.size() < 5000){
		                		matchedList.add(matchedPerf);
		                		//将匹配到的原始信息也保存到List中
		                		matchedSourceList.add(m);
	                		}
	                		matchedCount++;
	                	}else{
	                		//modify at 2015-9-19，匹配未成功列表也加上限制，最多存放5000条数据
	                		if(unMatchedList.size() < 5000){
	                			unMatchedList.add(m);
	                		}
	                		unMatchedCount++;
	                	}
                	}catch(Exception e){
                		unMatchedList.add(m);
                		logger.dLog("预览过程中发生错误"+e);
                		continue;
                	}
                }
            }
        } finally {
            JdbcOtherTools.closeConnection(connection);
        }
        logger.dLog("匹配成功["+matchedCount+"]条记录");
        logger.dLog("未匹配成功["+unMatchedCount+"]条记录");
        //预览，默认显示100条数据
        if(matchedList.size()>100){
        	matchedList = matchedList.subList(0, 100);
        	matchedSourceList = matchedSourceList.subList(0, 100);
        }
        if(unMatchedList.size()>100){
        	unMatchedList = unMatchedList.subList(0, 100);
        }
        retMap.put("matchedList", matchedList);
        retMap.put("matchedSourceList",matchedSourceList);
        retMap.put("unMatchedList", unMatchedList);
        return retMap;
    }

    /**
     * 为数据集的自定义sql增加时间条件限制，目前时间条件为：当前系统时间-5分钟
     * 
     * @param isAddSync 是否是增量更新
     * @param timeColumnName 时间字段的字段名
     * @param connection 数据库连接
     * @param ds 数据集对象
     * 
     * @return 包含时间段信息的SQL
     * @throws Exception
     */
    public String genTimeAreaSql(Boolean isAddSync,String timeColumnName,
    		Connection connection,DataSourcePool ds) throws Exception{
    	String newSql = "";
    	try{
			//如果是增量更新，并且从外部数据库中成功获取到了时间字段
			if(isAddSync && !"".equals(timeColumnName)){
				String customSql = ds.getCustomSql();
				Map<String, Map<String, String>> columns = null;
				//获取到所有的字段信息
				if(customSql!=null && customSql.length()>0){
					columns = JdbcConnection.getColumns(connection, ds.getSchema(), "", customSql);
				}else{
					columns = JdbcConnection.getColumns(connection, ds.getSchema(), ds.getTableName(), "");
				}
				//获取到数据库中的时间字段的字段类型
				Map<String,String> columnMap = columns.get(timeColumnName);
				String columnType = columnMap.get("字段类型");
				
				String dbType = JdbcConnection.getType(connection);
				
				//如果时间是LONG型的
				if("LONG".equalsIgnoreCase(columnType)){
					long endTime = System.currentTimeMillis();
					//粒度设定为5分钟
					long startTime = endTime - 300*1000;
					if(customSql!=null && customSql.length()>0){
						newSql = "select * from ("+customSql+") as uinnova " +
								"where uinnova."+timeColumnName+" < "+endTime + " and uinnova."+timeColumnName+" > "+startTime;
					}else{
						//如果自定义SQL为空，使用db.tableName作为自定义SQL
						newSql = "select * from "+ ds.getSchema()+"."+ds.getTableName()+" as uinnova " +
								"where uinnova."+timeColumnName+" < "+endTime + " and uinnova."+timeColumnName+" > "+startTime;
					}
					
				}else if("DATE".equalsIgnoreCase(columnType) || "DATETIME".equalsIgnoreCase(columnType)
						|| "TIMESTAMP".equalsIgnoreCase(columnType) 
						|| "DATETIME2".equalsIgnoreCase(columnType)){
					 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					 Calendar nowTime = Calendar.getInstance();
					 String endTime = sdf.format(nowTime.getTime());
					 nowTime.add(Calendar.MINUTE, -5);
					 String startTime = sdf.format(nowTime.getTime());
		 			if(customSql!=null && customSql.length()>0){
		 				customSql = "("+customSql+")";
						newSql = genSqlByDbType(dbType, customSql, timeColumnName, endTime, startTime);
					}else{
						//如果自定义SQL为空，使用db.tableName作为自定义SQL
						customSql = ds.getSchema()+"."+ds.getTableName();
						newSql = genSqlByDbType(dbType, customSql, timeColumnName, endTime, startTime);
					}
				}
			}
			return newSql;
    	}catch (Exception e){
    		logger.eLog("同步性能数据，生成时间段SQL出错",e);
    		return newSql;
    	}
    }
    
    /**
     * 通过类型生成SQL(除了Oracle语法不同需特殊处理，其余三个语法其实可以通用)
     * 
     * @param dbType 数据库类型
     * @param customSql 自定义SQL
     * @param timeColumnName 时间字段名
     * @param endTime 结束时间
     * @param startTime 开始时间
     */
    private String genSqlByDbType(String dbType,String customSql,
    		String timeColumnName,String endTime,String startTime){
    	
    	String newSql = "";
    	if ((dbType.equalsIgnoreCase("oracle")) || (dbType.equalsIgnoreCase("oraclerac"))) {
    		newSql = "select * from "+customSql+" as uinnova " +
    				"where uinnova."+timeColumnName+" between to_date('"+startTime + "','yyyy-mm-dd hh24:mi:ss') and to_date('"+endTime+"','yyyy-mm-dd hh24:mi:ss')";

    	} else {
    		newSql = "select * from "+customSql+" as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"'";
    	}/* else if (dbType.equals("mysql")) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";

    	} else if ((dbType.equals("sqlserver")) || (dbType.equals("sqlserver2000"))) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";
    	} else if (dbType.equals("db2")) {
        	customSql = "select * from ("+customSql+") as uinnova " +
    				"where uinnova."+timeColumnName+" < '"+endTime + "' and uinnova."+timeColumnName+" >= '"+startTime+"')";
    	}*/
    	return newSql;
    }
}
