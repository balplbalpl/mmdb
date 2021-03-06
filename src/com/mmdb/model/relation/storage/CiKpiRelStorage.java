package com.mmdb.model.relation.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;
import com.mmdb.util.HexString;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Component("ciKpiRelStorage")
public class CiKpiRelStorage {
	private Log log = LogFactory.getLogger("CiKpiRelStorage");

	public List<Map<String, String>> getAllCiKpiRel() throws SQLException {
		String sql = "select * from CiKpiRelation order by ciId";
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				Map<String, String> rel = new HashMap<String, String>();
				rel.put("ciid", rs.getString("ciId"));
				rel.put("kpiid", rs.getString("kpiId"));
				rel.put("autoRelation", rs.getString("autoRelation"));
				rel.put("hasData", rs.getString("hasData"));
				ret.add(rel);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
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

	/**
	 * 通过CI的ID获取到这个CI下的所有KPI
	 * 
	 * @param ciId
	 * @return List<Map<String, String>>
	 * @throws SQLException
	 */
	public List<Map<String, String>> getRelByCiId(String ciId)
			throws SQLException {
		String sql = "select * from CiKpiRelation where ciId='" + ciId + "'";
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				Map<String, String> rel = new HashMap<String, String>();
				rel.put("ciId", rs.getString("ciId"));
				rel.put("kpiId", rs.getString("kpiId"));
				rel.put("autoRelation", rs.getString("autoRelation"));
				rel.put("hasData", rs.getString("hasData"));
				ret.add(rel);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
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

	public Integer getCiKpiRelCount() throws SQLException {
		String sql = "select * from CiKpiRelation";
		MongoStatement pstmt = null;
		Connection conn = MongoConnect.getConnection();
		int count = 0;
		try {
			pstmt = (MongoStatement) conn.createStatement();
			count = pstmt.executeQueryCount(sql);
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return count;
	}

	/**
	 * 增加CI KPI关系
	 * 
	 * @param ciid ci的16进制ID
	 * @param kpiid kpi的16进制ID
	 * @param autoRelation 是否是自动关联的
	 * @param hasData 关系下是否有数据
	 * 
	 * @return 新增:true 更新:false
	 */
	public boolean addCiKpiRel(String ciid, String kpiid, boolean autoRelation,
			boolean hasData) {
		try {
			boolean isSave = true;
			DB db = MongoConnect.getDb();
			DBCollection collection = db.getCollection("CiKpiRelation");
			BasicDBObject queryObj = new BasicDBObject();
			queryObj.put("ciId", ciid);
			queryObj.put("kpiId", kpiid);

			DBCursor queryList = collection.find(queryObj);
			// 判断是否存在，如果存在则更新，不存在则插入
			if (queryList.hasNext()) {
				DBObject obj = queryList.next();
				obj.put("hasData", hasData + "");
				obj.put("autoRelation", autoRelation + "");
				collection.update(queryObj, obj, true, false);
				isSave = false;
			} else {
				BasicDBObject newObj = new BasicDBObject();
				newObj.put("ciId", ciid);
				newObj.put("kpiId", kpiid);
				
				String ciName = "";
				String kpiName = "";
				String ciCateName = "";
				String kpiCateName = "";
				try {
					String ciHex = HexString.decode(ciid);
					String kpiHex = HexString.decode(kpiid);
					JSONArray cis = JSONArray.fromObject(ciHex);
					JSONArray kpis =  JSONArray.fromObject(kpiHex);
					ciCateName = cis.getString(0);
					ciName = cis.getString(1);
					kpiCateName= kpis.getString(0);
					kpiName = kpis.getString(1);		
				} catch (JSONException e) {
					log.eLog("解析Hex发生错误",e);
				}
				
				//modify at 2015-9-19增加四个字段
				newObj.put("ciName", ciName);
				newObj.put("ciCategoryName", ciCateName);
				newObj.put("kpiName", kpiName);
				newObj.put("kpiCategoryName", kpiCateName);
				
				// 标识kpi和ci的关系是否是自动关联上的
				newObj.put("autoRelation", autoRelation + "");
				newObj.put("hasData", hasData + "");
				//collection.insert(newObj);
				//modify at 2015-9-18，改成update方法确保不会有重复数据
				collection.update(queryObj, newObj, true, false);
			}
			return isSave;
		} catch (Exception e) {
			log.eLog("保存CI及KPI映射关系时发生异常", e);
			return false;
		}
	}

	
	/**
	 * 通过rel id列表获取到指定的关系
	 * 
	 * @param ciId
	 * @return List<Map<String, String>>
	 * @throws SQLException
	 */
	public List<Map<String, String>> getRelByRelIds(List<String> relIds)
			throws SQLException {
		
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			StringBuffer idsSql = new StringBuffer();
			for (String relId : relIds) {
				idsSql.append(" _id = '");
				idsSql.append(relId);
				idsSql.append("' or");
			}
			idsSql.delete(idsSql.length() - 2, idsSql.length());
			String sql = "select * from CiKpiRelation where " + idsSql;
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				Map<String, String> rel = new HashMap<String, String>();
				rel.put("ciId", rs.getString("ciId"));
				rel.put("kpiId", rs.getString("kpiId"));
				rel.put("autoRelation", rs.getString("autoRelation"));
				rel.put("hasData", rs.getString("hasData"));
				ret.add(rel);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
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
	
	/**
	 * 批量删除关系(有分页限制最多每次删除30个)
	 * 
	 * @param relIds
	 *            rel ID集合
	 * @throws Exception
	 */
	public void deleteRelByIds(List<String> relIds) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			StringBuffer idsSql = new StringBuffer();
			for (String relId : relIds) {
				idsSql.append(" _id = '");
				idsSql.append(relId);
				idsSql.append("' or");
			}
			idsSql.delete(idsSql.length() - 2, idsSql.length());
			String delSql = "delete from CiKpiRelation where " + idsSql;
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel by ids error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}
	
	/**
	 * 删除CI KPI关系
	 * 
	 * @param ciId
	 * @param kpiIds
	 */
	public void deleteCiKpiRel(String ciId, List<String> kpiIds)
			throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			StringBuffer idsSql = new StringBuffer();
			for (String kpiId : kpiIds) {
				idsSql.append(" kpiId = '");
				idsSql.append(kpiId);
				idsSql.append("' or");
			}
			idsSql.delete(idsSql.length() - 2, idsSql.length());
			String delSql = "delete from CiKpiRelation where ciId= '" + ciId
					+ "' and (" + idsSql + ")";
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}

	public void deleteCiKpiRelByCi(String ciId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			String delSql = "delete from CiKpiRelation where ciId= '" + ciId
					+ "'";
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}

	/**
	 * 通过Ci的分类名称删除关系
	 * 
	 * @param ciCategoryName
	 * @throws Exception
	 */
	public void deleteRelByCiCateName(String ciCategoryName) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			String delSql = "delete from CiKpiRelation where ciCategoryName= '" + ciCategoryName
					+ "'";
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}
	
	/**
	 * 通过Kpi的分类名称删除关系
	 * 
	 * @param kpiCategoryName
	 * @throws Exception
	 */
	public void deleteRelByKpiCateName(String kpiCategoryName) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			String delSql = "delete from CiKpiRelation where kpiCategoryName= '" + kpiCategoryName
					+ "'";
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}
	
	public void deleteCiKpiRelByKpi(String kpiId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			String delSql = "delete from CiKpiRelation where kpiId= '" + kpiId
					+ "'";
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete rel error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}
	
	/**
	 * 删除所有CI KPI关系
	 * 
	 * @throws Exception
	 */
	public void deleteAll()throws Exception{
		MongoConnect.executeUpdate("delete from CiKpiRelation");
	}
	
	/**
	 * 通过Ci的Hex列表统计符合条件的记录数
	 * 
	 * @param ciHexs
	 * @return count
	 */
	public int getCountByCiHex(List<String> ciHexs) {
		int count = 0;
		if (ciHexs == null || ciHexs.size() == 0)
			return count;
		try {
			BasicDBList query = new BasicDBList();
			for (String ciHex : ciHexs) {
				query.add(ciHex);
			}
			//mongo的bug当or和sort一起使用时查询速度会特别慢
			//将Or查询改为In查询，速度会快很多
			BasicDBObject cond = new BasicDBObject();
			cond.put("ciId",new BasicDBObject("$in",query));
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("CiKpiRelation");
			DBCursor find = ciColl.find(cond);
			count = find.count();
		} catch (Exception e) {
			log.eLog(e.getMessage(),e);
		}
		return count;
	}
	
	/**
	 * 通过Ci分类查询关系记录数
	 * 
	 * @param ciCateName
	 * @return
	 */
	public int getCountByCiCate(String ciCateName) {
		Connection conn = MongoConnect.getConnection();
		MongoStatement stmt = null;
		try {
			String count = "select * from CiKpiRelation where `ciCategoryName` = '" + ciCateName
					+ "'";
			stmt = (MongoStatement) conn.createStatement();
			
			return stmt.executeQueryCount(count);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
		return 0;
	}
	
	/**
	 * 通过Ci分类的名称查询关系列表
	 * 
	 * @param ciCateName
	 * @param start
	 * @param limit
	 * 
	 * @return List<Map<String, String>> 
	 */
	public List<Map<String, String>> getRelByCiCate(String ciCateName,
			int start, int limit) {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		if (ciCateName == null || ciCateName.length()<= 0)
			return ret;
		try {
			StringBuffer match = new StringBuffer(
					"select * from CiKpiRelation where ");
			match.append("`ciCategoryName` = '").append(ciCateName).append("'");
				
			match.append(" order by ciId,kpiId limit ").append(start)
					.append(",").append(limit);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(match.toString());
			while (rs.next()) {
				Map<String, String> rel = new HashMap<String, String>();
				rel.put("id", rs.getString("_id"));
				rel.put("ciId", rs.getString("ciId"));
				rel.put("kpiId", rs.getString("kpiId"));
				rel.put("autoRelation", rs.getString("autoRelation"));
				rel.put("hasData", rs.getString("hasData"));
				rel.put("ciName", rs.getString("ciName"));
				rel.put("ciCategoryName", rs.getString("ciName"));
				rel.put("kpiName", rs.getString("kpiName"));
				rel.put("kpiCategoryName", rs.getString("kpiCategoryName"));
				ret.add(rel);
			}
		} catch (Exception e) {
			log.eLog(e.getMessage());
		} finally {
			MongoConnect.colse(stmt,rs);
		}
		return ret;
	}
	
	/**
	 * 通过Ci/Kpi分类的名称获取Ci/Kpi列表
	 * 
	 * @param cateName 分类名称
	 * @param type [ci/kpi]
	 * 
	 * @return List<String> 
	 */
	public List<String> getCiByCiCate(String cateName,String type) {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		List<String> ret = new ArrayList<String>();
		if (cateName == null || cateName.length()<= 0)
			return ret;
		try {
			StringBuffer match = new StringBuffer();
			if("kpi".equalsIgnoreCase(type)){
				match.append("select kpiId from CiKpiRelation where ");
				match.append("`kpiCategoryName` = '").append(cateName).append("'");
			}else{
				match.append("select ciId from CiKpiRelation where ");
				match.append("`ciCategoryName` = '").append(cateName).append("'");
			}
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(match.toString());
			while (rs.next()) {
				ret.add(rs.getString(1));
			}
		} catch (Exception e) {
			log.eLog(e.getMessage(),e);
		} finally {
			MongoConnect.colse(stmt,rs);
		}
		return ret;
	}
	

	/**
	 * 通过ci hex列表批量查询关系信息
	 * 
	 * @param ciHexs
	 * @param start
	 * @param limit
	 * @return
	 */
	public List<Map<String, String>> getKpiByCiHexs(List<String> ciHexs,
			int start, int limit) {
		List<Map<String, String>> ret = new ArrayList<Map<String, String>>();
		if (ciHexs == null || ciHexs.size() == 0)
			return ret;
		try {
			BasicDBList query = new BasicDBList();
			for (String ciHex : ciHexs) {
				/*BasicDBList l = new BasicDBList();
				l.add(new BasicDBObject("ciId", ciHex));
				BasicDBObject o = new BasicDBObject("$or", l);*/
				query.add(ciHex);
			}
			//mongo的bug当or和sort一起使用时查询速度会特别慢
			//将Or查询改为In查询，速度会快很多
			BasicDBObject cond = new BasicDBObject();
			cond.put("ciId",new BasicDBObject("$in",query));
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("CiKpiRelation");
			DBCursor find = ciColl.find(cond).sort(new BasicDBObject("ciId", 1).append("kpiId", 1)).skip(start).limit(limit);
			for (DBObject dbObject : find) {
				Map<String, String> rel = new HashMap<String, String>();
				rel.put("id", dbObject.get("_id").toString());
				rel.put("ciId", dbObject.get("ciId").toString());
				rel.put("kpiId", dbObject.get("kpiId").toString());
				rel.put("autoRelation", dbObject.get("autoRelation").toString());
				rel.put("hasData", dbObject.get("hasData").toString());
				rel.put("ciName", dbObject.get("ciName").toString());
				rel.put("ciCategoryName", dbObject.get("ciCategoryName").toString());
				rel.put("kpiName", dbObject.get("kpiName").toString());
				rel.put("kpiCategoryName", dbObject.get("kpiCategoryName").toString());
				ret.add(rel);
			}
		} catch (Exception e) {
			log.eLog(e.getMessage(),e);
		}
		return ret;
	}
	
	
	/**
	 * 通过kpi hex列表批量删除关系
	 * 
	 * @param kpiHexs
	 * @return
	 */
	public void delByKpiHexs(List<String> kpiHexs) {
		try {
			BasicDBList query = new BasicDBList();
			for (String kpiHex : kpiHexs) {
				query.add(kpiHex);
			}
			BasicDBObject cond = new BasicDBObject();
			cond.put("kpiId",new BasicDBObject("$in",query));
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("CiKpiRelation");
			ciColl.remove(cond);
		} catch (Exception e) {
			log.eLog("批量删除关系时发生错误："+e.getMessage(),e);
		}
	}
	
}
