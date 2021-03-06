package com.mmdb.model.database.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.mongo.MongoConnect;

/**
 * datasource的存储类 Created by XIE on 2015/3/30.
 */
@Component("dataSourceStorage")
public class DataSourceStorage {// extends NodeStorage<DataSourcePool>

	private Log log = LogFactory.getLogger("DataSourceStorage");
	@Autowired
	private DataBaseConfigStorage configStorage;

	/**
	 * 根据id获取db配置
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return
	 * @throws Exception
	 */
	public DataSourcePool getById(String id) throws Exception {
		List<DataSourcePool> list = this.getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库[" + id + "]不唯一");
			throw new Exception("数据库[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	public DataSourcePool getByName(String name) throws Exception {
		List<DataSourcePool> list = this.getByProperty("name", name);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库[" + name + "]不唯一");
			throw new Exception("数据库[" + name + "]不唯一");
		} else {
			return null;
		}
	}

	public List<DataSourcePool> getByProperty(String attr, String val) {
		String match = "select * from DataSourcePool where `" + attr + "` = '"
				+ val + "'";
		return query(match);
	}

	public List<DataSourcePool> getAll() {
		String match = "select * from DataSourcePool";
		return query(match);
	}

	public int delete(DataSourcePool pool) {
		String del = "delete from DataSourcePool where _id='" + pool.getId()
				+ "'";
		try {
			return MongoConnect.executeUpdate(del);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int deleteAll() {
		String del = "delete from DataSourcePool";
		try {
			return MongoConnect.executeUpdate(del);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean exist(DataSourcePool pool) {
		return exist(pool.getId());
	}

	public boolean exist(String id) {
		String match = "select * from DataSourcePool where _id='" + id + "'";
		List<DataSourcePool> result = query(match);
		if (result.size() == 0) {
			return false;
		}
		return true;
	}

	public DataSourcePool save(DataSourcePool pool) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("insert into DataSourcePool(name,description,schema,tableName,customSql,databaseConfigId,isSelf,owner) values(?,?,?,?,?,?,?,?)");
			pstmt.setString(1, pool.getName() == null ? "" : pool.getName());
			pstmt.setString(2,
					pool.getDescription() == null ? "" : pool.getDescription());
			pstmt.setString(3, pool.getSchema() == null ? "" : pool.getSchema());
			pstmt.setString(4,
					pool.getTableName() == null ? "" : pool.getTableName());
			pstmt.setString(5,
					pool.getCustomSql() == null ? "" : pool.getCustomSql());
			pstmt.setString(
					6,
					pool.getDatabaseConfigId() == null ? "" : pool
							.getDatabaseConfigId());
			pstmt.setString(7, pool.isSelf() + "");
			pstmt.setString(8, pool.getOwner());
			pstmt.executeUpdate();
			List<DataSourcePool> retDp = getByProperty("name", pool.getName());
			if (retDp.size() == 1) {
				return retDp.get(0);
			} else {
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			delete(pool);
			throw e;
		}
	}

	public DataSourcePool update(DataSourcePool pool) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("update DataSourcePool set name=?,description=?,schema=?,tableName=?,customSql=?,databaseConfigId=?,isSelf=?,owner=? where _id=?");
			pstmt.setString(1, pool.getName() == null ? "" : pool.getName());
			pstmt.setString(2,
					pool.getDescription() == null ? "" : pool.getDescription());
			pstmt.setString(3, pool.getSchema() == null ? "" : pool.getSchema());
			pstmt.setString(4,
					pool.getTableName() == null ? "" : pool.getTableName());
			pstmt.setString(5,
					pool.getCustomSql() == null ? "" : pool.getCustomSql());
			pstmt.setString(
					6,
					pool.getDatabaseConfigId() == null ? "" : pool
							.getDatabaseConfigId());
			pstmt.setString(7, pool.isSelf() + "");
			pstmt.setString(8, pool.getOwner());
			pstmt.setString(9, pool.getId());
			pstmt.executeUpdate();
			List<DataSourcePool> retDp = getByProperty("name", pool.getName());
			if (retDp.size() == 1) {
				return retDp.get(0);
			} else {
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			delete(pool);
			throw e;
		}
	}

	public List<DataSourcePool> query(String sql) {
		List<DataSourcePool> ret = null;
		ResultSet rs = null;
		try {
			Connection conn = MongoConnect.getConnection();
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ret = new ArrayList<DataSourcePool>();
			while (rs.next()) {
				DataSourcePool dp = new DataSourcePool();
				dp.setId(rs.getString("_id"));
				dp.setName(rs.getString("name"));
				dp.setDescription(rs.getString("description"));
				dp.setSchema(rs.getString("schema"));
				dp.setTableName(rs.getString("tableName"));
				dp.setCustomSql(rs.getString("customSql"));
				dp.setDatabaseConfigId(rs.getString("databaseConfigId"));
				dp.setSelf(new Boolean(rs.getString("isSelf")));
				dp.setOwner(rs.getString("owner"));
				ret.add(dp);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}
