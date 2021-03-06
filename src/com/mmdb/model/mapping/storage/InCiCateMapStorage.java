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
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.mongo.MongoConnect;

/**
 * 内部配置项分类[内部映射] - 存储仓库
 * 
 * @author XIE
 * 
 */
@Component("inCiCateMapStorage")
public class InCiCateMapStorage {
	private Log log = LogFactory.getLogger("InCiCateMapStorage");

	/**
	 * 根据name获取一条内部映射
	 * 
	 * @param uName
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public InCiCateMap getByName(String uName) throws Exception {
		List<InCiCateMap> list = this.getByProperty("name", uName);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("配置项内部映射[" + uName + "]不唯一");
			throw new Exception("配置项内部映射[" + uName + "]不唯一");
		} else {
			return null;
		}
	}

	public InCiCateMap getById(String id) throws Exception {
		List<InCiCateMap> list = this.getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("配置项内部映射[" + id + "]不唯一");
			throw new Exception("配置项内部映射[" + id + "]不唯一");
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

	public List<InCiCateMap> getAll() throws Exception {
		String match = "select * from InCiCateMap";
		return query(match);
	}

	public InCiCateMap save(InCiCateMap in) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("insert into InCiCateMap(name,relCateId,relValue,startCateId,startCateField,endCateId,endCateField,owner) values(?,?,?,?,?,?,?,?)");
			pstmt.setString(1, in.getName()==null ? "":in.getName());
			pstmt.setString(2, in.getRelCateId()==null ? "":in.getRelCateId());
			pstmt.setString(3, in.getRelValue()==null ? "{}":JSONObject.fromObject(in.getRelValue()).toString());
			pstmt.setString(4, in.getStartCateId()==null ? "":in.getStartCateId());
			pstmt.setString(5, in.getStartCateField()==null ? "":in.getStartCateField());
			pstmt.setString(6, in.getEndCateId()==null ? "":in.getEndCateId());
			pstmt.setString(7, in.getEndCateField()==null ? "":in.getEndCateField());
			pstmt.setString(8, in.getOwner());
			pstmt.executeUpdate();
			List<InCiCateMap> retDp = getByProperty("name", in.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			delete(in);
			throw e;
		}
	}

	public void delete(InCiCateMap im) throws Exception {
		String del = "delete from InCiCateMap where _id='" + im.getId() + "'";
		try {
			MongoConnect.executeUpdate(del);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteAll() throws Exception {
		String del = "delete from InCiCateMap";
		MongoConnect.executeUpdate(del);
	}

	public InCiCateMap update(InCiCateMap in) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("update InCiCateMap set name=?,relCateId=?,relValue=?,startCateId=?,startCateField=?,endCateId=?,endCateField=? where _id=?");
			pstmt.setString(1, in.getName()==null ? "":in.getName());
			pstmt.setString(2, in.getRelCateId()==null ? "":in.getRelCateId());
			pstmt.setString(3, in.getRelValue()==null ? "{}":JSONObject.fromObject(in.getRelValue()).toString());
			pstmt.setString(4, in.getStartCateId()==null ? "":in.getStartCateId());
			pstmt.setString(5, in.getStartCateField()==null ? "":in.getStartCateField());
			pstmt.setString(6, in.getEndCateId()==null ? "":in.getEndCateId());
			pstmt.setString(7, in.getEndCateField()==null ? "":in.getEndCateField());
			pstmt.setString(8, in.getId());
			pstmt.executeUpdate();
			List<InCiCateMap> retDp = getByProperty("name", in.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public void update(List<InCiCateMap> ims) throws Exception {
		for(InCiCateMap im:ims){
			update(im);
		}
	}

	public List<InCiCateMap> getByProperty(String key, String val)
			throws Exception {
		String match = "select * from InCiCateMap where `" + key + "` = '"
				+ val + "'";
		return query(match);
	}

	private List<InCiCateMap> query(String sql) throws SQLException {
		List<InCiCateMap> ret = new ArrayList<InCiCateMap>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				InCiCateMap cc = new InCiCateMap();
				cc.setId(rs.getString("_id"));
				cc.setName(rs.getString("name"));
				cc.setRelCateId(rs.getString("relCateId"));
				JSONObject relValue = null;
				try{
					relValue = JSONObject.fromObject(rs.getString("relValue"));
				}catch(Exception e){
					relValue = new JSONObject();
				}
				cc.setRelValue(relValue);
				cc.setStartCateId(rs.getString("startCateId"));
				cc.setStartCateField(rs.getString("startCateField"));
				cc.setEndCateId(rs.getString("endCateId"));
				cc.setEndCateField(rs.getString("endCateField"));
				cc.setOwner(rs.getString("owner"));
				ret.add(cc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return ret;
	}

}
