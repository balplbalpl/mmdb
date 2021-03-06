package com.mmdb.service.performance;

import java.util.List;
import java.util.Map;

import com.mmdb.model.bean.*;

public interface IPerformanceService {
	
	public Map<Integer,List<PerformanceBean>> getAllPerformanceDatas(int start,int limit);
	
	public Map<Integer,List<PerformanceBean>> getAllPerformanceDatasByInstance(String cis,String kpis,int start,int limit);
	
	public List<PerformanceBean> getTreeData();
	
	boolean deleteAll();
	
	public long getMaxTime();

	Map<Integer, List<PerformanceBean>> getAllPerformanceDatasByInstance(
			String qciCate, String qci, String qkpiCate, String qkpi,String instanceName,
			String startTime, String endTime, int start, int limit);
	
	public Page<Map<String,Object>> getPerformaceDatasByView(
			List<String[]> ciKpis, long time, int page, int pageSize);
	
	public Map<Integer, List<PerformanceBean>> getPerformaceDatas(
			String qciCate, String qci, String qkpiCate, String qkpi,String instanceName,
			String startTime, String endTime, int start, int limit, String username, boolean withUser);
}
