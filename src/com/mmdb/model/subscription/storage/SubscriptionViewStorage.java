package com.mmdb.model.subscription.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.springframework.stereotype.Repository;

import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;

@Repository("subscriptionViewStorage")
public class SubscriptionViewStorage {
	/**
	 * 保存阈值,
	 * 
	 * @param datas
	 * @return 返回的数据带有id(mongoid)
	 * @throws Exception
	 */
	public List<Map<String, Object>> save(List<Map<String, Object>> datas)
			throws Exception {
		String inser = "insert into SubscriptionKpiViewRel(`kpiId`,`ciId`,`subscriber`,`viewId`,"
				+ "`viewAuthor`,`threshold`,`ifNotify`,`ifEnable`)"
				+ " values(?,?,?,?,?,?,?,?)";
		String match = "select * from SubscriptionKpiViewRel where `viewId` = ? and `ciId` = ? and `kpiId` = ? ";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(inser);
			for (Map<String, Object> data : datas) {
				Object kpiId = data.get("kpiId");
				Object ciId = data.get("ciId");
				Object subscriber = data.get("subscriber").toString();
				Object viewId = data.get("viewId");
				Object viewAuthor = data.get("viewAuthor");
				String threshold = data.get("threshold").toString();
				Object ifNotify = data.get("ifNotify").toString();
				Object ifEnable = data.get("ifEnable").toString();

				stmt.setObject(1, kpiId);
				stmt.setObject(2, ciId);
				stmt.setObject(3, subscriber);
				stmt.setObject(4, viewId);
				stmt.setObject(5, viewAuthor);
				stmt.setObject(6, threshold);
				stmt.setObject(7, ifNotify);
				stmt.setObject(8, ifEnable);
				stmt.executeUpdate();
			}
			stmt = null;
			stmt = connection.prepareStatement(match);
			for (Map<String, Object> map : datas) {
				stmt.setObject(1, map.get("viewId"));
				stmt.setObject(2, map.get("ciId"));
				stmt.setObject(3, map.get("kpiId"));
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					map.put("id", rs.getObject("_id"));
				}
				rs.close();
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return datas;
	}

	public void update(List<Map<String, Object>> datas) throws Exception {
		String update = "update SubscriptionKpiViewRel set `kpiId` = ? ,`ciId` = ?,`subscriber` = ?,"
				+ "`viewId` = ?,`viewAuthor` = ?,`threshold` = ?,`ifNotify` = ?,`ifEnable` = ? where _id = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(update);
			for (Map<String, Object> data : datas) {
				Object id = data.get("id");
				Object kpiId = data.get("kpiId");
				Object ciId = data.get("ciId");
				Object subscriber = data.get("subscriber").toString();
				Object viewId = data.get("viewId");
				Object viewAuthor = data.get("viewAuthor");
				Object threshold = data.get("threshold").toString();
				Object ifNotify = data.get("ifNotify").toString();
				Object ifEnable = data.get("ifEnable").toString();

				stmt.setObject(1, kpiId);
				stmt.setObject(2, ciId);
				stmt.setObject(3, subscriber);
				stmt.setObject(4, viewId);
				stmt.setObject(5, viewAuthor);
				stmt.setObject(6, threshold);
				stmt.setObject(7, ifNotify);
				stmt.setObject(8, ifEnable);
				stmt.setObject(9, id);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * 找到一张视图的默认阈值
	 * 
	 * @param viewId
	 */
	public List<Map<String, Object>> getSubscriptionByViewIdAndViewAuthor(
			String viewId) {
		String match = "select userName from Views where _id = ?";
		List<Map<String, Object>> retData = null;
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		String author = null;
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				author = rs.getString("userName");
			}
			rs.close();
			stmt.close();
			if (author == null) {
				return null;
			}
			retData = new ArrayList<Map<String, Object>>();
			match = "select * from SubscriptionKpiViewRel where `viewId` = ? and `subscriber` like ?";
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			stmt.setString(2, author);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				retData.add(data);
			}
		} catch (SQLException e) {
		} finally {
			// if (stmt != null) {
			// stmt.close();
			// }
		}
		return retData;
	}

	/**
	 * 通过视图id,ciHexId,kpiHexId取出记录(唯一的),用于融合(追加订阅者名字)
	 * 
	 * @param viewId
	 *            视图的mongoid
	 * @param ciHex
	 *            ci的
	 * @param kpiHex
	 *            kpi的
	 * @param threshold
	 *            阈值
	 * @return
	 */
	public Map<String, Object> getdata(String viewId, String ciHex,
			String kpiHex, String threshold) {
		String match = "select * from SubscriptionKpiViewRel where `viewId` = ? and `ciId` = ? and `kpiId` = ? and `threshold` = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			stmt.setString(2, ciHex);
			stmt.setString(3, kpiHex);
			stmt.setString(4, threshold);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				return data;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
		}
		return null;
	}

	public void deleteByViewId(String viewId) {
		deleteByPropertis("viewId", viewId);
	}

	public List<Map<String, Object>> getByViewId(String viewId) {
		return getByPropertis("viewId", viewId);
	}

	public List<Map<String, Object>> getByCi(String ciId) {
		return getByPropertis("ciId", ciId);
	}

	public List<Map<String, Object>> getByKpi(String kpiId) {
		return getByPropertis("kpiId", kpiId);
	}

	public List<Map<String, Object>> getByCiKpiRel(String ciId, String kpiId) {
		String match = "select * from SubscriptionKpiViewRel where `ciId` = ? and `kpiId` = ?";
		Connection connection = MongoConnect.getConnection();
		List<Map<String, Object>> retData = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, ciId);
			stmt.setString(2, kpiId);
			rs = stmt.executeQuery();
			retData = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				retData.add(data);
			}
		} catch (SQLException e) {
		} finally {
		}
		return retData;
	}

	public void deleteByIds(List<String> delete) {
		if (delete == null || delete.size() == 0)
			return;
		String del = "delete from SubscriptionKpiViewRel where  `_id` = ? ";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del.toString());
			for (String id : delete) {
				stmt.setString(1, id);
				stmt.executeUpdate();
			}
		} catch (SQLException e) {
		} finally {
		}

	}

	public void deleteByKpiId(String kpiId) {
		deleteByPropertis("kpiId", kpiId);
	}

	public void deleteByCiId(String ciId) {
		deleteByPropertis("ciId", ciId);
	}

	public void deleteByCiKpiRel(String ciId, String kpiId) {
		String del = "delete from SubscriptionKpiViewRel where `ciId` = ? and `kpiId`=?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, ciId);
			stmt.setObject(2, kpiId);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	private void deleteByPropertis(String key, String val) {
		String del = "delete from SubscriptionKpiViewRel where `" + key
				+ "` = ? ";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, val);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	public Map<String, Object> getById(String id) {
		List<Map<String, Object>> ret = getByPropertis("_id", id);
		if (ret == null || ret.size() == 0)
			return null;
		return ret.get(0);
	}

	private List<Map<String, Object>> getByPropertis(String key, String val) {
		String match = "select * from SubscriptionKpiViewRel where `" + key
				+ "` = ?";
		Connection connection = MongoConnect.getConnection();
		List<Map<String, Object>> retData = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(match);
			// stmt.setString(1, key);
			stmt.setString(1, val);
			rs = stmt.executeQuery();
			retData = new ArrayList<Map<String, Object>>();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				retData.add(data);
			}
		} catch (SQLException e) {
		} finally {
		}
		return retData;
	}

	/**
	 * 通过订阅获取视图一个阈值信息
	 * 
	 * @param viewId
	 *            视图id
	 * @param subscriber
	 *            订阅人
	 * @return
	 */
	public List<Map<String, Object>> getDataBySubUserAndView(String viewId,
			String subscriber) {
		String match = "select * from SubscriptionKpiViewRel where `viewId` = ? and `subscriber` like ?";
		Connection connection = MongoConnect.getConnection();
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			stmt.setString(2, subscriber);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				retData.add(data);
			}
		} catch (SQLException e) {
		} finally {
		}
		List<Map<String, Object>> filter = new ArrayList<Map<String, Object>>();
		// 过滤可能多余的数据,取阈值时订阅人名字出现 ['-aa-','aa'] 的情况
		for (Map<String, Object> map : retData) {
			Object object = map.get("subscriber");
			JSONArray suber = JSONArray.fromObject(object);
			if (suber.contains(subscriber)) {
				filter.add(map);
			}
		}
		return filter;
	}

	// 订阅视图
	public void subscriptionView(String viewId, String subscriber) {
		String save = "insert into SubscriberViewRel(viewId,subscriber) values(?,?)";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(save);
			stmt.setString(1, viewId);
			stmt.setString(2, subscriber);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	// 取消订阅
	public void unSubscriptionView(String viewId, String subscriber) {
		String del = "delete from SubscriberViewRel where viewId = ? and subscriber = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setString(1, viewId);
			stmt.setString(2, subscriber);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	/**
	 * 视图被删除了,删除全部用户与视图的关系.
	 * 
	 * @param viewId
	 */
	public void unSubscriptionByView(String viewId) {
		String del = "delete from SubscriberViewRel where viewId = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setString(1, viewId);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	/**
	 * 用户被注销了
	 * 
	 * @param subscriber
	 */
	public void unSubscriptionByUser(String subscriber) {
		String del = "delete from SubscriberViewRel where subscriber = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setString(1, subscriber);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}

	/**
	 * 这个视图上的订阅人
	 * 
	 * @param viewId
	 */
	public List<String> getSubscriberByViewId(String viewId) {
		String match = "select * from SubscriberViewRel where viewId = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> subs = new ArrayList<String>();
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			rs = stmt.executeQuery();
			while (rs.next()) {
				subs.add(rs.getString("subscriber"));
			}
		} catch (SQLException e) {
		} finally {
		}
		return subs;
	}

	/**
	 * 这个用户订阅的视图
	 * 
	 * @param subscriber
	 */
	public List<String> getViewBySubscriber(String subscriber) {
		String match = "select * from SubscriberViewRel where subscriber = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<String> viewIds = new ArrayList<String>();
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, subscriber);
			rs = stmt.executeQuery();
			while (rs.next()) {
				viewIds.add(rs.getString("viewId"));
			}
		} catch (SQLException e) {
		} finally {
		}
		return viewIds;
	}

	public boolean exist(String ciId, String kpiId) {
		String match = "select * from SubscriptionKpiViewRel where ciId = '"
				+ ciId + "' and kpiId = '" + kpiId + "'";
		MongoStatement stmt = null;
		int count = 0;
		try {
			Connection conn = MongoConnect.getConnection();
			stmt = (MongoStatement) conn.createStatement();
			count = stmt.executeQueryCount(match);
			if (count > 0)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return false;
	}

	public List<Map<String, Object>> getByViewIdAndCi(String viewId,
			String ciHex) {
		String match = "select * from SubscriptionKpiViewRel where `viewId` = ? and `ciId` like ?";
		Connection connection = MongoConnect.getConnection();
		List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement(match);
			stmt.setString(1, viewId);
			stmt.setString(2, ciHex);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("id", rs.getObject("_id"));
				data.put("kpiId", rs.getObject("kpiId"));
				data.put("ciId", rs.getObject("ciId"));
				data.put("viewId", rs.getObject("viewId"));
				data.put("viewAuthor", rs.getObject("viewAuthor"));
				data.put("subscriber", rs.getObject("subscriber"));
				data.put("threshold", rs.getObject("threshold"));
				data.put("ifNotify", rs.getObject("ifNotify"));
				data.put("ifEnable", rs.getObject("ifEnable"));
				retData.add(data);
			}
		} catch (SQLException e) {
		} finally {
		}
		return retData;
	}

	public void deleteByViewAndCi(String viewId, String ciId) {
		String del = "delete from SubscriptionKpiViewRel where `viewId` = ? and `ciId`=?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, viewId);
			stmt.setObject(2, ciId);
			stmt.executeUpdate();
		} catch (SQLException e) {
		} finally {
		}
	}
}
