package com.mmdb.ruleEngine;

import java.io.Reader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

public class EventDao {
	private static Log log = LogFactory.getLogger("EventDao");
	private static SqlSessionFactory sessionFactory = null;
	
	private static SqlSession getSession(){
		try{
			if(sessionFactory==null){
				String resource = "com/mmdb/ruleEngine/eventDaoConf.xml"; 
				Reader reader = Resources.getResourceAsReader(resource);
				sessionFactory = new SqlSessionFactoryBuilder().build(reader); 
			}
			return sessionFactory.openSession();
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
		}
		return null;
	}
	
	public static List<Map<String, Object>> getEventByTime(List<JSONArray> cis, Long startTime, Long endTime){
		SqlSession session = null;
		try{
			session = getSession();
			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
			Map<String, Object> input = new HashMap<String, Object>();
			StringBuilder ciSb = new StringBuilder();
			for(JSONArray ci:cis){
				ciSb.append("ciid='"+ci.getString(2).replace("'", "''")+"' ");
				ciSb.append("and cicategory='"+ci.getString(1).replace("'", "''")+"' ");
				ciSb.append("and ciscene='"+ci.getString(0).replace("'", "''")+"' or ");
			}
			input.put("ci", ciSb.length()>0 ? "("+ciSb.substring(0, ciSb.length()-4)+")":"1=1");
			input.put("startTime", new Timestamp(startTime));
			input.put("endTime", new Timestamp(endTime));
			System.out.println("getEventByTime===="+input.get("ci")+"----"+input.get("startTime")+"----"+input.get("endTime"));
			List list = session.selectList("com.mmdb.ruleEngine.EventDaoMapper.getEventByTime", input);
			if(list!=null){
				for(int i=0;i<list.size();i++){
					Map map = (Map)list.get(i);
					Map<String, Object> event = new HashMap<String, Object>();
					JSONArray ciJs = new JSONArray();
					ciJs.add(map.get("CISCENE")+"");
					ciJs.add(map.get("CICATEGORY")+"");
					ciJs.add(map.get("CIID")+"");
					event.put("ci", ciJs);
					event.put("arrivalTime", ((Timestamp)map.get("FIRSTOCCURRENCE")).getTime());
					event.put("modifyTime", ((Timestamp)map.get("LASTOCCURRENCE")).getTime());
					if(map.get("CLOSETIME")!=null){
						event.put("cTime", ((Date)map.get("CLOSETIME")).getTime());
					}
					event.put("severity", Long.parseLong(map.get("SEVERITY")+""));
					event.put("msg", map.get("SUMMARY")+"");
					result.add(event);
				}
			}
			return result;
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e.getMessage());
			return null;
		}finally{
			if(session!=null){
				session.close();
			}
			session = null;
		}
	}
}
