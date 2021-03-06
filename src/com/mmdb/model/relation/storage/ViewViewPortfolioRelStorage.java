package com.mmdb.model.relation.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mmdb.mongo.MongoConnect;

/**
 * 视图与组合视图之间的关系
 * 
 * @author xiongjian
 * 
 */
@Component()
public class ViewViewPortfolioRelStorage {
	// private Log log = LogFactory.getLogger("ViewViewPortfolioRelStorage");

	public Map<String, List<String>> getAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			sql = "select * from ViewRelToProtfolio";
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String viewId = rs.getString(1);
					String hexId = rs.getString(2);
					List<String> hexIds = result.get(viewId);
					if (hexIds == null) {
						hexIds = new ArrayList<String>();
						result.put(viewId, hexIds);
					}
					hexIds.add(hexId);
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
			if (rs != null) {
				rs.close();
			}
		}
	}

	public List<String> getByView(String viewId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			List<String> result = new ArrayList<String>();
			sql = "select * from ViewRelToProtfolio where viewId = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, viewId);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					result.add(rs.getString("protfolioId"));
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
			if (rs != null) {
				rs.close();
			}
		}
	}

	public List<String> getByProtfolio(String protfolioId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			List<String> result = new ArrayList<String>();
			sql = "select * from ViewRelToProtfolio where protfolioId = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, protfolioId);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					result.add(rs.getString("viewId"));
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
			if (rs != null) {
				rs.close();
			}
		}
	}

	public void save(String protfolioId, List<String> viewIds) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "insert into ViewRelToProtfolio(protfolioId,viewId) values (?,?) ";
			st = conn.prepareStatement(sql);
			for (String viewId : viewIds) {
				st.setString(1, protfolioId);
				st.setString(2, viewId);
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

	public void update(String protfolioId, List<String> viewIds)
			throws Exception {
		deleteByProtfolio(protfolioId);
		save(protfolioId, viewIds);
	}

	/**
	 * 通过组合视图删除相关的视图关系
	 * 
	 * @param viewId
	 * @throws Exception
	 */
	public void deleteByProtfolio(String protfolioId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from ViewRelToProtfolio where protfolioId = ? ";
			st = conn.prepareStatement(sql);
			st.setString(1, protfolioId);
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
	 * 删除一个视图时调用???
	 * 
	 * @param hexId
	 * @throws Exception
	 */
	public void deleteByView(String viewId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from ViewRelToProtfolio where viewId = ? ";
			st = conn.prepareStatement(sql);
			st.setString(1, viewId);
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

	public void deleteAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from ViewRelToProtfolio ";
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
}
