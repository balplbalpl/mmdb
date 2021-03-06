package com.mmdb.model.mapping.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.mongo.MongoConnect;

/**
 * 数据映射 Created by XIE on 2015/3/30.
 */
@Component("sourceToCateMapStorage")
public class SourceToCategoryStorage {
	private Log log = LogFactory.getLogger("DataSourceToCategoryStorage");

	/**
	 * 获取一条db映射
	 * 
	 * @param id
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public SourceToCategoryMapping getById(String id) throws Exception {
		List<SourceToCategoryMapping> list = getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库同步映射[" + id + "]不唯一");
			throw new Exception("数据库同步映射[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	public SourceToCategoryMapping getByName(String name) throws Exception {
		List<SourceToCategoryMapping> list = getByProperty("name", name);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库同步映射[" + name + "]不唯一");
			throw new Exception("数据库同步映射[" + name + "]不唯一");
		} else {
			return null;
		}
	}

	public boolean exist(String id) {
		try {
			if (getById(id) == null) {
				return false;
			}
		} catch (Exception e) {
		}
		return true;
	}

	public void delete(SourceToCategoryMapping t) throws Exception {
		String id = t.getId();
		String del = "delete from SourceToCategoryMapping where _id = '" + id
				+ "'";
		MongoConnect.executeUpdate(del);
	}

	public void deleteAll() throws Exception {
		String del = "delete from SourceToCategoryMapping";
		MongoConnect.executeUpdate(del);
	}

	public List<SourceToCategoryMapping> getAll() throws Exception {
		String match = "select * from SourceToCategoryMapping";
		return query(match);
	}

	public List<SourceToCategoryMapping> getByProperty(String key, Object value)
			throws Exception {
		String match = "select * from SourceToCategoryMapping where `" + key
				+ "` = '" + value + "'";
		return query(match);
	}

	public SourceToCategoryMapping save(SourceToCategoryMapping stcm)
			throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("insert into SourceToCategoryMapping(name,cateId,dataSourceId,fieldMap,owner) values(?,?,?,?,?)");
			pstmt.setString(1, stcm.getName()==null ? "":stcm.getName());
			pstmt.setString(2, stcm.getCateId()==null ? "":stcm.getCateId());
			pstmt.setString(3, stcm.getDataSourceId()==null ? "":stcm.getDataSourceId());
			pstmt.setString(4, stcm.getFieldMap()==null ? "{}":JSONObject.fromObject(stcm.getFieldMap()).toString());
			pstmt.setString(5, stcm.getOwner());
			
			pstmt.executeUpdate();
			List<SourceToCategoryMapping> retDp = getByProperty("name", stcm.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			delete(stcm);
			throw e;
		}
	}

	public SourceToCategoryMapping update(SourceToCategoryMapping t)
			throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("update SourceToCategoryMapping set name=?,cateId=?,dataSourceId=?,fieldMap=? where _id=?");
			pstmt.setString(1, t.getName()==null ? "":t.getName());
			pstmt.setString(2, t.getCateId()==null ? "":t.getCateId());
			pstmt.setString(3, t.getDataSourceId()==null ? "":t.getDataSourceId());
			pstmt.setString(4, t.getFieldMap()==null ? "{}":JSONObject.fromObject(t.getFieldMap()).toString());
			pstmt.setString(5, t.getId());
			pstmt.executeUpdate();
			List<SourceToCategoryMapping> retDp = getByProperty("name", t.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private List<SourceToCategoryMapping> query(String sql)
			throws SQLException {
		List<SourceToCategoryMapping> ret = new ArrayList<SourceToCategoryMapping>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Connection conn = MongoConnect.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				SourceToCategoryMapping cd = new SourceToCategoryMapping();
				cd.setId(rs.getString("_id"));
				cd.setName(rs.getString("name"));
				cd.setCateId(rs.getString("cateId"));
				cd.setDataSourceId(rs.getString("dataSourceId"));
				JSONObject fieldMap = null;
				try{
					fieldMap = JSONObject.fromObject(rs.getString("fieldMap"));
				}catch(Exception e){
					fieldMap = new JSONObject();
				}
				cd.setFieldMap(fieldMap);
				cd.setOwner(rs.getString("owner"));
				ret.add(cd);
			}
		} catch (Exception e) {
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return ret;
	}

}
