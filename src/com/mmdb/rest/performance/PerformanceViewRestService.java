package com.mmdb.rest.performance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.ChartBean;
import com.mmdb.model.bean.ChartData;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.model.bean.PerformanceViewBean;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.service.performance.IPerformanceViewService;
import com.mmdb.service.performance.impl.PerformanceService;
import com.mmdb.service.performance.impl.PerformanceViewService;

public class PerformanceViewRestService extends BaseRest{
	
	private IPerformanceViewService service;

	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private IPerformanceService dataService;
	
	@Override
	public void ioc(ApplicationContext context) {
		service =  new PerformanceViewService();
		dataService = new PerformanceService();
	}
	
	@Override
	public Representation postHandler(Representation entity)  throws Exception{
		JSONObject params = parseEntity(entity);
		String desc = params.containsKey("desc") == false ? "" : params.getString("desc");
		String viewName = params.getString("viewName");
		JSONArray arr = params.getJSONArray("kpis");
		String message = "操作成功!";
		String user = getUsername();
		
		PerformanceViewBean bean = new PerformanceViewBean();
		bean.setUserName(user);
		bean.setDesc(desc);
		bean.setKpis(arr.toString());
		bean.setViewName(viewName);
		bean.setCreateTime(sf.format(new Date(System.currentTimeMillis())));
		
		if(service.getViewByUserAndName(user, viewName) != null){
			throw new MException("视图已经存在!");
		}else{
			boolean flag = service.savePerformanceView(bean);
			if(!flag){
				throw new MException("保存视图失败!");
			}
		}
		
		JSONObject ret = new JSONObject();
		ret.put("message", message);
		return new JsonRepresentation(ret.toString());
	}
	
	@Override
	public Representation putHandler(Representation entity) throws Exception{
		JSONObject params = parseEntity(entity);
		String desc = params.containsKey("desc") == false ? "" : params.getString("desc");
		String viewName = params.getString("viewName");
		JSONArray arr = params.getJSONArray("kpis");
		String message = "操作成功!";
		String user = getUsername();
		
		PerformanceViewBean bean = new PerformanceViewBean();
		bean.setUserName(user);
		bean.setDesc(desc);
		bean.setKpis(arr.toString());
		bean.setViewName(viewName);
		boolean flag = service.updatePerformanceView(bean);
		if(!flag){
			throw new MException("操作失败!");
		}
		
		JSONObject ret = new JSONObject();
		ret.put("message", message);
		return new JsonRepresentation(ret.toString());
	}
	
	
	@Override
	public Representation delHandler(Representation entity) throws Exception{
		String viewName = getValue("viewName");
		String message = "操作成功!";
		String user = getUsername(); 
		
		boolean flag = service.deletePerformanceView(user, viewName);
		if(!flag){
			throw new MException("操作失败!");
		}
		JSONObject ret = new JSONObject();
		ret.put("message", message);
		return new JsonRepresentation(ret.toString());
	}
	
	
	@Override
	public Representation getHandler() throws Exception{
		String message = "操作成功!";
		int start = 0;
		int limit = 300;
		Map<Integer,List<PerformanceViewBean>> map = null;
		String user = getUsername();
		String viewName = getValue("viewName");
		JSONObject ret = new JSONObject();
		ret.put("message", message);
		if(viewName != null && !viewName.equals("null")){
			PerformanceViewBean bean = service.getViewByUserAndName(user, viewName);
			String kpis = bean.getKpis();
			JSONArray arr = JSONArray.fromObject(kpis);
			List<ChartData> datas = new ArrayList<ChartData>();
			Calendar endTime = Calendar.getInstance();
			Calendar startTime = (Calendar) endTime.clone();
			startTime.add(Calendar.DATE, -1);
			//System.out.println(startTime.getTime().toLocaleString());
			//System.out.println(endTime.getTime().toLocaleString());
			for(int i = 0 ; i < arr.size(); i++){
				JSONObject json = arr.getJSONObject(i);
				String ciCate = json.getString("ciCate");
				String ci = json.getString("ci");
				String kpiCate = json.getString("kpiCate");
				String kpi = json.getString("kpi");
				Integer index = json.getInt("orderIndex");
				Map<Integer,List<PerformanceBean>> dataMap = dataService.getAllPerformanceDatasByInstance(ciCate, ci, kpiCate, kpi, "",sf.format(startTime.getTime()),sf.format(endTime.getTime()), start, 100000);
			    Integer total = dataMap.keySet().iterator().next();
			    List<PerformanceBean> list = dataMap.get(total);
			   // System.out.println(total);
			    Map<String,List<ChartBean>> cmap = new HashMap<String,List<ChartBean>>();
			    for(PerformanceBean b : list){
			    	ci = b.getCiName();
			    	kpi = b.getKpiName();
		    		//ChartBean chartBean = new ChartBean(b.getStartTime().substring(11,16),b.getValue());
			    	ChartBean chartBean = new ChartBean(b.getStartTime(),b.getValue());
		    		String inst = b.getInstance();
		    		if(inst == null || inst.equals("null") || inst.length() == 0){
		    			if(cmap.get(b.getKpiName()) == null){
			    			List<ChartBean> chartBeans = new ArrayList<ChartBean>();
			    			chartBeans.add(chartBean);
			    			cmap.put(b.getKpiName(), chartBeans);
			    		}else{
			    			cmap.get(b.getKpiName()).add(chartBean);
			    		}
		    		}else{
			    		if(cmap.get(inst) == null){
			    			List<ChartBean> chartBeans = new ArrayList<ChartBean>();
			    			chartBeans.add(chartBean);
			    			cmap.put(inst, chartBeans);
			    		}else{
			    			cmap.get(inst).add(chartBean);
			    		}
		    		}
			    }
			    ChartData data = new ChartData();
			    data.setTitle(ci);
			    data.setSubTitle(kpi);
			    data.setIndex(index);
			    JSONArray array = new JSONArray();
			    Iterator<String> it = cmap.keySet().iterator();
			    while(it.hasNext()){
			    	String inst = it.next();
			    	List<ChartBean> bs = cmap.get(inst);
			    	JSONObject jsonv = new JSONObject();
			    	jsonv.put("inst", inst);
			    	jsonv.put("value", JSONArray.fromObject(bs).toString());
			    	array.add(jsonv);
			    }
			    data.setValues(array.toString());
			    datas.add(data);
			}
			Map<String,List<ChartData>> res = new HashMap<String,List<ChartData>>();
			res.put("datas", datas);
			return new JsonRepresentation(JSONObject.fromObject(res));
		}else{
			 map = service.getViewByUser(user, start, limit);
			 Iterator<Integer> it = map.keySet().iterator();
			 Integer totalCount = it.next();
			 List<PerformanceViewBean> list = map.get(totalCount);
			 Page page = new Page(list);
			 page.setStart(start);
			 page.setPageSize(limit);
			 page.setTotalCount(totalCount);
			 return new JsonRepresentation(JSONObject.fromObject(page));
		}
	}
}
