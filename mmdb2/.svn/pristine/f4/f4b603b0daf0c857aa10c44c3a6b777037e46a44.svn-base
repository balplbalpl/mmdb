package com.mmdb.model.info.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * KPI数据 - 存储仓库
 * 
 * @author yuhao.guan
 */
@Component("kpiInfoStorage")
public class KpiInfoStorage {
	private Log log = LogFactory.getLogger("KpiInfoStorage");

	/**
	 * 获取指定分类下的数据
	 * 
	 * @param cateId
	 *            分类id
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getByCategory(String cateId) throws Exception {
		List<KpiInformation> ret = getByProperty("kpiCategoryId", cateId);
		if (ret != null) {
			return ret;
		} else {
			throw new Exception("获取数据异常");
		}
	}

	/**
	 * 获取指定用户下的数据
	 * 
	 * @param cateId
	 *            分类id
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getByUser(String userName) throws Exception {
		List<KpiInformation> ret = getByProperty("owner", userName);
		return ret;
	}
	
	public KpiInformation getKpiById(String kpiId) throws Exception {
		List<KpiInformation> ret = getByProperty("_id", kpiId);
		if (ret != null) {
			if (ret.size() == 1) {
				return ret.get(0);
			} else if (ret.size() > 1) {
				throw new Exception("数据[" + kpiId + "]不唯一");
			} else {
				return null;
			}
		} else {
			throw new Exception("获取数据异常");
		}
	}

	public KpiInformation getKpiByName(String cateId,String name) throws Exception {
		String whereParam = " kpiCategoryId='"+cateId+"' and name = '"+name+"'";
		//List<KpiInformation> ret = getByProperty("name", name);
		List<KpiInformation> ret = this.findBySql(whereParam);
		if (ret != null) {
			if (ret.size() == 1) {
				return ret.get(0);
			} else if (ret.size() > 1) {
				throw new Exception("数据[" + name + "]不唯一");
			} else {
				return null;
			}
		} else {
			throw new Exception("获取数据异常");
		}
	}

	public KpiInformation getKpiByHex(String hexId) throws Exception {
		List<KpiInformation> ret = getByProperty("kpiHex", hexId);
		if (ret != null) {
			if (ret.size() == 1) {
				return ret.get(0);
			} else if (ret.size() > 1) {
				throw new Exception("数据[" + hexId + "]不唯一");
			} else {
				return null;
			}
		} else {
			throw new Exception("获取数据异常");
		}
	}
	
	/**
	 * 通过mongoId获取十六进制id
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public String getHexById(String id) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "select kpiHex from  Kpi where _id = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, id);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				return rs.getString("kpiHex");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
		return null;
	}

	public List<String> getHexByCateId(String cateId) throws Exception {
		List<String> ret = new ArrayList<String>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "select kpiHex from  Kpi where kpiCategoryId = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, cateId);
			ResultSet rs = pst.executeQuery();
			while (rs.next()) {
				ret.add(rs.getString("kpiHex"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
		return ret;
	}

	public KpiInformation save(KpiInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "insert into Kpi(name,kpiCategoryId,kpiCategoryName,kpiHex,threshold,source,unit,owner) values(?,?,?,?,?,?,?,?)";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getName());
			pst.setString(2, t.getKpiCategoryId());
			pst.setString(3, t.getKpiCategoryName());
			pst.setString(4, t.getKpiHex());
			pst.setString(5, t.getThreshold());
			pst.setString(6, t.getSource());
			pst.setString(7, t.getUnit());
			pst.setString(8, t.getOwner());
			pst.executeUpdate();
			return getKpiByName(t.getKpiCategoryId(),t.getName());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	public KpiInformation update(KpiInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update Kpi set name=?,kpiCategoryId=?,kpiCategoryName=?,kpiHex=?,threshold=?,source=?,unit=? where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getName());
			pst.setString(2, t.getKpiCategoryId());
			pst.setString(3, t.getKpiCategoryName());
			pst.setString(4, t.getKpiHex());
			pst.setString(5, t.getThreshold());
			pst.setString(6, t.getSource());
			pst.setString(7, t.getUnit());
			pst.setString(8, t.getId());
			pst.executeUpdate();
			return getKpiById(t.getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	public void delete(KpiInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from Kpi where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getId());
			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	/**
	 * 批量删除KPI
	 * 
	 * @param kpiIds
	 *            kpiId集合
	 * @throws Exception
	 */
	public void deleteKpiByIds(List<String> kpiIds) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement stmt = null;
		try {
			StringBuffer idsSql = new StringBuffer();
			for (String kpiId : kpiIds) {
				idsSql.append(" _id = '");
				idsSql.append(kpiId);
				idsSql.append("' or");
			}
			idsSql.delete(idsSql.length() - 2, idsSql.length());
			String delSql = "delete from Kpi where " + idsSql;
			stmt = conn.createStatement();
			stmt.executeUpdate(delSql);
		} catch (Exception e) {
			log.eLog("Delete kpi by ids error!", e);
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			stmt = null;
		}
	}

	public void deleteByCategory(KpiCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from Kpi where kpiCategoryId=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getId());
			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	public void deleteAllKpi() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from Kpi";
			pst = conn.prepareStatement(sql);
			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	/**
	 * 获取到全部的KPI分类
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getAll() throws Exception {
		String sql = "select * from Kpi";
		List<KpiInformation> ret = query(sql);
		if (ret != null) {
			return ret;
		} else {
			throw new Exception("获取数据异常");
		}
	}

	/**
	 * 查找某个分类下的KPI（不包含子分类）
	 * 
	 * @param cateId 分类Id
	 * @param param KPI名称的模糊查询参数
	 * @param userName 用户名
	 * @param start 分页起始记录
	 * @param limit 分页条数
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> find(String cateId, String param, String userName,
			int start, int limit)
			throws Exception {
		String sql = "select * from Kpi";
		boolean where = false;
		if (cateId != null && cateId.length() > 0) {
			sql = sql + " where kpiCategoryId='" + cateId + "'";
			where = true;
		}
		if (param != null && param.length() > 0) {
			if (where) {
				sql = sql + " and name like '%" + param + "%'";
			} else {
				sql = sql + " where name like '%" + param + "%'";
				where = true;
			}
		}
		if (userName != null && userName.length() > 0) {
			if (where) {
				sql = sql + " and owner = '" + userName + "'";
			} else {
				sql = sql + " where owner = '" + userName + "'";
			}
		}
		if(start != -1){
			sql = sql + " order by name limit "+start+ ","+limit;
		}
		List<KpiInformation> ret = query(sql);
		if (ret != null) {
			return ret;
		} else {
			throw new Exception("获取数据异常");
		}
	}
	
	/**
	 * 统计某个分类下的KPI（不包含子分类）的记录总数
	 * 
	 * @param cateId 分类Id
	 * @param param KPI名称的模糊查询参数
	 * @param userName 用户名
	 * @return
	 * @throws Exception
	 */
	public int countFind(String cateId, String param, String userName)
			throws Exception {
		String sql = "";
		boolean where = false;
		if (cateId != null && cateId.length() > 0) {
			sql = sql + " kpiCategoryId='" + cateId + "'";
			where = true;
		}
		if (param != null && param.length() > 0) {
			if (where) {
				sql = sql + " and name like '%" + param + "%'";
			} else {
				sql = sql + " name like '%" + param + "%'";
				where = true;
			}
		}
		if (userName != null && userName.length() > 0) {
			if (where) {
				sql = sql + " and owner = '" + userName + "'";
			} else {
				sql = sql + " owner = '" + userName + "'";
			}
		}
		int ret = this.countBySql(sql.toString());
		return ret;
	}

	public List<KpiInformation> getByProperty(String key, String val) {
		String sql = "select * from Kpi where `" + key + "`='" + val + "'";
		return query(sql);
	}

	/**
	 * 通过sql语句中where条件查询KPI
	 * 
	 * @param whereParam
	 * @return
	 */
	public List<KpiInformation> findBySql(String whereParam) {
		String sql = "select * from Kpi where " + whereParam;
		return query(sql);
	}
	
	/**
	 * 通过sql语句中where条件统计记录总数
	 * 
	 * @param whereParam
	 * @return
	 */
	public int countBySql(String whereParam) {
		String sql = "select _id from Kpi where " + whereParam;
		Connection conn = MongoConnect.getConnection();
		MongoStatement st = null;
		int count = 0;
		try {
			st = (MongoStatement)conn.createStatement();
			count = st.executeQueryCount(sql);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			MongoConnect.colse(st, null);
		}
	}
	

	private List<KpiInformation> query(String sql) {
		List<KpiInformation> result = new ArrayList<KpiInformation>();
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					KpiInformation info = new KpiInformation();
					info.setId(rs.getString("_id"));
					info.setName(rs.getString("name"));
					info.setKpiCategoryId(rs.getString("kpiCategoryId"));
					info.setKpiCategoryName(rs.getString("kpiCategoryName"));
					info.setKpiHex(rs.getString("kpiHex"));
					info.setThreshold(rs.getString("threshold"));
					info.setSource(rs.getString("source"));
					info.setUnit(rs.getString("unit"));
					info.setOwner(rs.getString("owner"));
					result.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			st = null;
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			rs = null;
		}
		return result;
	}

	/**
	 * 通过ID列表 查询kpi
	 * 
	 * @return List<KpiInformation>
	 */
	public List<KpiInformation> getByKpiIds(List<String> kpiIds){
		List<KpiInformation> ret = new ArrayList<KpiInformation>();
		if (kpiIds == null || kpiIds.size() == 0)
			return ret;
		try {
			BasicDBList query = new BasicDBList();
			for (String kpiId : kpiIds) {
				//DBObject idObject =new BasicDBObject();  
				//idObject.put("_id", new ObjectId(kpiId));
				query.add( new ObjectId(kpiId));
			}
			BasicDBObject cond = new BasicDBObject();
			cond.put("_id",new BasicDBObject("$in",query));
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("Kpi");
			DBCursor find = ciColl.find(cond);
			for (DBObject dbObject : find) {
				KpiInformation info = new KpiInformation();
				info.setId(dbObject.get("_id").toString());
				info.setName(dbObject.get("name").toString());
				info.setKpiCategoryId(dbObject.get("kpiCategoryId").toString());
				info.setKpiCategoryName(dbObject.get("kpiCategoryName").toString());
				info.setKpiHex(dbObject.get("kpiHex").toString());
				info.setThreshold(dbObject.get("threshold").toString());
				info.setSource(dbObject.get("source").toString());
				info.setUnit(dbObject.get("unit").toString());
				info.setOwner(dbObject.get("owner").toString());
				ret.add(info);
			}
		} catch (Exception e) {
			log.eLog(e.getMessage(),e);
		}
		return ret;
	}
	
	public List<String> getMongoIdsForCiKpiRel(String cateName, String kpi) {
		List<String> result = new ArrayList<String>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder("select * from Kpi ");
			List<String> valList = new ArrayList<String>();
			boolean where = false;
			if (cateName != null) {
				sql.append("where kpiCategoryName=? ");
				valList.add(cateName);
				where = true;
			}
			if (kpi != null) {
				if (where) {
					sql.append("and name=? ");
				} else {
					sql.append("where name=? ");
				}
				valList.add(kpi);
			}
			pst = conn.prepareStatement(sql.toString());
			for (int i = 0; i < valList.size(); i++) {
				pst.setString(i + 1, valList.get(i));
			}
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					result.add(rs.getString("kpiHex"));
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			pst = null;
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			rs = null;
		}
	}
	
	public List<List<KpiInformation>> saveOrUpdate(KpiCategory category,
			List<KpiInformation> infos) throws Exception {
		List<List<KpiInformation>> ret = new ArrayList<List<KpiInformation>>(
				infos.size());
		if (infos.size() > 0) {
			Set<String> allJsonIds = new HashSet<String>();// 存在的jsonId
			List<KpiInformation> crt = new ArrayList<KpiInformation>();
			List<KpiInformation> upd = new ArrayList<KpiInformation>();
			String match = "select distinct kpiHex from Kpi where kpiCategoryId = '"
					+ category.getId() + "'";
			Connection conn = MongoConnect.getConnection();
			Statement statm = null;
			try {
				statm = conn.createStatement();
				ResultSet rs = statm.executeQuery(match);
				while (rs.next()) {
					String jsonId = rs.getString("kpiHex");
					allJsonIds.add(jsonId);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (statm != null) {
					statm.close();
				}
			}
			for (KpiInformation info : infos) {
				String jsonId = info.getKpiHex();
				if (allJsonIds.contains(jsonId)) {
					upd.add(info);
				} else {
					crt.add(info);
				}
			}

			insertInfo(crt);
			updateInfo(upd);
			ret.add(crt);
			ret.add(upd);
			return ret;
		}
		return null;
	}
	
	public void insertInfo(List<KpiInformation> ciInfos) throws Exception {
		if (ciInfos != null && ciInfos.size() > 0) {
			KpiInformation tmp = ciInfos.get(0);
			Map<String, Object> tmpMap = tmp.toMap();
			// insert时不需要id
			tmpMap.remove("id");
			List<String> sortKeys = new ArrayList<String>(tmpMap.keySet());

			StringBuffer sql = new StringBuffer();
			sql.append("insert into Kpi(");
			for (String key : sortKeys) {
				sql.append("`");
				sql.append(key);
				sql.append("`,");
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(") values (");
			for (int j = 0; j < tmpMap.size(); j++) {
				sql.append("?,");
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(")");

			for (int i = 0; i < ciInfos.size(); i++) {
				Map<String, Object> infoMap = ciInfos.get(i).toMap();
				Connection conn = MongoConnect.getConnection();
				PreparedStatement pst = conn.prepareStatement(sql.toString());
				for (int j = 0, len = sortKeys.size(); j < len; j++) {
					pst.setObject(j + 1, infoMap.get(sortKeys.get(j)));
				}
				pst.executeUpdate();
			}
		}

	}
	
	public void updateInfo(List<KpiInformation> ciInfos) throws Exception {
		if (ciInfos != null && ciInfos.size() > 0) {
			KpiInformation tmp = ciInfos.get(0);
			Map<String, Object> tmpMap = tmp.toMap();
			// insert时不需要id
			tmpMap.remove("id");
			List<String> sortedKeys = new ArrayList<String>(tmpMap.keySet());
			StringBuffer sql = new StringBuffer();
			sql.append("update Kpi set ");
			for (String key : sortedKeys) {
				sql.append("`");
				sql.append(key);
				sql.append("`= ?,");
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(" where kpiHex = ?");

			Connection conn = MongoConnect.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql.toString());

			for (int i = 0; i < ciInfos.size(); i++) {
				KpiInformation ciInfo = ciInfos.get(i);
				Map<String, Object> infoMap = ciInfo.toMap();
				for (int j = 0, len = sortedKeys.size(); j < len; j++) {
					pst.setObject(j + 1, infoMap.get(sortedKeys.get(j)));
				}
				pst.setString(sortedKeys.size() + 1, (String)infoMap.get("kpiHex"));
				pst.executeUpdate();
			}
		}
	}
}