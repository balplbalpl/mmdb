package com.mmdb.service.performance.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.mmdb.model.bean.Page;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.storage.KpiInfoStorage;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.model.role.storage.RoleDao;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.util.HexString;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MapReduceOutput;

public class CopyOfPerformanceService implements IPerformanceService {

	private IRoleDao roleDao = new RoleDao();
	@Autowired
	private KpiInfoStorage kpiInfoStorage;
	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	public Map<Integer, List<PerformanceBean>> getAllPerformanceDatas(
			int start, int limit) {
		// TODO Auto-generated method stub
		Map<Integer, List<PerformanceBean>> map = new HashMap<Integer, List<PerformanceBean>>();
		List<PerformanceBean> list = new ArrayList<PerformanceBean>();
		String sql = "select * from Performance order by time desc limit "
				+ start + "," + limit;
		Integer count = roleDao.getCount(sql);
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				String ciHex = rs.getString("ciHex");
				String kpiHex = rs.getString("kpiHex");
				String ciId = rs.getString("ciId");
				String kpiId = rs.getString("kpiId");
				// 分类，子类;
				// kpi CPU,KPI
				ciHex = HexString.decode(ciHex);
				kpiHex = HexString.decode(kpiHex);
				JSONArray cis = JSONArray.fromObject(ciHex);
				JSONArray kpis = JSONArray.fromObject(kpiHex);
				String ciCate = cis.getString(0);
				String ciName = cis.getString(1);
				String kpiCate = kpis.getString(0);
				String kpiName = kpis.getString(1);
				String val = rs.getString("val");
				String instance = rs.getString("instance");
				Long stime = rs.getLong("time");
				String startTime = sf.format(new Date(stime));

				PerformanceBean bean = new PerformanceBean();
				bean.setCiCate(ciCate);
				bean.setCiName(ciName);
				bean.setKpiCate(kpiCate);
				bean.setKpiName(kpiName);
				bean.setStartTime(startTime);
				bean.setValue(val);
				bean.setInstance(instance);
				bean.setCiId(ciId);
				bean.setKpiId(kpiId);
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		map.put(count, list);
		return map;
	}

	@Override
	public Map<Integer, List<PerformanceBean>> getAllPerformanceDatasByInstance(
			String pcis, String pkpis, int start, int limit) {
		Map<Integer, List<PerformanceBean>> map = new HashMap<Integer, List<PerformanceBean>>();
		List<PerformanceBean> list = new ArrayList<PerformanceBean>();
		String sql = "select * from Performance where ciHex='" + pcis
				+ "' and kpiHex='" + pkpis + "' order by time desc limit "
				+ start + "," + limit;
		Integer count = roleDao.getCount(sql);
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				String ciHex = rs.getString("ciHex");
				String kpiHex = rs.getString("kpiHex");
				String ciId = rs.getString("ciId");
				String kpiId = rs.getString("kpiId");
				// 分类，子类;
				// kpi CPU,KPI
				ciHex = HexString.decode(ciHex);
				kpiHex = HexString.decode(kpiHex);
				JSONArray cis = JSONArray.fromObject(ciHex);
				JSONArray kpis = JSONArray.fromObject(kpiHex);
				String ciCate = cis.getString(0);
				String ciName = cis.getString(1);
				String kpiCate = kpis.getString(0);
				String kpiName = kpis.getString(1);
				String val = rs.getString("val");
				String instance = rs.getString("instance");
				Long stime = rs.getLong("time");
				String startTime = sf.format(new Date(stime));

				PerformanceBean bean = new PerformanceBean();
				bean.setCiCate(ciCate);
				bean.setCiName(ciName);
				bean.setKpiCate(kpiCate);
				bean.setKpiName(kpiName);
				bean.setStartTime(startTime);
				bean.setValue(val);
				bean.setInstance(instance);
				bean.setCiId(ciId);
				bean.setKpiId(kpiId);
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		map.put(count, list);
		return map;
	}

	@Override
	public List<PerformanceBean> getTreeData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, List<PerformanceBean>> getAllPerformanceDatasByInstance(
			String qciCate, String qci, String qkpiCate, String qkpi,
			String instanceName, String s_startTime, String s_endTime,
			int start, int limit) {
		Map<Integer, List<PerformanceBean>> map = new HashMap<Integer, List<PerformanceBean>>();
		List<PerformanceBean> list = new ArrayList<PerformanceBean>();
		String queryString = "where";
		if (qciCate.length() > 0) {
			queryString += " `ciCategoryId` = '" + qciCate + "' and";
		}
		if (qci.length() > 0) {
			queryString += " `ciId` = '" + qci + "' and";
		}
		if (qkpiCate.length() > 0) {
			queryString += " `kpiCategoryId` = '" + qkpiCate + "' and";
		}
		if (qkpi.length() > 0) {
			queryString += " `kpiId` = '" + qkpi + "' and";
		}
		if (instanceName.length() > 0) {
			queryString += " `instance` = '" + instanceName + "' and";
		}
		if (s_startTime.length() > 0) {
			long startTime = 0;
			long endTime = System.currentTimeMillis();
			try {
				if(s_startTime.length() > 0){
					startTime = sf.parse(s_startTime).getTime();
				}
				if(s_endTime.length() > 0){
					endTime = sf.parse(s_endTime).getTime();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
			queryString += " `time` >= " + startTime + " and `time` <="
					+ endTime + " and";
		}

		if (queryString.equals("where")) {
			queryString = "";
		} else {
			queryString = queryString.substring(0, queryString.length() - 3);
		}

		String sql = "select * from Performance " + queryString
				+ " order by ciHex,kpiHex,time desc limit " + start + ","
				+ limit;
		Integer count = roleDao.getCount(sql);
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				String ciHex = rs.getString("ciHex");
				String kpiHex = rs.getString("kpiHex");
				String ciId = rs.getString("ciId");
				String kpiId = rs.getString("kpiId");
				// 分类，子类;
				// kpi CPU,KPI
				ciHex = HexString.decode(ciHex);
				kpiHex = HexString.decode(kpiHex);
				JSONArray cis = JSONArray.fromObject(ciHex);
				JSONArray kpis = JSONArray.fromObject(kpiHex);
				String ciCate = cis.getString(0);
				String ciName = cis.getString(1);
				String kpiCate = kpis.getString(0);
				String kpiName = kpis.getString(1);
				String val = rs.getString("val");
				String instance = rs.getString("instance");
				Long stime = rs.getLong("time");
				String sTimes = sf.format(new Date(stime));

				PerformanceBean bean = new PerformanceBean();
				bean.setCiCate(ciCate);
				bean.setCiName(ciName);
				bean.setKpiCate(kpiCate);
				bean.setKpiName(kpiName);
				bean.setStartTime(sTimes);
				bean.setValue(val);
				bean.setInstance(instance);
				bean.setCiId(ciId);
				bean.setKpiId(kpiId);
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		map.put(count, list);
		return map;
	}

	@Override
	public boolean deleteAll() {
		// TODO Auto-generated method stub
		return roleDao.deleteObject("delete from Performance");
	}

	@Override
	public Page<Map<String, Object>> getPerformaceDatasByView(
			List<String[]> ciKpis, long time, int page, int pageSize) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		long sTime = (time - 30000000) * 1000;
		long eTime = (time + 30000000) * 1000;
//		long sTime = 11296619120L;
//		long eTime = 1459661912000L;
		if (ciKpis == null || ciKpis.size() == 0)
			return new Page<Map<String, Object>>();

		int size = ciKpis.size();
		final int pSize = 50;
		int index = size / pSize;
		if (size % pSize != 0) {
			index++;
		}

		Map<String, Integer> map = new HashMap<String, Integer>();
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < index; i++) {
			StringBuilder match = new StringBuilder(
					"select * from Performance where `time` >").append(sTime)
					.append(" and `time` < ").append(eTime).append(" and (");

			int end = i * pSize + pSize;
			if (end > size) {
				end = size;
			}

			for (int j = i * pSize; j < end; j++) {
				String[] ciKpi = ciKpis.get(j);
				String ci = ciKpi[0];
				String kpi = ciKpi[1];
				match.append("(`ciHex`='").append(ci)
						.append("' and `kpiHex` ='").append(kpi)
						.append("') or");
			}
			match.delete(match.length() - 2, match.length());
			match.append(") order by ciHex,kpiHex,time desc ");
			ResultSet rs = roleDao.getAll(match.toString());

			try {
				while (rs.next()) {
					String ciHex = rs.getString("ciHex");
					String mongoId = rs.getString("ciId");
					String kpiHex = rs.getString("kpiHex");
					Long stime = rs.getLong("time");

					String key = ciHex + "_^_" + kpiHex;

					Map<String, Object> data = null;
					Integer tIndex = map.get(key);
					if (tIndex != null) {
						data = datas.get(tIndex);
					}
					if (data != null) {
						long nTime = (Long) data.get("time");
						if (nTime < stime) {
							Map<String, Object> result = new HashMap<String, Object>();
							// result.put("ciHex", ciHex);
							result.put("ciId", ciHex);
							result.put("mongoId", mongoId);
							result.put("kpiHex", kpiHex);
							
							ciHex = HexString.decode(ciHex);
							kpiHex = HexString.decode(kpiHex);
							JSONArray cis = JSONArray.fromObject(ciHex);
							JSONArray kpis = JSONArray.fromObject(kpiHex);
							String ciCate = cis.getString(0);
							String ciName = cis.getString(1);
							String kpiCate = kpis.getString(0);
							String kpiName = kpis.getString(1);

							result.put("ciCate", ciCate);
							result.put("ciName", ciName);

							result.put("kpiCate", kpiCate);
							result.put("kpiName", kpiName);

							result.put("kpiId", rs.getString("kpiId"));
							result.put("val", rs.getString("val"));
							result.put("instance", rs.getString("instance"));
							result.put("time", stime);
							datas.add(result);
							map.put(key, datas.size() - 1);
						}
					} else {
						Map<String, Object> result = new HashMap<String, Object>();
						// result.put("ciHex", ciHex);
						result.put("ciId", ciHex);
						result.put("mongoId", mongoId);
						result.put("kpiHex", kpiHex);
						
						ciHex = HexString.decode(ciHex);
						kpiHex = HexString.decode(kpiHex);
						JSONArray cis = JSONArray.fromObject(ciHex);
						JSONArray kpis = JSONArray.fromObject(kpiHex);
						String ciCate = cis.getString(0);
						String ciName = cis.getString(1);
						String kpiCate = kpis.getString(0);
						String kpiName = kpis.getString(1);

						result.put("ciCate", ciCate);
						result.put("ciName", ciName);

						result.put("kpiCate", kpiCate);
						result.put("kpiName", kpiName);

						result.put("kpiId", rs.getString("kpiId"));
						result.put("val", rs.getString("val"));
						result.put("instance", rs.getString("instance"));
						result.put("time", stime);
						datas.add(result);
						map.put(key, datas.size() - 1);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		int count = datas.size();
		int start = (page - 1) * pageSize;
		start = start < 0 ? 0 : start;
		int end = page * pageSize;
		start = start > count ? count : start;
		end = end > count ? count : end;
		datas = datas.subList(start, end);

		Page<Map<String, Object>> ret = new Page<Map<String, Object>>();
		ret.setCount(datas.size());
		ret.setPageSize(pageSize);
		ret.setStart(page);
		ret.setTotalCount(count);
		ret.setDatas(datas);
		return ret;
	}

	@Override
	public long getMaxTime(){
		long sTimes = System.currentTimeMillis() -6*60*1000;
		String sql = "select * from Performance order by time desc limit 0,1";
		ResultSet rs = roleDao.getAll(sql);
		try {
			while (rs.next()) {
				sTimes = rs.getLong("time");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sTimes;
	}
	

	@Override
	public Map<Integer, List<PerformanceBean>> getPerformaceDatas(
			String qciCate, String qci, String qkpiCate, String qkpi,String instanceName,
			String stime, String etime, int start, int limit, String username, boolean withUser) {
		Map<Integer, List<PerformanceBean>> res = new HashMap<Integer, List<PerformanceBean>>();
		DB db = MongoConnect.getDb();
		DBCollection col = db.getCollection("Performance");
		String map = "function(){emit(this.ciName+'|'+this.kpiName+'|'+this.instance"
				+ ",{time:this.time"
				+ ",val:this.val"
				+ ",instance:this.instance"
				+",ciId:this.ciId"
				+",ciHex:this.ciHex"
				+",ciName:this.ciName"
				+",ciCategoryId:this.ciCategoryId"
				+",ciCategoryName:this.ciCategoryName"
				+",kpiId:this.kpiId"
				+",kpiName:this.kpiName"
				+",kpiCategoryId:this.kpiCategoryId"
				+",kpiCategoryName:this.kpiCategoryName"
				+",kpiHex:this.kpiHex"
				+ "});}";
		String reduce = "function(key,values){val=0;time=0;for(var i in values){if(values[i].time>time) {time=values[i].time;val=values[i].val;}} return {"
				+ "time:time"
				+ ",val:val"
				+ ",instance:values[i].instance"
				+",ciId:values[i].ciId"
				+",ciName:values[i].ciName"
				+",ciHex:values[i].ciHex"
				+",ciCategoryId:values[i].ciCategoryId"
				+",ciCategoryName:values[i].ciCategoryName"
				+",kpiId:values[i].kpiId"
				+",kpiName:values[i].kpiName"
				+",kpiCategoryId:values[i].kpiCategoryId"
				+",kpiCategoryName:values[i].kpiCategoryName"
				+",kpiHex:values[i].kpiHex"
				+ "};}";
	
		List<PerformanceBean> list = new ArrayList<PerformanceBean>();
		int total = 0;
		long startTime = getMaxTime() - 7 * 60 * 1000;
		long endTime = System.currentTimeMillis();
		try {
			if(stime.length() > 0){
				startTime = sf.parse(stime).getTime();
			}
			if(etime.length() > 0){
				endTime = sf.parse(etime).getTime();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try{
		/*	BasicDBObject query = new BasicDBObject("value.time",new BasicDBObject("$gte",startTime));
			query.append("value.time",new BasicDBObject("$lte",endTime));*/
			BasicDBObject query = new BasicDBObject();
			if (qciCate.length() > 0) {
				query.append("value.ciCategoryId",qciCate);
			}
			if (qci.length() > 0) {
				query.append("value.ciId",qci);
			}
			if (qkpiCate.length() > 0) {
				query.append("value.kpiCategoryId",qkpiCate);
			}
			if (qkpi.length() > 0) {
				query.append("value.kpiId",qkpi);
			}
			if (instanceName.length() > 0) {
				query.append("value.instance",instanceName);
			}
			if(withUser){
				List<KpiInformation> kpis = kpiInfoStorage.findBySql(" owner='"+username+"'");
				BasicDBList l = new BasicDBList();
				for(KpiInformation kpi:kpis){
					BasicDBList ll = new BasicDBList();
					ll.add(new BasicDBObject("kpiId", kpi.getId()));
					BasicDBObject o = new BasicDBObject("$or", ll);
					l.add(o);
				}
				if(l.size()>0){
					query.append("$and", l);
				}else{
					query.append("value.kpiId", "aaa");
				}
			}
						
			//col.group(key, cond, initial, reduce)
			BasicDBObject orderBy = new BasicDBObject("value.ciCategoryName",1);
			orderBy.append("value.ciName", 1);
			DBCursor rs = null;
			String tableName = "performance_group";
			total = (int) db.getCollection(tableName).count();
			if(total > 0){
				//total = (int) db.getCollection(tableName).count();
				total = (int) db.getCollection(tableName).find(query).count();
				rs = db.getCollection(tableName).find(query).sort(orderBy).limit(limit).skip(start);
			}else{
				BasicDBObject query1 = new BasicDBObject("time",new BasicDBObject("$gte",startTime));
				query1.append("time",new BasicDBObject("$lte",endTime));
				MapReduceOutput out = col.mapReduce(map,  
		                reduce, tableName, query1);  
				total = out.getOutputCount();
				Thread.sleep(200);
				rs = out.getOutputCollection().find(query).sort(orderBy).limit(limit).skip(start);
			}
			
			while(rs!=null&&rs.hasNext()){
				JSONObject json = JSONObject.fromObject(rs.next().get("value"));
				String ciHex = json.getString("ciHex");
				String kpiHex = json.getString("kpiHex");
				String ciId = json.getString("ciId");
				String kpiId = json.getString("kpiId");
				// 分类，子类;
				// kpi CPU,KPI
				ciHex = HexString.decode(ciHex);
				kpiHex = HexString.decode(kpiHex);
				JSONArray cis = JSONArray.fromObject(ciHex);
				JSONArray kpis = JSONArray.fromObject(kpiHex);
				String ciCate = cis.getString(0);
				String ciName = cis.getString(1);
				String kpiCate = kpis.getString(0);
				String kpiName = kpis.getString(1);
				String val = json.getString("val");
				String instance = json.getString("instance");
				Long sTime = json.getLong("time");
				String sTimes = sf.format(new Date(sTime));
	
				PerformanceBean bean = new PerformanceBean();
				bean.setCiCate(ciCate);
				bean.setCiName(ciName);
				bean.setKpiCate(kpiCate);
				bean.setKpiName(kpiName);
				bean.setStartTime(sTimes);
				bean.setValue(val);
				bean.setInstance(instance);
				bean.setCiId(ciId);
				bean.setKpiId(kpiId);
				list.add(bean);
			}
			res.put(total, list);
			//out.drop();
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}
	
}
