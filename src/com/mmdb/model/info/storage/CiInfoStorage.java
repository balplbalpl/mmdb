package com.mmdb.model.info.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.db.neo4jdb.Neo4jDao;
import com.mmdb.model.info.CiInformation;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;
import com.mmdb.util.HexString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 配置项数据 - 存储仓库
 * 
 * @author XIE
 */
@Component("ciInfoStorage")
public class CiInfoStorage {// extends NodeStorage<CiInformation>
	private Log log = LogFactory.getLogger("CiInfoStorage");

	/**
	 * 分类下获取一条数据
	 * 
	 * @param id
	 *            id(分类内不会重复)
	 * @return
	 * @throws Exception
	 */
	public CiInformation getById(String id) throws Exception {// 已重写xj
		List<CiInformation> list = getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			throw new MException("配置项数据[" + list.get(0).getName() + "]不唯一");
		} else {
			return null;
		}
	}

	public CiInformation getByJsonId(String jsonId) throws Exception {
		List<CiInformation> list = getByProperty("jsonId", jsonId);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			throw new MException("配置项数据[" + list.get(0).getName() + "]不唯一");
		} else {
			return null;
		}
	}

	public List<CiInformation> getByJsonIds(List<String> jsonIds)
			throws Exception {
		if (jsonIds == null || jsonIds.size() == 0) {
			return null;
		}
		List<CiInformation> ret = new ArrayList<CiInformation>();
		int r = jsonIds.size() / 50;
		if (jsonIds.size() % 50 != 0) {
			r++;
		}
		for (int i = 0; i < r; i++) {
			int s = i * 50;
			int e = s + 50;
			if (e > jsonIds.size()) {
				e = jsonIds.size();
			}
			StringBuffer js = new StringBuffer();
			for (int j = s; j < e; j++) {
				js.append(" jsonId = '");
				js.append(jsonIds.get(j));
				js.append("' or");
			}
			js.delete(js.length() - 2, js.length());
			String sql = "select * from Ci where " + js.toString();
			List<CiInformation> query = query(sql);
			ret.addAll(query);
		}
		return ret;
	}

	/**
	 * 获取指定分类下的数据
	 * 
	 * @param cateId
	 *            分类id
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> getByCategory(String cateId) throws Exception {
		String match = "select * from Ci where categoryId ='" + cateId + "'";
		return query(match);
	}

	public void delete(CiInformation info) throws Exception {
		String jsonId = info.getCiHex();

		// 必定会有一个关系ci2Cate
		String del = "match (n:Ci {jsonId:'" + jsonId
				+ "'})-[r]-(m) delete n,r";
		Neo4jDao.base(del);

		String delSql = "delete from Ci	where jsonId = '" + jsonId + "'";
		MongoConnect.executeUpdate(delSql);
	}

	public void deleteAll() throws Exception {
		String del = "match (n:Ci) optional match (n)-[r]-(m) delete n,r";
		Neo4jDao.base(del);
		String delSql = "delete from Ci";
		MongoConnect.executeUpdate(delSql);
	}

	/**
	 * 通过 CiInfo 的 neo4j id实现删除一组ci
	 * 
	 * @param list
	 *            ci必须包含id,categoryId,scene
	 * @throws Exception
	 */
	public void delete(List<CiInformation> list) throws Exception {
		if (list.size() == 0)
			return;
		StringBuffer infosAttr = new StringBuffer();
		// List<String> jsonIds = new ArrayList<String>();
		StringBuffer jsonIds = new StringBuffer();
		infosAttr.append("[");
		for (CiInformation info : list) {
			infosAttr.append("'");
			infosAttr.append(info.getCiHex());
			infosAttr.append("',");
			jsonIds.append(" jsonId = '");
			jsonIds.append(info.getCiHex());
			jsonIds.append("' or");
			// jsonIds.add(getJsonId(info));
		}
		infosAttr.delete(infosAttr.length() - 1, infosAttr.length());
		infosAttr.append("]");
		jsonIds.delete(jsonIds.length() - 2, jsonIds.length());
		StringBuffer del = new StringBuffer();
		del.append("with ");
		del.append(infosAttr);
		del.append(" as infos unwind infos as info ");
		del.append("match (n:Ci {`jsonId`:info})");
		del.append(" optional match (n)-[r]-() delete n,r");
		Neo4jDao.base(del.toString());

		String delSql = "delete from Ci where " + jsonIds.toString();
		MongoConnect.executeUpdate(delSql);
	}

	public int deleteCisByCiCate(String cateId) throws Exception {
		StringBuffer del = new StringBuffer("	match (CiCate:CiCategory {id:'");
		del.append(cateId);
		del.append(" '})<-[r:Ci2Cate]-(n:Ci) optional match (n)-[r2]-() delete r2,n return count(n)");

		long count = Neo4jDao.getDataLong(del.toString());
		String delSql = "delete from Ci where categoryId = '" + cateId + "'";
		MongoConnect.executeUpdate(delSql);
		return (int) count;
	}

	/**
	 * 返回这个分类下的全部ci的十六进制id
	 * 
	 * @param cateId
	 * @return
	 * @throws Exception
	 */
	public List<String> getHexIdsByCateId(String cateId) throws Exception {
		List<String> ret = new ArrayList<String>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		connection = MongoConnect.getConnection();
		stmt = connection
				.prepareStatement("select jsonId from Ci where categoryId = ?");
		stmt.setString(1, cateId);
		rs = stmt.executeQuery();
		while (rs.next()) {
			ret.add(rs.getString("jsonId"));
		}
		return ret;
	}

	public void deleteByJsonIds(List<String> jsonids) throws Exception {
		if (jsonids.size() == 0)
			return;
		StringBuffer infosAttr = new StringBuffer();
		// List<String> jsonIds = new ArrayList<String>();
		StringBuffer jsonIds = new StringBuffer();
		infosAttr.append("[");
		for (String jsonid : jsonids) {
			infosAttr.append("'");
			infosAttr.append(jsonid);
			infosAttr.append("',");
			jsonIds.append(" jsonId = '");
			jsonIds.append(jsonid);
			jsonIds.append("' or");
			// jsonIds.add(getJsonId(info));
		}
		infosAttr.delete(infosAttr.length() - 1, infosAttr.length());
		infosAttr.append("]");
		jsonIds.delete(jsonIds.length() - 2, jsonIds.length());
		StringBuffer del = new StringBuffer();
		del.append("with ");
		del.append(infosAttr);
		del.append(" as infos unwind infos as info ");
		del.append("match (n:Ci {`jsonId`:info})");
		del.append(" optional match (n)-[r]-() delete n,r");
		Neo4jDao.base(del.toString());

		String delSql = "delete from Ci where " + jsonIds.toString();
		MongoConnect.executeUpdate(delSql);
	}

	public List<CiInformation> getAll() throws Exception {
		List<CiInformation> ret = query("select * from Ci");
		return ret;
	}

	public CiInformation save(CiInformation info) throws Exception {
		ArrayList<CiInformation> infos = new ArrayList<CiInformation>();
		infos.add(info);
		insertInfo(infos);
		return info;
	}

	public CiInformation update(CiInformation info) throws Exception {
		ArrayList<CiInformation> infos = new ArrayList<CiInformation>();
		infos.add(info);
		updateInfo(infos);

		String match = "select * from Ci where jsonId = '" + info.getCiHex()
				+ "'";
		List<CiInformation> query = query(match);
		if (query.size() == 0) {
			throw new MException("配置项数据[" + info.getId() + "]不存在");
		}
		return query.get(0);
	}

	// ----------------------------------------------------------------------------
	// --------------------------------新增方法--------------------------------------------

	/**
	 * 获取分类下id的Info
	 * 
	 * @param scene
	 *            场景
	 * @param cateId
	 *            分类Id
	 * @param id
	 *            数据的id
	 * @return
	 * @throws Exception
	 */
	public CiInformation getInfoInCate(String cateId, String id)
			throws Exception {// 已改xj
		String match = "select * from Ci where jsonId = '"
				+ getJsonId(cateId, id) + "'";
		List<CiInformation> query = query(match);
		if (query == null || query.size() == 0)
			return null;
		return query.get(0);
	}

	/**
	 * 通过属性查找相应的对象
	 * 
	 * @param scene
	 *            scene=null,忽略场景
	 * @param key
	 *            ci的一个属性名称
	 * @param value
	 *            ci的属性值
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> getByProperty(String key, Object value)
			throws Exception {
		String sql = "select * from Ci where `" + key + "` = ?";
		ArrayList<Object> params = new ArrayList<Object>();
		params.add(value);
		return queryPrepare(sql, params);
	}

	public List<List<CiInformation>> saveOrUpdate(String cateId,
			List<CiInformation> infos) throws Exception {
		List<List<CiInformation>> ret = new ArrayList<List<CiInformation>>(
				infos.size());
		if (infos.size() > 0) {
			Set<String> allJsonIds = new HashSet<String>();// 存在的jsonId
			List<CiInformation> crt = new ArrayList<CiInformation>();
			List<CiInformation> upd = new ArrayList<CiInformation>();
			String match = "select jsonId from Ci where categoryId = '"
					+ cateId + "'";
			Connection conn = MongoConnect.getConnection();
			Statement statm = null;
			try {
				statm = conn.createStatement();
				ResultSet rs = statm.executeQuery(match);
				while (rs.next()) {
					String jsonId = rs.getString("jsonId");
					allJsonIds.add(jsonId);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (statm != null) {
					statm.close();
				}
			}
			for (CiInformation info : infos) {
				String jsonId = info.getCiHex();
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

	public List<CiInformation> getCis(List<CiInformation> infos)
			throws Exception {
		StringBuffer ids = new StringBuffer();
		for (CiInformation info : infos) {
			ids.append(" jsonId = '");
			ids.append(info.getCiHex());
			ids.append("' or");
		}
		ids.delete(ids.length() - 2, ids.length());
		String match = "select * from Ci where " + ids.toString();
		return query(match);
	}

	/**
	 * 只有同一个ciCategory下的info才能调用这个方法进行批量插入
	 * 
	 * @param ciInfos
	 * @throws Exception
	 */
	public void insertInfo(List<CiInformation> ciInfos) throws Exception {
		if (ciInfos != null && ciInfos.size() > 0) {
			CiInformation tmp = ciInfos.get(0);
			Map<String, String> tmpMap = asMap(tmp);
			List<String> sortKeys = new ArrayList<String>(tmpMap.keySet());

			StringBuffer sql = new StringBuffer();
			sql.append("insert into Ci(");
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
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql.toString());

			for (int i = 0; i < ciInfos.size(); i++) {
				Map<String, String> infoMap = asMap(ciInfos.get(i));
				for (int j = 0, len = sortKeys.size(); j < len; j++) {
					pst.setObject(j + 1, infoMap.get(sortKeys.get(j)));
				}
				pst.executeUpdate();
			}
			MongoConnect.colse(pst, null);
		}

	}

	/**
	 * 只有同一个ciCategory下的info才能调用这个方法进行批量更新
	 * 
	 * @param ciInfo
	 * @throws Exception
	 */
	public void updateInfo(List<CiInformation> ciInfos) throws Exception {
		if (ciInfos != null && ciInfos.size() > 0) {
			CiInformation tmp = ciInfos.get(0);
			Map<String, String> tmpMap = asMap(tmp);
			List<String> sortedKeys = new ArrayList<String>(tmpMap.keySet());
			StringBuffer sql = new StringBuffer();
			sql.append("update Ci set ");
			for (String key : sortedKeys) {
				sql.append("`");
				sql.append(key);
				sql.append("`= ?,");
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(" where jsonId = ?");

			Connection conn = MongoConnect.getConnection();
			PreparedStatement pst = conn.prepareStatement(sql.toString());

			for (int i = 0; i < ciInfos.size(); i++) {
				CiInformation ciInfo = ciInfos.get(i);
				Map<String, String> infoMap = asMap(ciInfo);
				for (int j = 0, len = sortedKeys.size(); j < len; j++) {
					pst.setObject(j + 1, infoMap.get(sortedKeys.get(j)));
				}
				pst.setString(sortedKeys.size() + 1, infoMap.get("jsonId"));
				pst.executeUpdate();
			}
		}
	}

	/**
	 * 通过Where条件SQL执行单纯的查询
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> queryByWhereParam(String whereParam)
			throws Exception {
		String sql = "select * from Ci where " + whereParam;
		return query(sql);
	}

	public List<CiInformation> query(DBObject query) throws Exception {
		DB db = MongoConnect.getDb();
		DBCollection ciColl = db.getCollection("Ci");
		DBCursor find = ciColl.find(query);
		List<CiInformation> ret = new ArrayList<CiInformation>(find.count());
		for (DBObject dbObject : find) {
			ret.add(dataToObject(dbObject));
		}
		return ret;
	}

	/**
	 * 执行单纯的查询
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> query(String sql) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement statm = null;
		try {
			statm = conn.createStatement();
			ResultSet executeQuery = statm.executeQuery(sql);
			return handlerResult(executeQuery);
		} catch (Exception e) {
			throw e;
		} finally {
			MongoConnect.colse(statm, null);
		}
	}

	/**
	 * 执行单纯的查询COUNT
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public Integer queryCount(String sql) throws Exception {
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

	public List<CiInformation> queryPrepare(String sql, List<Object> params)
			throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement statm = null;
		try {
			statm = conn.prepareStatement(sql);
			int i = 1;
			for (Object data : params) {
				statm.setObject(i, data);
			}
			ResultSet executeQuery = statm.executeQuery();
			return handlerResult(executeQuery);
		} catch (Exception e) {
			throw e;
		} finally {
			MongoConnect.colse(statm, null);
		}
	}

	/**
	 * 如果查询的CiInfo比较多时,可以先调用CiCateStorage的getAll
	 * 
	 * @param cypher
	 * @param ciCategoryCache
	 *            key是CiCate的id,val是CiCategory对象
	 * @return
	 */
	public List<CiInformation> handlerResult(ResultSet result) {
		long sTime = System.currentTimeMillis();
		List<CiInformation> ret = new ArrayList<CiInformation>();
		try {
			while (result.next()) {
				ResultSetMetaData metaData = result.getMetaData();
				int count = metaData.getColumnCount();
				Map<String, String> map = new HashMap<String, String>();
				for (int i = 1; i <= count; i++) {
					String label = metaData.getColumnLabel(i);
					String value = result.getString(label);
					map.put(label, value);
				}
				ret.add(mapToObject(map));
			}
			log.dLog("查询[" + ret.size() + "]个节点耗时 ："
					+ (System.currentTimeMillis() - sTime));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			MongoConnect.colse(null, result);
		}
		return ret;
	}

	public void addAttr(String cateId, List<Attribute> attrs) throws Exception {
		if (cateId != null) {
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("Ci");
			BasicDBObject update = new BasicDBObject();
			for (Attribute attribute : attrs) {
				update.append("data$" + attribute.getName(),
						attribute.getDefaultValue());
			}
			ciColl.updateMulti(new BasicDBObject("categoryId", cateId),
					new BasicDBObject("$set", update));
		}
	}

	public void deleteAttr(String cateId, List<Attribute> attrs)
			throws Exception {
		if (cateId != null) {
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("Ci");
			BasicDBObject update = new BasicDBObject();
			for (Attribute attribute : attrs) {
				update.append("data$" + attribute.getName(), true);
			}
			ciColl.updateMulti(new BasicDBObject("categoryId", cateId),
					new BasicDBObject("$unset", update));
		}
	}

	/**
	 * 
	 * @param ciCate
	 * @param data
	 *            key为老的名字,value为新的属性
	 * @throws Exception
	 */
	public void alterAttr(String cateId, Map<String, Attribute> data)
			throws Exception {
		if (cateId != null && data != null) {
			DB db = MongoConnect.getDb();
			DBCollection ciColl = db.getCollection("Ci");
			BasicDBObject update = new BasicDBObject();
			Set<String> keySet = data.keySet();
			for (String old : keySet) {
				String newName = "data$" + data.get(old).getName();
				if (!("data$" + old).equals(newName))
					update.append("data$" + old, newName);
			}
			ciColl.updateMulti(new BasicDBObject("categoryId", cateId),
					new BasicDBObject("$rename", update));
		}
	}

	public Map<String, Map<String, String>> getCiByMongoIds(List<String> ids)
			throws Exception {
		Map<String, Map<String, String>> ret = new HashMap<String, Map<String, String>>();
		Connection conn = MongoConnect.getConnection();
		Statement statm = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder("select * from Ci where ");
			for (String id : ids) {
				sql.append("_id='" + id + "' or ");
			}
			sql.delete(sql.length() - 4, sql.length());
			statm = conn.createStatement();
			rs = statm.executeQuery(sql.toString());
			if (rs != null) {
				while (rs.next()) {
					Map<String, String> ci = new HashMap<String, String>();
					String id = rs.getString("_id");
					ci.put("ciid", id);
					ci.put("ciName", rs.getString("id"));
					ci.put("ciCategoryId", rs.getString("categoryId"));
					ci.put("ciCateName", rs.getString("categoryName"));
					ci.put("hexId", rs.getString("jsonId"));
					ret.put(id, ci);
				}
			}
			return ret;
		} catch (Exception e) {
			return new HashMap<String, Map<String, String>>();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (statm != null) {
				statm.close();
			}
		}
	}

	public List<String> getMongoIdsForCiKpiRel(String cateName, String attr,
			String val, Map<String, CiCategory> ciCateMap) {
		List<String> ret = new ArrayList<String>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement statm = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder("select * from Ci ");
			List<String> valList = new ArrayList<String>();
			boolean where = false;
			if (cateName != null) {
				sql.append("where categoryName=? ");
				valList.add(cateName);
				where = true;
			}
			if (val != null) {
				if (attr == null) {
					Set<String> attrs = new HashSet<String>();
					if (cateName == null) {
						Set<String> ciCateSet = ciCateMap.keySet();
						for (String ciCateName : ciCateSet) {
							CiCategory ciCate = ciCateMap.get(ciCateName);
							List<String> ats = ciCate.getAttributeNames();
							for (String at : ats) {
								attrs.add(at);
							}
						}
					} else {
						CiCategory ciCate = ciCateMap.get(cateName);
						if (ciCate != null) {
							List<String> ats = ciCate.getAttributeNames();
							for (String at : ats) {
								attrs.add(at);
							}
						}
					}
					if (attrs.size() > 0) {
						if (where) {
							sql.append("and (");
						} else {
							sql.append("where (");
						}
						for (String at : attrs) {
							sql.append("`data$" + at + "`=? or ");
							valList.add(val);
						}
						sql.delete(sql.length() - 4, sql.length());
						sql.append(") ");
					}
				} else {
					if (where) {
						sql.append("and `data$" + attr + "`=? ");
					} else {
						sql.append("where `data$" + attr + "`=? ");
					}
					valList.add(val);
				}
			}
			statm = conn.prepareStatement(sql.toString());
			for (int i = 0; i < valList.size(); i++) {
				statm.setString(i + 1, valList.get(i));
			}
			rs = statm.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					ret.add(rs.getString("jsonId"));
				}
			}
			return ret;
		} catch (Exception e) {
			return new ArrayList<String>();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (statm != null) {
				try {
					statm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Map<String, String> asMap(CiInformation info) {
		Map<String, Object> data = info.getData();
		Map<String, String> dataMap = new HashMap<String, String>();
		Iterator<Map.Entry<String, Object>> iter = data.entrySet().iterator();

		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object val = entry.getValue();
			dataMap.put("data$" + key, val == null ? "" : val.toString());
		}

		Attribute attribute = info.getCategory().getClientId();
		if (attribute != null) {
			String name = attribute.getName();
			// 展示id的值
			dataMap.put("name", dataMap.get("data$" + name));
		} else {
			// 没有展示id时使用主键的值
			dataMap.put("name", info.getName());
		}
		// 主键的值
		dataMap.put("id", info.getName());
		dataMap.put("categoryId", info.getCategoryId());
		dataMap.put("source", info.getSource());
		dataMap.put("tag", info.getTag() == null ? "" : info.getTag());
		long createTime = 0;
		long updateTime = 0;
		try {
			createTime = TimeUtil.str2Time(info.getCreateTime(),
					TimeUtil.YMDHMS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			updateTime = TimeUtil.str2Time(info.getUpdateTime(),
					TimeUtil.YMDHMS);
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataMap.put("createTime", createTime + "");
		dataMap.put("updateTime", updateTime + "");
		net.sf.json.JSONObject record = net.sf.json.JSONObject.fromObject(info
				.getRecord());
		dataMap.put("record", record.toString());
		dataMap.put("jsonId", info.getCiHex());
		dataMap.put("categoryName", info.getCategory() == null ? "" : info
				.getCategory().getName());
		dataMap.put("owner", info.getOwner());
		return dataMap;
	}

	private String getJsonId(String categoryId, String infoId) {
		net.sf.json.JSONArray ja = new net.sf.json.JSONArray();
		ja.add(categoryId);
		ja.add(infoId);
		String jsonId = HexString.encode(ja.toString());
		return jsonId;
	}

	/**
	 * 將返回的node节点转化成对象 nData格式
	 * 
	 * @param datas
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private CiInformation mapToObject(Map<String, String> datas) {
		CiInformation info = new CiInformation();
		try {
			String id = datas.get("_id");
			String categoryId = datas.get("categoryId");
			String tag = "";
			try {
				tag = datas.get("tag");
			} catch (Exception e) {
			}

			String source = datas.get("source");
			String record = datas.get("record");
			String createTime = datas.get("createTime");
			String updateTime = datas.get("updateTime");
			String ciHex = datas.get("jsonId");
			String owner = datas.get("owner");
			long crT = Long.parseLong(createTime);
			long upT = Long.parseLong(updateTime);
			createTime = TimeUtil.convertTime(crT, TimeUtil.YMDHMS);
			updateTime = TimeUtil.convertTime(upT, TimeUtil.YMDHMS);

			Map<String, Object> data = new HashMap<String, Object>();

			for (Iterator keys = datas.keySet().iterator(); keys.hasNext();) {
				String key = keys.next().toString();
				if (key.startsWith("data$")) {
					Object value = datas.get(key);
					key = key.substring(5);
					data.put(key, value);
				}
			}

			info.setCategoryId(categoryId);
			info.setCreateTime(createTime);
			info.setUpdateTime(updateTime);
			info.setData(data);
			info.setId(id);
			info.setRecord(net.sf.json.JSONObject.fromObject(record));
			info.setSource(source);
			info.setTag(tag);
			info.setCiHex(ciHex);
			info.setOwner(owner);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CiInformation dataToObject(DBObject datas) {
		CiInformation info = new CiInformation();
		try {
			String id = ((ObjectId) datas.get("_id")).toHexString();
			String categoryId = (String) datas.get("categoryId");
			String tag = "";
			try {
				tag = (String) datas.get("tag");
			} catch (Exception e) {
			}

			String source = (String) datas.get("source");
			String record = (String) datas.get("record");
			String createTime = (String) datas.get("createTime");
			String updateTime = (String) datas.get("updateTime");
			String ciHex = (String) datas.get("jsonId");
			String owner = datas.get("owner") == null ? "" : datas.get("owner")
					.toString();
			long crT = Long.parseLong(createTime);
			long upT = Long.parseLong(updateTime);
			createTime = TimeUtil.convertTime(crT, TimeUtil.YMDHMS);
			updateTime = TimeUtil.convertTime(upT, TimeUtil.YMDHMS);

			Map<String, Object> data = new HashMap<String, Object>();

			for (Iterator keys = datas.keySet().iterator(); keys.hasNext();) {
				String key = keys.next().toString();
				if (key.startsWith("data$")) {
					Object value = datas.get(key);
					key = key.substring(5);
					data.put(key, value);
				}
			}

			// info.setNeo4jid(neo4jid);
			info.setCategoryId(categoryId);
			info.setCreateTime(createTime);
			info.setUpdateTime(updateTime);
			info.setData(data);
			info.setId(id);
			info.setRecord(net.sf.json.JSONObject.fromObject(record));
			info.setSource(source);
			info.setTag(tag);
			info.setCiHex(ciHex);
			info.setOwner(owner);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;
	}
}