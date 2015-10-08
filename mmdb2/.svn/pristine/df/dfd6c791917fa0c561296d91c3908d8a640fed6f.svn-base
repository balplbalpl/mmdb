package com.mmdb.model.event;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mmdb.core.framework.neo4j.annotation.Space;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.KpiCategory;

@Space("eventView")
public class EventView {
	private String id;
	private String name;
	private List<Map<String, String>> titleMap;
	private List<String> severities;
	private List<String> ciConf;
	private List<Map<String, Object>> kpiConf;
	private Long lastTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Map<String, String>> getTitleMap() {
		return titleMap;
	}
	public void setTitleMap(List<Map<String, String>> titleMap) {
		this.titleMap = titleMap;
	}
	public List<String> getSeverities() {
		return severities;
	}
	public void setSeverities(List<String> severities) {
		this.severities = severities;
	}
	public Long getLastTime() {
		return lastTime;
	}
	public void setLastTime(Long lastTime) {
		this.lastTime = lastTime;
	}
	public List<String> getCiConf() {
		return ciConf;
	}
	public void setCiConf(List<String> ciConf) {
		this.ciConf = ciConf;
	}
	public List<Map<String, Object>> getKpiConf() {
		return kpiConf;
	}
	public void setKpiConf(List<Map<String, Object>> kpiConf) {
		this.kpiConf = kpiConf;
	}
	
	public JSONObject asMap(Map<String, CiCategory> ciCateMap, Map<String, KpiCategory> kpiCateMap, Map<String, String> severityMap) throws Exception{
		JSONObject ret = new JSONObject();
		ret.put("id", id);
		ret.put("name", name==null ? "":name);
		JSONArray titles = new JSONArray();
		try{
			titles = JSONArray.fromObject(titleMap);
		}catch(Exception e){
		}
		ret.put("titleMap", titles);
		JSONArray severityJs = new JSONArray();
		try{
			for(String severity:severities){
				JSONObject sJs = new JSONObject();
				sJs.put("id", severity);
				sJs.put("name", severityMap.get(severity));
				severityJs.add(sJs);
			}
		}catch(Exception e){
		}
		ret.put("severities", severityJs);
		JSONArray ciCateJs = new JSONArray();
		try{
			for(String cate:ciConf){
				JSONObject cateJs = new JSONObject();
				cateJs.put("id", cate);
				cateJs.put("name", ciCateMap.get(cate).getName());
				ciCateJs.add(cateJs);
			}
		}catch(Exception e){
		}
		ret.put("ciCates", ciCateJs);
		JSONArray kpiCateJs = new JSONArray();
		try{
			for(Map<String, Object> cate:kpiConf){
				JSONObject cateJs = new JSONObject();
				cateJs.put("id", cate.get("id"));
				cateJs.put("name", kpiCateMap.get(cate.get("id")).getName());
				cateJs.put("kpis", JSONArray.fromObject(cate.get("kpis")));
				kpiCateJs.add(cateJs);
			}
		}catch(Exception e){
		}
		ret.put("kpiCates", kpiCateJs);
		ret.put("lastTime", lastTime==null ? -1L:lastTime);
		return ret;
	}
}
