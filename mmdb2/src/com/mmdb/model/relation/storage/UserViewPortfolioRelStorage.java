package com.mmdb.model.relation.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

import com.mmdb.mongo.MongoConnect;

/**
 * 用户和组合使用的关系.
 * 
 * @author xiongjian
 * 
 */
@Repository
public class UserViewPortfolioRelStorage {

	public void save(String viewPId, String loginName) throws Exception {
		String inser = "insert into SubscriberViewPortfolioRel(viewPortfolio,subscriber) values (?,?)";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(inser);

			stmt.setObject(1, viewPId);
			stmt.setObject(2, loginName);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public void del(String viewPId, String loginName) throws Exception {
		String del = "delete from SubscriberViewPortfolioRel where viewPortfolio = ? and subscriber = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, viewPId);
			stmt.setObject(2, loginName);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	public List<String> getSubscribersByViewPortfolioId(String viewPId)
			throws Exception {
		List<String> ret = new ArrayList<String>();
		String del = "select * from SubscriberViewPortfolioRel where viewPortfolio = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, viewPId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String subscriber = rs.getString("subscriber");
				ret.add(subscriber);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return ret;
	}

	public List<String> getViewPortfoliosBySubscriber(String sub)
			throws Exception {
		List<String> ret = new ArrayList<String>();
		String del = "select * from SubscriberViewPortfolioRel where subscriber = ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, sub);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String subscriber = rs.getString("viewPortfolio");
				ret.add(subscriber);
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return ret;
	}

	public void delForCloseView(String viewId, String loginName)
			throws SQLException {
		String del = "delete * from SubscriberViewPortfolioRel where viewPortfolio = ? and subscriber != ?";
		Connection connection = MongoConnect.getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(del);
			stmt.setObject(1, viewId);
			stmt.setObject(2, loginName);
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}
}
