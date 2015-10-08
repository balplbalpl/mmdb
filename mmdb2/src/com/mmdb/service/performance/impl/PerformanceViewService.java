/**
 * 
 */
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

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.PerformanceViewBean;
import com.mmdb.model.role.IRoleDao;
import com.mmdb.model.role.storage.RoleDao;
import com.mmdb.service.performance.IPerformanceViewService;

/**
 * @author guojun
 *
 */
public class PerformanceViewService implements IPerformanceViewService {
	
	private IRoleDao dao = new RoleDao();
	
	private SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Log log = LogFactory.getLogger("PerformanceViewService");
	
	@Override
	public boolean savePerformanceView(PerformanceViewBean view) {
		long createTime = 0l;
		try {
			createTime = sf.parse(view.getCreateTime()).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String sql = "insert into tb_performance_view(`viewname`,`username`,`kpis`,`desc`,`createtime`) values("
				+"'"+view.getViewName()+"',"
				+"'"+view.getUserName()+"',"
				+"'"+view.getKpis()+"',"
				+"'"+view.getDesc()+"',"
				+createTime
				+ ")";
		return dao.saveObject(sql);
	}

	@Override
	public boolean updatePerformanceView(PerformanceViewBean view) {
		String sql = "update tb_performance_view set `kpis`='"+view.getKpis()+"',`desc`='"+view.getDesc()+"' where `viewname`="
				   +"'"+view.getViewName()+"' and `username`='"+view.getUserName()+"'";
		return dao.updateObject(sql);
	}

	@Override
	public Map<Integer,List<PerformanceViewBean>> getViewByUser(String user, int start,
			int limit) {
		Map<Integer,List<PerformanceViewBean>> map = new HashMap<Integer,List<PerformanceViewBean>>();
		String sql = "select * from tb_performance_view where `username`='"+user
				+"' order by createtime,viewname limit "+start+","+limit;
		PerformanceViewBean bean = null;
		ResultSet rs = dao.getObjectById(sql);
		Integer count = dao.getCount(sql);
		List<PerformanceViewBean> list = new ArrayList<PerformanceViewBean>();
		try {
			while(rs.next()){
				bean = new PerformanceViewBean();
				String kpis = rs.getString("kpis");
				String viewName = rs.getString("viewname");
				String desc = rs.getString("desc");
				Long time = rs.getLong("createtime");
				String createTime = sf.format(new Date(time));
				bean.setKpis(kpis);
				bean.setUserName(user);
				bean.setViewName(viewName);
				bean.setCreateTime(createTime);
				bean.setDesc(desc);
				list.add(bean);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		map.put(count, list);
		return map;
	}

	@Override
	public boolean deletePerformanceView(String user, String viewName) {
		String sql = "delete tb_performance_view  where `viewname`="
				   +"'"+viewName+"' and `username`='"+user+"'";
		return dao.deleteObject(sql);
	}

	@Override
	public PerformanceViewBean getViewByUserAndName(String user, String viewName) {
		String sql = "select * from tb_performance_view where `username`='"+user
				+"' and `viewname`='"+viewName+"'";
		PerformanceViewBean bean = null;
		ResultSet rs = dao.getObjectById(sql);
		try {
			while(rs.next()){
				bean = new PerformanceViewBean();
				String kpis = rs.getString("kpis");
				String desc = rs.getString("desc");
				Long time = rs.getLong("createtime");
				String createTime = sf.format(new Date(time));
				bean.setKpis(kpis);
				bean.setUserName(user);
				bean.setViewName(viewName);
				bean.setCreateTime(createTime);
				bean.setDesc(desc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			log.dLog(e.getMessage());
		}
		return bean;
	}

}
