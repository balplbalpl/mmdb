package com.mmdb.ruleEngine;

import java.util.HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.SqlSession;
import org.springframework.web.context.ContextLoaderListener;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;


public class Dao {
	private static Log log = LogFactory.getLogger("Dao");
	private static SqlSession sqlSession = (SqlSession) new ContextLoaderListener().getCurrentWebApplicationContext().getBean("sqlSession");
	
	/*public static String insertEvent(JSONArray event) {
		try{Long a = System.currentTimeMillis();
			String eid = "";
			Map<String, Object> param = new HashMap<String, Object>();
			String sql1 = "";
			String sql2 = "";
			String sql3 = "";
			String sql4 = "";
			for(int i=0;i<event.size();i++){
				JSONObject attr = event.getJSONObject(i);
				if(attr.getString("type").equals("string")){
					sql1 = sql1 + "'" + attr.getString("val").replace("'", "''") + "',";
				}else{
					sql1 = sql1 +  attr.getString("val") + ",";
				}
				sql2 = sql2 + attr.getString("name") + ",";
				if(!attr.getString("name").equals("eid")){
					sql3 = sql3 + attr.getString("name") + "=vals." + attr.getString("name") + ",";
				}else{
					eid = attr.getString("val");
				}
				sql4 = sql4 + "vals." + attr.getString("name") + ",";
			}
			sql1 = sql1.substring(0, sql1.length()-1);
			sql2 = sql2.substring(0, sql2.length()-1);
			sql3 = sql3.substring(0, sql3.length()-1);
			sql4 = sql4.substring(0, sql4.length()-1);
			param.put("sql1", sql1);
			param.put("sql2", sql2);
			param.put("sql3", sql3);
			param.put("sql4", sql4);Long a1 = System.currentTimeMillis();
			sqlSession.update("hsql.insertEvent", param);
			Long b = System.currentTimeMillis();System.out.println("equals select neo4j time===="+(b-a));
			return eid;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static void ciLinkToEvent(JSONArray cis, String eid) {
		try{
			for(int i=0;i<cis.size();i++){
				JSONObject ci = cis.getJSONObject(i);
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("ci", ci.toString().replace("'", "''"));
				param.put("eid", eid.replace("'", "''"));
				sqlSession.update("hsql.ciLinkToEvent", param);
			}
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
		}
	}

	public static Set<String> getKpiSet(){
		try{
			List list = sqlSession.selectList("hsql.getKpiSet");
			Set<String> set = new HashSet<String>();
			if(list!=null){
				for(int i=0;i<list.size();i++){
					Map map = (Map)list.get(i);
					if(map.get("KPI")!=null){
						set.add((String)map.get("KPI"));
					}
				}
			}
			return set;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static Map<String, List<Map<String, Object>>> getKpiMap(){
		try{
			List list = sqlSession.selectList("hsql.getKpiMap");
			Map<String, List<Map<String, Object>>> result = new HashMap<String, List<Map<String, Object>>>();
			if(list!=null){
				for(int i=0;i<list.size();i++){
					Map map = (Map)list.get(i);
					if(map.get("KPI")!=null){
						set.add((String)map.get("KPI"));
					}
				}
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static void addCiKpiIndi(JSONArray cis, JSONObject kpi, String indi){
		try{
			Long timestamp = System.currentTimeMillis()/1000L;
			for(int i=0;i<cis.size();i++){
				JSONArray ci = cis.getJSONArray(i);
				Map<String, Object> param=new HashMap<String, Object>();
				param.put("categoryId", ci.getString(0));
				param.put("ciid", ci.getString(1));
				param.put("kpi", kpi.toString());
				param.put("indi", indi);
				param.put("timestamp", timestamp);
				sqlSession.update("hsql.addCiKpiIndi", param);
			}
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
		}
	}*/
	
	public static List<String> getAllIndi(){
		try{
			List list = sqlSession.selectList("hsql.getAllIndi");
			List<String> result = new ArrayList<String>();
			if(list!=null){
				for(int i=0;i<list.size();i++){
					if(list.get(i)!=null){
						Map map = (Map)list.get(i);
						if(map.get("INDI")!=null){
							result.add((String)map.get("INDI"));
						}
					}
				}
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static void addIndi(String indi){
		try{
			Map map = new HashMap();
			if(indi!=null){
				map.put("indi", indi);
				sqlSession.insert("hsql.addIndi", map);
			}
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
		}
	}
	
	/*public static List queryEventByTime(Long startTime, Long endTime){
		try{
			Map<String, Long> param = new HashMap<String, Long>();
			param.put("startTime", startTime);
			param.put("endTime", endTime);
			List list = sqlSession.selectList("hsql.queryEventByTime", param);
			return list;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static List queryOpenEvent(){
		try{
			List list = sqlSession.selectList("hsql.queryOpenEvent");
			return list;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static List queryOpenEventByCi(String ci){
		try{
			Map param = new HashMap();
			param.put("ci", ci);
			List list = sqlSession.selectList("hsql.queryOpenEventByCi", param);
			return list;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static Integer getIndiByCi(String categoryId, String id){
		try{
			Map param = new HashMap();
			param.put("categoryId", categoryId);
			param.put("id", id);
			List list = sqlSession.selectList("hsql.getIndiByCi", param);
			return list.size();
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static Map<String, Map<String, Long>> getKpiCiByTime(Long endTime){
		try{
			Map<String, Long> param = new HashMap<String, Long>();
			param.put("endTime", endTime/1000L);
			List list = sqlSession.selectList("hsql.getKpiCiByTime", param);
			Map<String, Map<String, Long>> result = new HashMap<String, Map<String, Long>>();
			JSONArray curCi = null;
			Map<String, Long> indiMap = null;
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				JSONArray tmpCi = new JSONArray();
				tmpCi.add((String)map.get("CATEGORYID"));
				tmpCi.add((String)map.get("CIID"));
				if(curCi==null){
					curCi = tmpCi;
					indiMap = new HashMap<String, Long>();
					indiMap.put((String)map.get("INDI"), Long.parseLong(map.get("MT")+""));
				}else{
					if(curCi.equals(tmpCi)){
						indiMap.put((String)map.get("INDI"), Long.parseLong(map.get("MT")+""));
					}else{
						result.put(curCi.toString(), indiMap);
						curCi = tmpCi;
						indiMap = new HashMap<String, Long>();
						indiMap.put((String)map.get("INDI"), Long.parseLong(map.get("MT")+""));
					}
				}
			}
			if(curCi!=null){
				result.put(curCi.toString(), indiMap);
			}
			
			return result;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}
	
	public static Map<String, String> getKpiByCi(String categoryId, String id, Long endTime){
		try{
			Map param = new HashMap();
			param.put("categoryId", categoryId);
			param.put("id", id);
			param.put("endTime", endTime/1000L);
			List list = sqlSession.selectList("hsql.getKpiByCi", param);
			Map<String, Map<String, Object>> kpiMap = new HashMap<String, Map<String, Object>>();
			for(int i=0;i<list.size();i++){
				Map map = (Map)list.get(i);
				String indi = (String)map.get("INDI");
				Long timestamp = Long.parseLong(map.get("TIMESTAMP")+"");
				if(kpiMap.get(indi)==null){
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("kpi", (String)map.get("KPI"));
					m.put("timestamp", timestamp);
					kpiMap.put(indi, m);
				}else{
					if((Long)kpiMap.get(indi).get("timestamp")<timestamp){
						Map<String, Object> m = new HashMap<String, Object>();
						m.put("kpi", (String)map.get("KPI"));
						m.put("timestamp", timestamp);
						kpiMap.put(indi, m);
					}
				}
			}
			Map<String, String> result = new HashMap<String, String>();
			Set<String> kpiSet = kpiMap.keySet();
			Iterator<String> kpiIt = kpiSet.iterator();
			while(kpiIt.hasNext()){
				String indi = kpiIt.next();
				String kpi = (String)kpiMap.get(indi).get("kpi");
				result.put(kpi, indi);
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}
	}*/
}
