package com.mmdb.model.relation.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.mongo.MongoConnect;

@Component("ciViewRelStorage")
public class CiViewRelStorage {
	private Log log = LogFactory.getLogger("CiViewRelStorage");

	public Map<String, List<String>> getAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			Map<String, List<String>> result = new HashMap<String, List<String>>();
			sql = "select * from CiViewRel";
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

	public List<String> getByCi(String hexId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			List<String> result = new ArrayList<String>();
			sql = "select * from CiViewRel where ciHexId = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, hexId);
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
	
	public List<String> getByView(String viewId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		String sql = null;
		try {
			List<String> result = new ArrayList<String>();
			sql = "select * from CiViewRel where viewId = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, viewId);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					result.add(rs.getString("ciHexId"));
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

	public void save(String viewId, List<String> ciHexIds) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "insert into CiViewRel(viewId,ciHexId) values (?,?) ";
			st = conn.prepareStatement(sql);
			for (String hexId : ciHexIds) {
				st.setString(1, viewId);
				st.setString(2, hexId);
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

	public void update(String viewId, List<String> ciHexIds) throws Exception {
		deleteByView(viewId);
		save(viewId, ciHexIds);
	}

	/**
	 * 通过视图删除与ci的关系
	 * 
	 * @param viewId
	 * @throws Exception
	 */
	public void deleteByView(String viewId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from CiViewRel where viewId = ? ";
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

	/**
	 * 删除一个CI时调用???
	 * 
	 * @param hexId
	 * @throws Exception
	 */
	public void deleteByCi(String hexId) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from CiViewRel where ciHexId = ? ";
			st = conn.prepareStatement(sql);
			st.setString(1, hexId);
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
			String sql = "delete from CiViewRel ";
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
