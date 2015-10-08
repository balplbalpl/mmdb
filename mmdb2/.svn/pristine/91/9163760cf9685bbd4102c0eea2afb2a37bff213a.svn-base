package com.mmdb.model.event.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.event.EventView;
import com.mmdb.mongo.MongoConnect;

@Component("eventViewStorage")
public class EventViewStorage {
	private Log log = LogFactory.getLogger("EventViewStorage");

	public List<EventView> getAll(User user) throws Exception {
		String sql = "select * from EventView where loginName='"+user.getLoginName()+"'";
		return query(sql);
	}
	
	public EventView getById(String id, User user) throws Exception {
		List<EventView> ret = getByProperty("_id", id, user);
		if(ret.size()==1){
			return ret.get(0);
		}else if(ret.size()>1){
			throw new Exception("事件视图["+id+"]不唯一");
		}else{
			return null;
		}
	}
	
	public EventView getByName(String name, User user) throws Exception {
		List<EventView> ret = getByProperty("name", name, user);
		if(ret.size()==1){
			return ret.get(0);
		}else if(ret.size()>1){
			throw new Exception("事件视图["+name+","+user.getLoginName()+"]不唯一");
		}else{
			return null;
		}
	}
	
	private List<EventView> getByProperty(String key, String val, User user) throws Exception {
		String sql = "select * from EventView where `"+key+"`='"+val+"' and loginName='"+user.getLoginName()+"'";
		return query(sql);
	}
	
	private List<EventView> query(String sql) throws Exception {
		List<EventView> ret = new ArrayList<EventView>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				EventView view = new EventView();
				view.setId(rs.getString("_id"));
				view.setName(rs.getString("name"));
				JSONArray titleMap = null;
				try{
					titleMap = JSONArray.fromObject(rs.getString("titleMap"));
				}catch(Exception e){
					titleMap = new JSONArray();
				}
				view.setTitleMap(titleMap);
				JSONArray severities = null;
				try{
					severities = JSONArray.fromObject(rs.getString("severities"));
				}catch(Exception e){
					severities = new JSONArray();
				}
				view.setSeverities(severities);
				JSONArray ciCateIds = null;
				try{
					ciCateIds = JSONArray.fromObject(rs.getString("ciConf"));
				}catch(Exception e){
					ciCateIds = new JSONArray();
				}
				view.setCiConf(ciCateIds);
				JSONArray kpiCate = null;
				try{
					kpiCate = JSONArray.fromObject(rs.getString("kpiConf"));
				}catch(Exception e){
					kpiCate = new JSONArray();
				}
				view.setKpiConf(kpiCate);
				view.setLastTime(rs.getLong("lastTime"));
				ret.add(view);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					log.eLog(e.getMessage());
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					log.eLog(e.getMessage());
				}
			}
		}
		return ret;
	}
	
	public EventView save(EventView view, User user) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("insert into EventView(name,titleMap,severities,ciConf,kpiConf,lastTime,loginName) values(?,?,?,?,?,?,?)");
			pstmt.setString(1, view.getName()==null ? "":view.getName());
			pstmt.setString(2, view.getTitleMap()==null ? "[]":JSONArray.fromObject(view.getTitleMap()).toString());
			pstmt.setString(3, view.getSeverities()==null ? "[]":JSONArray.fromObject(view.getSeverities()).toString());
			pstmt.setString(4, view.getCiConf()==null ? "[]":JSONArray.fromObject(view.getCiConf()).toString());
			pstmt.setString(5, view.getKpiConf()==null ? "[]":JSONArray.fromObject(view.getKpiConf()).toString());
			pstmt.setLong(6, view.getLastTime()==null ? -1L:view.getLastTime());
			pstmt.setString(7, user.getLoginName());
			pstmt.executeUpdate();
			List<EventView> evs = getByProperty("name", view.getName(), user);
			if(evs.size()==1){
				return evs.get(0);
			}else{
				delete(evs);
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			log.eLog(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public EventView update(EventView view, User user) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("update EventView set name=?,titleMap=?,severities=?,ciConf=?,kpiConf=?,lastTime=?,loginName=? where _id=?");
			pstmt.setString(1, view.getName()==null ? "":view.getName());
			pstmt.setString(2, view.getTitleMap()==null ? "[]":JSONArray.fromObject(view.getTitleMap()).toString());
			pstmt.setString(3, view.getSeverities()==null ? "[]":JSONArray.fromObject(view.getSeverities()).toString());
			pstmt.setString(4, view.getCiConf()==null ? "[]":JSONArray.fromObject(view.getCiConf()).toString());
			pstmt.setString(5, view.getKpiConf()==null ? "[]":JSONArray.fromObject(view.getKpiConf()).toString());
			pstmt.setLong(6, view.getLastTime()==null ? -1L:view.getLastTime());
			pstmt.setString(7, user.getLoginName());
			pstmt.setString(8, view.getId());
			pstmt.executeUpdate();
			List<EventView> evs = getByProperty("name", view.getName(), user);
			if(evs.size()==1){
				return evs.get(0);
			}else{
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			log.eLog(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	public void delete(List<EventView> evs) throws Exception {
		for(EventView ev:evs){
			delete(ev);
		}
	}
	
	private void delete(EventView view) throws Exception {
		String del = "delete from EventView where _id='" + view.getId() + "'";
		try {
			MongoConnect.executeUpdate(del);
		} catch (Exception e) {
			log.eLog(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void deleteAll(User user) throws Exception {
		String del = "delete from EventView where loginName='" + user.getLoginName() + "'";
		try {
			MongoConnect.executeUpdate(del);
		} catch (Exception e) {
			log.eLog(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public List<String> tmptmp(String ci) throws Exception {
		List<String> ret = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			String sql = "select _id from Ci where id like '"+ci+"'";
			pstmt = conn.prepareStatement(sql);
//			pstmt.setString(1, ci);
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				ret.add(rs.getString("_id"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
					log.eLog(e.getMessage());
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					log.eLog(e.getMessage());
				}
			}
		}
		return ret;
	}
}
