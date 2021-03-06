package com.mmdb.model.db.neo4jdb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;


/**
 * 
 * @author TY
 * @version 2015年5月15日
 */
public class Neo4jDao {

	/**
	 * 通过cypher获取对象数据
	 * 
	 * @param sql
	 * @return
	 */
	public static List<JSONObject> getDataMap(String sql) {
		List<JSONObject> restL = new ArrayList<JSONObject>();
		JSONArray dataL = Neo4jConnect.executionCypher(sql);
		for (int i = 0; i < dataL.length(); i++) {
			try {
				JSONArray currL = (JSONArray) dataL.get(i);
				JSONObject currM = (JSONObject) currL.get(0);
				JSONObject restM = new JSONObject();
				restM.put("data", currM.get("data"));
				JSONObject js = (JSONObject)currM.get("metadata");
				Iterator it = js.keys();
				while(it.hasNext()){
					String key = it.next().toString();
					restM.put(key,js.get(key));
				}
				restL.add(restM);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return restL;
	}

	/**
	 * 通过cypher获取对象数据 返回结果是多个 (return n,r)
	 * 
	 * @param sql
	 * @return
	 */
	public static List<List<JSONObject>> getDataMulMap(String sql) {
		List<List<JSONObject>> restL = new ArrayList<List<JSONObject>>();
		JSONArray dataL = Neo4jConnect.executionCypher(sql);
		for (int i = 0; i < dataL.length(); i++) {
			try {
				JSONArray currL = (JSONArray)dataL.get(i);
				List<JSONObject> mL = new ArrayList<JSONObject>();
				for (int j = 0; j < currL.length(); j++) {
					JSONObject currM =  (JSONObject) currL.get(j);
					JSONObject restM = new JSONObject();
					restM.put("data", currM.get("data"));
					JSONObject js = (JSONObject)currM.get("metadata");
					Iterator it = js.keys();
					while(it.hasNext()){
						String key = it.next().toString();
						restM.put(key,js.get(key));
					}
					mL.add(restM);
				}
				restL.add(mL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return restL;
	}
	
//	public static List<List> getDataMulMap1(String sql) {
//		List<List> restL = new ArrayList<List>();
//		List<List> dataL = Neo4jConnect.executionCypher(sql);
//		for (int i = 0; i < dataL.size(); i++) {
//			List currL = (List) dataL.get(i);
//			List<Map> mL = new ArrayList<Map>();
//			for (int j = 0; j < currL.size(); j++) {
//				Map currM = (Map) currL.get(j);
//				Map restM = new HashMap();
//				restM.put("data", (Map) currM.get("data"));
//				restM.putAll((Map) currM.get("metadata"));
//				mL.add(restM);
//			}
//			restL.add(mL);
//		}
//		return restL;
//	}

	/**
	 * 带参数形势 ， 通过cypher获取对象数据 返回结果是多个 (return n,r)
	 * 
	 * @param sql
	 * @return
	 */
	public static List<List<JSONObject>> getDataMulMap(String sql,String param) {
		List<List<JSONObject>> restL = new ArrayList<List<JSONObject>>();
		JSONArray dataL = Neo4jConnect.executionCypher(sql,param);
		for (int i = 0; i < dataL.length(); i++) {
			try {
				JSONArray currL = (JSONArray)dataL.get(i);
				List<JSONObject> mL = new ArrayList<JSONObject>();
				for (int j = 0; j < currL.length(); j++) {
					JSONObject currM =  (JSONObject) currL.get(j);
					JSONObject restM = new JSONObject();
					restM.put("data", currM.get("data"));
					JSONObject js = (JSONObject)currM.get("metadata");
					Iterator it = js.keys();
					while(it.hasNext()){
						String key = it.next().toString();
						restM.put(key,js.get(key));
					}
					mL.add(restM);
				}
				restL.add(mL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return restL;
	}

	/**
	 * 带参数形势 ，通过cypher获取对象数据
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	public static List<JSONObject> getDataMap(String sql, String param) {
		List<JSONObject> restL = new ArrayList<JSONObject>();
		JSONArray dataL = Neo4jConnect.executionCypher(sql, param);
		for (int i = 0; i < dataL.length(); i++) {
			try {
				JSONArray currL = (JSONArray) dataL.get(i);
				JSONObject currM = (JSONObject) currL.get(0);
				JSONObject restM = new JSONObject();
				restM.put("data", currM.get("data"));
				JSONObject js = (JSONObject)currM.get("metadata");
				Iterator it = js.keys();
				while(it.hasNext()){
					String key = it.next().toString();
					restM.put(key,js.get(key));
				}
				restL.add(restM);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return restL;
	}

	/**
	 * 带参数形势 ，通过cypher获取指定id对象数据
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	public static JSONObject getDataOneMap(String sql, String param) {
		JSONObject restM = new JSONObject();
		JSONArray dataL = Neo4jConnect.executionCypher(sql, param);
		for (int i = 0; i < dataL.length(); i++) {
			JSONArray currL;
			try {
				currL = (JSONArray) dataL.get(i);
				JSONObject currM = (JSONObject) currL.get(0);
				restM.put("data", currM.get("data"));
				JSONObject js = (JSONObject)currM.get("metadata");
				Iterator it = js.keys();
				while(it.hasNext()){
					String key = it.next().toString();
					restM.put(key,js.get(key));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return restM;
	}

	/**
	 * 通过cypher获取指定id对象数据
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	public static JSONObject getDataOneMap(String sql) {
		JSONObject restM = new JSONObject();
		JSONArray dataL = Neo4jConnect.executionCypher(sql);
		for (int i = 0; i < dataL.length(); i++) {
			JSONArray currL;
			try {
				currL = (JSONArray) dataL.get(i);
				JSONObject currM = (JSONObject) currL.get(0);
				restM.put("data", currM.get("data"));
				JSONObject js = (JSONObject)currM.get("metadata");
				Iterator it = js.keys();
				while(it.hasNext()){
					String key = it.next().toString();
					restM.put(key,js.get(key));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return restM;
	}

	/**
	 * 通过cypher获取属性数组或者数字数组
	 * 
	 * @param sql
	 * @return
	 */
	public static JSONArray getDataList(String sql) {
		JSONArray dataL = Neo4jConnect.executionCypher(sql);
		return dataL;
	}

	/**
	 * 带参数形势 ，通过cypher获取属性数组或者数字数组
	 * 
	 * @param sql
	 * @param param
	 * @return
	 */
	public static JSONArray getDataList(String sql, String param) {
		JSONArray dataL = new JSONArray();
		if (sql != null && !"".equals(sql)) {
			dataL = Neo4jConnect.executionCypher(sql, param);
		}
		return dataL;
	}

	/**
	 * 通过cypher获取数字
	 * 
	 * @param sql
	 * @return
	 */
	public static long getDataLong(String sql) {
		long res = 0;
		if (sql != null && !"".equals(sql)) {
			try {
				JSONArray dataL = Neo4jConnect.executionCypher(sql);
				if (dataL.length() != 0) {
					JSONArray currL = (JSONArray) dataL.get(0);
					res = Long.parseLong(currL.get(0).toString());
				} else {
					res = 0;
				}
			} catch (Exception e) {
			}
		}
		return res;
	}

	/**
	 * 执行无需返回值的cypher
	 * 
	 * @param sql
	 * @return
	 */
	public static void executeNoRest(String sql) {
		if (sql != null && !"".equals(sql)) {
			Neo4jConnect.executionCypher(sql);
		}

	}

	/**
	 * 带参数形势 ，执行无需返回值的cypher
	 * 
	 * @param sql
	 * @return
	 */
	public static void executeNoRest(String sql, String param) {
		if (sql != null && !"".equals(sql)) {
			Neo4jConnect.executionCypher(sql, param);
		}

	}

	/**
	 * 
	 * return id(m);则是 [[12],[13]]
	 * 
	 * @param sql
	 *            "match (n)-[r]-(m) return n,r,m"
	 * @return {data:[[{data:{n为节点时这是n的
	 *         属性s},metadata:{id:"1",labels:["Ci",""]},{
	 *         data:{假设r是关系,这是关系的属性},metadata:{id:"",type:"关系的名字"}},{m}],
	 *         [{},{},{}],[{},{},{}]],column:["n","r","m"]}
	 * @throws Exception
	 */
	public static JSONObject base(String sql) throws Exception {
		JSONObject executionCypherBase = Neo4jConnect.executionCypherBase(sql);
		return executionCypherBase;
	}
}
