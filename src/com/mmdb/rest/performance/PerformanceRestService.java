package com.mmdb.rest.performance;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

import com.mmdb.model.bean.ChartBean;
import com.mmdb.model.bean.ChartData;
import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.service.performance.impl.PerformanceService;

public class PerformanceRestService extends BaseRest{
	private IPerformanceService service;
	
	@Override
	public void ioc(ApplicationContext context) {
		service =  new PerformanceService();
	}
	
	@Override
	public Representation delHandler(Representation entity) throws Exception{
		service.deleteAll();
		JSONObject ret = new JSONObject();
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}
					
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Representation postHandler(Representation entity)  throws Exception{
		String chart = getValue("chart");
		int start = 0;
		int limit = 300;
		String ciId = "";
		String ciCategory  = "";
		String startTime = "";
		String endTime = "";
		String kpiCategory = "";
		String kpi = "";
		String kpiInstance = "";
		JSONObject params = parseEntity(entity);
		start = params.containsKey("start") == false ? start : Integer.parseInt(params.getString("start"));
		limit = params.containsKey("limit") == false ? limit : Integer.parseInt(params.getString("limit"));
		ciId = params.containsKey("ci") == false ? ciId : params.getString("ci");
		ciCategory = params.containsKey("ciCate") == false ? ciCategory : params.getString("ciCate");
		kpi = params.containsKey("kpi") == false ? kpi : params.getString("kpi");
		kpiCategory = params.containsKey("kpiCate") == false ? kpiCategory : params.getString("kpiCate");
		kpiInstance = params.containsKey("kpiInstance") == false ? kpiInstance : params.getString("kpiInstance");
		startTime = (params.containsKey("startTime") == false &&  params.getString("startTime").length() >0 && params.getString("startTime").equals("null")) ? startTime : params.getString("startTime");
		endTime = (params.containsKey("endTime") == false &&  params.getString("endTime").length() >0 && params.getString("endTime").equals("null")) ? endTime : params.getString("endTime");
		
	    if("chart".equals(chart)){
	    	Map<Integer,List<PerformanceBean>> map = service.getAllPerformanceDatasByInstance(ciCategory, ciId, kpiCategory, kpi, kpiInstance,startTime, endTime, start, limit);
		    Integer total = map.keySet().iterator().next();
		    List<PerformanceBean> list = map.get(total);
	    	  String ciName ="";
	    	  String kpiName = "";
	    	  limit = 100000;
	    	   Map<String,List<ChartBean>> cmap = new HashMap<String,List<ChartBean>>();
			    for(PerformanceBean b : list){
			    	ciName = b.getCiName();
			    	kpiName = b.getKpiName();
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
			    data.setTitle(ciName);
			    data.setSubTitle(kpiName);
			    data.setIndex(0);
			    JSONArray array = new JSONArray();
			    Iterator<String> it = cmap.keySet().iterator();
			    while(it.hasNext()){
			    	String inst = it.next();
			    	List<ChartBean> bs = cmap.get(inst);
			    	JSONObject jsonv = new JSONObject();
			    	jsonv.put("inst", inst);
/*			    	jsonv.put("color", "#000"); //控制线的颜色
			    	jsonv.put("hideInLegend", false); //控制图表名称是否隐藏
			    	jsonv.put("hideVisible", false); //控制图表是否隐藏
*/			    	jsonv.put("value", JSONArray.fromObject(bs).toString());
			    	array.add(jsonv);
			    }
			    data.setValues(array.toString());
			    Map<String,ChartData> res = new HashMap<String,ChartData>();
				res.put("datas", data);
				return new JsonRepresentation(JSONObject.fromObject(res));  	
	    }else if("getmyperf".equals(chart)){
	    	String username = getUsername();
	    	Map<Integer,List<PerformanceBean>> map = service.getPerformaceDatas(ciCategory, ciId, kpiCategory, kpi, kpiInstance,startTime, endTime, start, limit, username, true);
//	    	map = service.getPerformaceDatas(ciCategory, ciId, kpiCategory, kpi, kpiInstance,startTime, endTime, start, limit, username, true);
	    	Integer total = map.keySet().iterator().next();
		    List<PerformanceBean> list = map.get(total);
	    	Page page = new Page(list);
			page.setStart(start);
			page.setPageSize(limit);
			page.setTotalCount(total);
			return new JsonRepresentation(JSONObject.fromObject(page));
	    }else{
	    	String username = getUsername();
	    	Map<Integer,List<PerformanceBean>> map = service.getPerformaceDatas(ciCategory, ciId, kpiCategory, kpi, kpiInstance,startTime, endTime, start, limit, username, false);
//	    	map = service.getPerformaceDatas(ciCategory, ciId, kpiCategory, kpi, kpiInstance,startTime, endTime, start, limit, username, false);
	    	Integer total = map.keySet().iterator().next();
		    List<PerformanceBean> list = map.get(total);
	    	Page page = new Page(list);
			page.setStart(start);
			page.setPageSize(limit);
			page.setTotalCount(total);
			return new JsonRepresentation(JSONObject.fromObject(page));
	    }
	}	
	
	@Override
	public Representation getHandler() throws Exception{
		return null;
	}
	
	@Override
	public Representation putHandler(Representation entity) throws Exception{
		return null;
	}
}
