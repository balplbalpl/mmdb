package com.mmdb.model.mapping.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.mongo.MongoConnect;

/**
 * 关系映射 Created by XIE on 2015/3/31.
 */
@Component("sourceToRelMapStorage")
public class SourceToRelationStorage {
	private Log log = LogFactory.getLogger("DataSourceToRelationStorage");

	/**
	 * 获取一条db映射
	 * 
	 * @param id
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public SourceToRelationMapping getById(String id) throws Exception {
		List<SourceToRelationMapping> list = this.getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库关系映射[" + id + "]不唯一");
			throw new Exception("数据库关系映射[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	/**
	 * 获取一条db映射
	 * 
	 * @param name
	 *            映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public SourceToRelationMapping getByName(String name) throws Exception {
		List<SourceToRelationMapping> list = this.getByProperty("name", name);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库关系映射[" + name + "]不唯一");
			throw new Exception("数据库关系映射[" + name + "]不唯一");
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

	public List<SourceToRelationMapping> getByProperty(String attr, String value)
			throws Exception {
		String match = "select * from SourceToRelationMapping where `" + attr
				+ "` = '" + value + "'";
		return query(match);
	}

	public List<SourceToRelationMapping> getAll() throws Exception {
		String match = "select * from SourceToRelationMapping";
		return query(match);
	}

	public void deleteAll() throws Exception {
		String del = "delete from SourceToRelationMapping";
		MongoConnect.executeUpdate(del);
	}

	public void delete(SourceToRelationMapping mapping) throws Exception {
		String del = "delete from SourceToRelationMapping where _id = '"
				+ mapping.getId() + "'";
		MongoConnect.executeUpdate(del);
	}

	public SourceToRelationMapping save(SourceToRelationMapping mapping)
			throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("insert into SourceToRelationMapping(name,dataSourceId,relCateId,relValue,sourceCateId,sourceField,targetCateId,targetField,owner) values(?,?,?,?,?,?,?,?,?)");
			pstmt.setString(1, mapping.getName()==null ? "":mapping.getName());
			pstmt.setString(2, mapping.getDataSourceId()==null ? "":mapping.getDataSourceId());
			pstmt.setString(3, mapping.getRelCateId()==null ? "":mapping.getRelCateId());
			pstmt.setString(4, mapping.getRelValue()==null ? "{}":JSONObject.fromObject(mapping.getRelValue()).toString());
			pstmt.setString(5, mapping.getSourceCateId()==null ? "":mapping.getSourceCateId());
			pstmt.setString(6, mapping.getSourceField()==null ? "":mapping.getSourceField());
			pstmt.setString(7, mapping.getTargetCateId()==null ? "":mapping.getTargetCateId());
			pstmt.setString(8, mapping.getTargetField()==null ? "":mapping.getTargetField());
			pstmt.setString(9, mapping.getOwner());
			pstmt.executeUpdate();
			List<SourceToRelationMapping> retDp = getByProperty("name", mapping.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			delete(mapping);
			throw e;
		}
	}

	public SourceToRelationMapping update(SourceToRelationMapping mapping)
			throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("update SourceToRelationMapping set name=?,dataSourceId=?,relCateId=?,relValue=?,sourceCateId=?,sourceField=?,targetCateId=?,targetField=? where _id=?");
			pstmt.setString(1, mapping.getName()==null ? "":mapping.getName());
			pstmt.setString(2, mapping.getDataSourceId()==null ? "":mapping.getDataSourceId());
			pstmt.setString(3, mapping.getRelCateId()==null ? "":mapping.getRelCateId());
			pstmt.setString(4, mapping.getRelValue()==null ? "{}":JSONObject.fromObject(mapping.getRelValue()).toString());
			pstmt.setString(5, mapping.getSourceCateId()==null ? "":mapping.getSourceCateId());
			pstmt.setString(6, mapping.getSourceField()==null ? "":mapping.getSourceField());
			pstmt.setString(7, mapping.getTargetCateId()==null ? "":mapping.getTargetCateId());
			pstmt.setString(8, mapping.getTargetField()==null ? "":mapping.getTargetField());
			pstmt.setString(9, mapping.getId());
			pstmt.executeUpdate();
			List<SourceToRelationMapping> retDp = getByProperty("name", mapping.getName());
			if(retDp.size()==1){
				return retDp.get(0);
			}else{
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 查询数据返回对象
	 * 
	 * @param cypher
	 *            match(n:SourceToRelationMapping) ... return n 返回的第一参数必须是
	 *            SourceToRelationMapping对象的节点
	 * @param ciCates
	 *            防止getAll时重复调用getOne方法,而使用的缓存
	 * @param relCates防止getAll时重复调用getOne方法
	 *            ,而使用的缓存
	 * @param dbPools防止getAll时重复调用getOne方法
	 *            ,而使用的缓存
	 * @return
	 * @throws Exception
	 */
	public List<SourceToRelationMapping> query(String sql) throws Exception {
		List<SourceToRelationMapping> ret = new ArrayList<SourceToRelationMapping>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			Connection conn = MongoConnect.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				SourceToRelationMapping cr = new SourceToRelationMapping();
				cr.setId(rs.getString("_id"));
				cr.setName(rs.getString("name"));
				cr.setDataSourceId(rs.getString("dataSourceId"));
				cr.setRelCateId(rs.getString("relCateId"));
				JSONObject relValue = null;
				try{
					relValue = JSONObject.fromObject(rs.getString("relValue"));
				}catch(Exception e){
					relValue = new JSONObject();
				}
				cr.setRelValue(relValue);
				cr.setSourceCateId(rs.getString("sourceCateId"));
				cr.setSourceField(rs.getString("sourceField"));
				cr.setTargetCateId(rs.getString("targetCateId"));
				cr.setTargetField(rs.getString("targetField"));
				cr.setOwner(rs.getString("owner"));
				ret.add(cr);
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

	public static void main(String[] args) {
		// SourceToRelationStorage s = new SourceToRelationStorage();
		// s.getById(id)

	}
}
