package com.mmdb.mongo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ResourceBundle;

import com.mmdb.mongo.mongodb.jdbc.MongoConnection;
import com.mongodb.DB;

public class MongoConnect {
	private static Connection c = null;

	public static Connection getConnection() {
		try {
			if (c == null) {
				ResourceBundle init = ResourceBundle
						.getBundle("config.demo.demo-global");
				String surl = init.getString("mongo.url");
				Class.forName("com.mmdb.mongo.mongodb.jdbc.MongoDriver");
				c = DriverManager.getConnection(surl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return c;
	}

	public static int executeUpdate(String sql) throws SQLException {
		Connection conn = MongoConnect.getConnection();
		Statement stat = null;
		int execute = -1;
		try {
			stat = conn.createStatement();
			execute = stat.executeUpdate(sql);
		} catch (SQLException e) {
			throw e;
		} finally {
			colse(conn, stat, null);
		}
		return execute;
	}

	public static int executeUpdate(String sql, Object parmas)
			throws SQLException {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement stat = null;
		int execute = -1;
		try {
			stat = conn.prepareStatement(sql);
			stat.setObject(1, parmas);
			execute = stat.executeUpdate();
		} catch (SQLException e) {
			throw e;
		} finally {
			colse(conn, stat, null);
		}
		return execute;
	}

	public static int executeUpdate(String sql, List<Object> params) {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement stat = null;
		int execute = -1;
		try {
			stat = conn.prepareStatement(sql);
			for (int i = 0; i < params.size(); i++) {
				stat.setObject(i + 1, params.get(i));
			}
			execute = stat.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			colse(conn, stat, null);
		}
		return execute;
	}

	public static void colse(Connection conn, Statement stat, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (Exception e) {
			}
		}
		if (conn != null) {
			conn = null;
		}
	}
	
	public static void colse(Statement stat, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception e) {
			}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (Exception e) {
			}
		}
	}
	
	public static DB getDb() {
		return ((MongoConnection) getConnection()).getNativeDB();
	}
}
