package com.mmdb.model.role.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.stereotype.Repository;

import com.mmdb.model.role.IRoleDao;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.mongo.mongodb.jdbc.MongoStatement;

@Repository("roleDao")
public class RoleDao implements IRoleDao {

	private Connection conn = null;

	public RoleDao() {
		if (conn == null) {
			// MongoConnect mongo = new MongoConnect();
			conn = MongoConnect.getConnection();
		}
	}

	@Override
	public boolean saveObject(String sql) {
		boolean flag = true;
		try {
			conn.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean updateObject(String sql) {

		boolean flag = true;
		try {
			conn.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public boolean deleteObject(String sql) {
		boolean flag = true;
		try {
			conn.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			flag = false;
		}
		return flag;
	}

	@Override
	public ResultSet getAll(String sql) {
		ResultSet rs = null;
		try {
			rs = conn.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public ResultSet getAll(String sql, int start, int limit) {
		ResultSet rs = null;
		try {
			rs = conn.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public ResultSet getObjectById(String sql) {
		ResultSet rs = null;
		try {
			rs = conn.prepareStatement(sql).executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public int getCount(String sql) {
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

}
