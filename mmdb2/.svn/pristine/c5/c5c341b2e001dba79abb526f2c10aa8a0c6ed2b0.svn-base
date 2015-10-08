package com.mmdb.model.info.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.model.categroy.storage.ViewCateStorage;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;

/**
 * 视图 - 存储仓库
 * 
 * @author XIE
 */
@Component("viewInfoStorage")
public class ViewInfoStorage {
	private Log log = LogFactory.getLogger("ViewInfoStorage");

	@Autowired
	private ViewCateStorage viewCateStorage;

	/**
	 * 获取总数
	 * 
	 * @return
	 * @throws Exception
	 */
	public int count() throws Exception {
		int count = 0;
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select * from Views";
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					count++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
			if (rs != null) {
				rs.close();
			}
			rs = null;
		}
		return count;
	}

	/**
	 * 批量删除
	 * 
	 * @param list
	 * @throws Exception
	 */
	public void delete(List<ViewInformation> list) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from Views where _id=?";
			for (ViewInformation view : list) {
				pst = conn.prepareStatement(sql);
				pst.setString(1, view.getId());
				pst.executeUpdate();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
		}
	}

	/**
	 * 单个删除
	 * 
	 * @param t
	 * @throws Exception
	 */
	public void delete(ViewInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from Views where _id=?";
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
	 * 清空
	 * 
	 * @throws Exception
	 */
	public void deleteAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		try {
			String sql = "delete from Views";
			st = conn.createStatement();
			st.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

 

	public List<ViewInformation> getByProperty(String key, Object value,
			List<ViewCategory> all) throws Exception {
		String sql = "select * from Views where userName=? and `status`='"
				+ ViewInformation.NORMAL + "' and " + key + "=?";
		List<Object> params = new ArrayList<Object>();
		params.add(value);
		return getViewsBySql(sql, params, all);
	}

	/**
	 * 根据条件查询
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> query(String query, List<ViewCategory> all)
			throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			Map<String, ViewCategory> allMap = new HashMap<String, ViewCategory>();
			if (all == null) {
				all = viewCateStorage.getAll();
				for (ViewCategory cate : all) {
					allMap.put(cate.getId(), cate);
				}
			}
			List<ViewInformation> result = new ArrayList<ViewInformation>();
			st = conn.createStatement();
			rs = st.executeQuery(query);
			if (rs != null) {
				while (rs.next()) {
					ViewInformation view = new ViewInformation();
					view.setId(rs.getString("_id"));
					view.setName(rs.getString("name"));
					view.setDescription(rs.getString("description"));
					view.setXml(rs.getString("view_xml"));
					view.setSvg(rs.getString("svg"));
					view.setPoints(rs.getString("points"));
					view.setImageUrl(rs.getString("imageUrl"));
					view.setOpen(rs.getBoolean("isOpen"));
					view.setCreateTime(rs.getLong("createTime"));
					view.setUpdateTime(rs.getLong("updateTime"));
					view.setUserName(rs.getString("userName"));
					view.setCategoryId(rs.getString("categoryId"));
					view.setCategory(allMap.get(view.getCategoryId()));
					view.setVersion(rs.getInt("version"));
					view.setStatus(rs.getString("status"));
					String subscripers = rs.getString("subscripers");
					if (subscripers != null && subscripers.length() > 3) {
						view.setSubscripers(JSONArray.fromObject(subscripers));
					}
					result.add(view);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
			if (rs != null) {
				rs.close();
			}
			rs = null;
		}
	}

	/**
	 * 根据条件查询
	 * 
	 * @param query
	 *            查询条件
	 * @param vcM
	 *            视图分类list 转的Map
	 * @return
	 * @throws Exception
	 */
	/*
	 * public List<ViewInformation> query(String query,Map<String,ViewCategory>
	 * vcM) throws Exception { List<ViewInformation> vL = new
	 * ArrayList<ViewInformation>(); if(query == null || "".equals(query)){
	 * return null; } List<JSONObject> dL = Neo4jDao.getDataMap(query);
	 * if(dL.size() > 0){ // List<ViewCategory> vcL =
	 * viewCateStorage.getAll();//取出所所有的分类
	 * 
	 * // Map<String,ViewCategory> vcM = new HashMap<String,ViewCategory>(); //
	 * for(ViewCategory vc:vcL){//找到对应的视图分类 ，加到info里面 // vcM.put(vc.getId(),
	 * vc); // }
	 * 
	 * for(JSONObject m : dL){ ViewInformation vc = new ViewInformation();
	 * if(m.length() != 0){ vc = this.columeAttr(vc, m); }
	 * vc.setCategory(vcM.get(vc.getCategoryId())); vL.add(vc); } } return vL; }
	 */

	// @Override
	// public List<ViewInformation> query(String exp) throws Exception {
	// // TODO Auto-generated method stub
	// return super.query(exp);
	// }

	// @Override
	// public List<ViewInformation> queryByProperty(String key, Object value)
	// throws Exception {
	// // TODO Auto-generated method stub
	// return super.queryByProperty(key, value);
	// }

	/**
	 * 保存节点
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public ViewInformation save(ViewInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		String sql = null;
		try {
			if (t.getId() == null || "".equals(t.getId())) {
				sql = "insert into Views(categoryId,name,description,view_xml,svg,imageUrl,userName,isOpen,version,createTime,updateTime,subscripers,points,status) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			} else {
				sql = "insert into Views(categoryId,name,description,view_xml,svg,imageUrl,userName,isOpen,version,createTime,updateTime,subscripers,points,status,_id) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			}
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getCategoryId());
			pst.setString(2, t.getName());
			pst.setString(3, t.getDescription());
			pst.setString(4, t.getXml());
			pst.setString(5, t.getSvg());
			pst.setString(6, t.getImageUrl());
			pst.setString(7, t.getUserName());
			pst.setObject(8, t.getOpen());
			pst.setInt(9, t.getVersion());
			pst.setLong(10, t.getCreateTime());
			pst.setLong(11, t.getUpdateTime());
			pst.setString(12, t.getSubscripers() == null ? "" : JSONArray
					.fromObject(t.getSubscripers()).toString());
			pst.setString(13, t.getPoints());
			pst.setString(14, t.getStatus());
			if (t.getId() != null && !"".equals(t.getId())) {
				pst.setString(14, t.getId());
			}
			pst.executeUpdate();
			return getByName(t.getCategoryId(), t.getName());
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

	public ViewInformation update(ViewInformation t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update Views set _id=?,categoryId=?,name=?,"
					+ "description=?,view_xml=?,svg=?,imageUrl=?,userName=?,isOpen=?,version=?,createTime=?,updateTime=?,subscripers=?,points=?,status=? where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(16, t.getId());
			pst.setString(1, t.getId());
			pst.setString(2, t.getCategoryId());
			pst.setString(3, t.getName());
			pst.setString(4, t.getDescription());
			pst.setString(5, t.getXml());
			pst.setString(6, t.getSvg());
			pst.setString(7, t.getImageUrl());
			pst.setString(8, t.getUserName());
			pst.setObject(9, t.getOpen());
			pst.setInt(10, t.getVersion());
			pst.setLong(11, t.getCreateTime());
			pst.setLong(12, t.getUpdateTime());
			pst.setString(13, t.getSubscripers() == null ? "" : JSONArray
					.fromObject(t.getSubscripers()).toString());
			pst.setString(14, t.getPoints());
			pst.setString(15, t.getStatus());
			pst.executeUpdate();
			return t;
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
	 * [批量]获取指定CATE和InfoId的数据
	 * 
	 * @param category
	 *            分类
	 * @param InfoIds
	 *            数据InfoIds
	 * @return
	 * @throws Exception
	 */
	public Map<String, ViewInformation> getByCateInfoIds(ViewCategory category,
			List<String> InfoIds) throws Exception {
		Map<String, ViewInformation> ret = new HashMap<String, ViewInformation>();
		List<ViewCategory> all = new ArrayList<ViewCategory>();
		all.add(category);
		List<ViewInformation> list = this.getByProperty("categoryId",
				category.getId(), all);
		for (ViewInformation info : list) {
			if (InfoIds.contains(info.getId())) {
				ret.put(info.getId(), info);
			}
		}
		return ret;
	}

	/**
	 * [批量]删除数据
	 * 
	 * @param list
	 *            数据
	 * @return
	 * @throws Exception
	 */
	public void deletes(List<ViewInformation> list) throws Exception {
		for (ViewInformation info : list) {
			this.delete(info);
		}
	}

	public List<ViewInformation> getViewsBySql(String sql, List<Object> params,
			List<ViewCategory> all) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			Map<String, ViewCategory> allMap = new HashMap<String, ViewCategory>();
			if (all == null) {
				all = viewCateStorage.getAll();
			}
			for (ViewCategory cate : all) {
				allMap.put(cate.getId(), cate);
			}
			List<ViewInformation> result = new ArrayList<ViewInformation>();
			pst = conn.prepareStatement(sql);
			int i = 1;
			if (params != null)
				for (Object param : params) {
					pst.setObject(i, param);
					i++;
				}
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					ViewInformation view = new ViewInformation();
					view.setId(rs.getString("_id"));
					view.setName(rs.getString("name"));
					view.setDescription(rs.getString("description"));
					view.setXml(rs.getString("view_xml"));
					view.setSvg(rs.getString("svg"));
					view.setPoints(rs.getString("points"));
					view.setImageUrl(rs.getString("imageUrl"));
					view.setOpen(rs.getBoolean("isOpen"));
					view.setCreateTime(rs.getLong("createTime"));
					view.setUpdateTime(rs.getLong("updateTime"));
					view.setUserName(rs.getString("userName"));
					view.setCategoryId(rs.getString("categoryId"));
					view.setCategory(allMap.get(view.getCategoryId()));
					view.setVersion(rs.getInt("version"));
					view.setStatus(rs.getString("status"));
					String subscripers = rs.getString("subscripers");
					if (subscripers != null && subscripers.length() > 3) {
						view.setSubscripers(JSONArray.fromObject(subscripers));
					}
					result.add(view);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pst != null) {
				pst.close();
			}
			pst = null;
			if (rs != null) {
				rs.close();
			}
			rs = null;
		}
	}

	public boolean exist(String cateId, String name) throws Exception {
		if (getByName(cateId, name) != null)
			return true;
		return false;
	}

	public ViewInformation getByName(String cateId, String name)
			throws Exception {
		String sql = "select * from Views where categoryId=? and name = ? and `status` ='"
				+ ViewInformation.NORMAL + "'";
		List<Object> params = new ArrayList<Object>();
		params.add(cateId);
		params.add(name);
		List<ViewInformation> viewsBySql = getViewsBySql(sql, params, null);
		if (viewsBySql.size() == 1) {
			return viewsBySql.get(0);
		}
		return null;
	}

	public List<String> getViewIdsByCategoryId(String cateId) throws Exception {
		String sql = "select _id from Views where categoryId = ? and `status` = 'NORMAL'";
		Connection connection = MongoConnect.getConnection();
		List<String> ret = new ArrayList<String>();
		PreparedStatement ptmt = null;
		try {
			ptmt = connection.prepareStatement(sql);
			ptmt.setObject(1, cateId);
			ResultSet rs = ptmt.executeQuery();

			while (rs.next()) {
				ret.add(rs.getString("_id"));
			}
		} catch (Exception e) {
			throw e;
		}
		return ret;
	}

	public ViewInformation getById(String id) throws Exception {
		String sql = "select * from Views where _id= '" + id + "'";
		List<ViewInformation> viewsBySql = getViewsBySql(sql, null, null);
		if (viewsBySql.size() == 1) {
			return viewsBySql.get(0);
		}
		return null;
	}

	public List<ViewInformation> getAll() throws Exception {
		String sql = "select * from Views";
		return getViewsBySql(sql, null, null);
	}

	/**
	 * 获取用户的全部视图,但不包含软删除状态的
	 * 
	 * @param loginName
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> getAllPrivateViewByUser(String loginName)
			throws Exception {
		String sql = "select * from Views where userName = ? and `status` = 'NORMAL'";
		List<Object> params = new ArrayList<Object>();
		params.add(loginName);
		return getViewsBySql(sql, params, null);
	}

	public List<ViewInformation> getAllOpenInfo() throws Exception {
		String sql = "select * from Views where isOpen=? and `status` = 'NORMAL'";
		List<Object> params = new ArrayList<Object>();
		params.add(true);
		return getViewsBySql(sql, params, null);
	}

	public void deleteAllOpenView() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from Views where isOpen=?";
			st = conn.prepareStatement(sql);
			st.setObject(1, true);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

	public void deleteAllPrivateViewByUser(String userName) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from Views where userName=?";
			st = conn.prepareStatement(sql);
			st.setObject(1, userName);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

	public void deleteById(String id) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from Views where _id='" + id + "'";
			st = conn.prepareStatement(sql);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

	public void deleteByViewCategory(List<ViewCategory> vCates)
			throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from Views where categoryId=?";
			st = conn.prepareStatement(sql);
			for (ViewCategory viewCategory : vCates) {
				st.setObject(1, viewCategory.getId());
				st.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

	public List<ViewInformation> getOpenViewByUser(String userName)
			throws Exception {
		String sql = "select * from Views where isOpen = ? and userName = ? and `status` = 'NORMAL'";
		List<Object> q = new ArrayList<Object>();
		q.add(true);
		q.add(userName);
		return getViewsBySql(sql, q, null);
	}

	public List<String> getAllOpenViewAuthor() throws Exception {
		Set<String> names = new HashSet<String>();
		String sql = "select `userName` from Views where `isOpen` = ? and `status` = 'NORMAL'";
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(sql);
			st.setObject(1, true);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("userName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
		return new ArrayList<String>(names);
	}

	public int getCount(String sql) {
		Connection conn = MongoConnect.getConnection();
		MongoStatement stmt = null;
		int count = 0;
		try {
			stmt = (MongoStatement) conn.createStatement();
			count = stmt.executeQueryCount(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * 获取分类下的视图数量
	 * 
	 * @param cateId
	 * @return
	 */
	public int getCountByCategory(String cateId) {
		String count = "select * from Views where categoryId = '" + cateId
				+ "' and `status` = 'NORMAL'";
		return getCount(count);
	}

	/**
	 * 软删除一个视图
	 * 
	 * @param id
	 * @throws Exception
	 */
	public void softDelete(String id) throws Exception {

		String upd = "update Views set status = '"
				+ ViewInformation.SOFT_DELETE + "' where _id = '" + id + "'";
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(upd);
			st.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
	}

	/**
	 * 获取一个用户被软删除的视图
	 * 
	 * @param userName
	 *            用户的名称,null时获取全部.
	 * @param isOpen
	 *            null时为获取全部,true时只获取共有的,false时只获取私有的
	 * @return
	 * @throws Exception
	 */
	public List<ViewInformation> getSoftDeleteViewByUser(String userName,
			Boolean isOpen) throws Exception {
		StringBuffer match = new StringBuffer(
				"select * from Views where `status`= '"
						+ ViewInformation.SOFT_DELETE + "'");
		if (userName != null && !"".equals(userName)) {
			match.append(" and `userName` = '").append(userName).append("' ");
		}
		if (isOpen != null) {
			match.append("and `isOpen` = ").append(isOpen);
		}
		return query(match.toString(), null);
	}

	public List<ViewInformation> getSoftDeleteViewByUser(String userName,
			int start, int limit) throws Exception {
		StringBuffer match = new StringBuffer(
				"select * from Views where `status`= '"
						+ ViewInformation.SOFT_DELETE + "'");
		match.append(" and `userName` = '").append(userName)
				.append("' order by updateTime desc limit ").append(start)
				.append(",").append(limit);

		return query(match.toString(), null);
	}

	public int getSoftDeleteViewCountByUser(String userName) {
		StringBuffer match = new StringBuffer(
				"select * from Views where `status`= '"
						+ ViewInformation.SOFT_DELETE + "'");
		match.append(" and `userName` = '").append(userName).append("'");

		return getCount(match.toString());
	}
}
