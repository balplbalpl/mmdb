package com.mmdb.model.database.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.mongo.MongoConnect;

/**
 * 数据库连接 Created by XIE on 2015/3/30.
 */
@Component("dbConfigStorage")
public class DataBaseConfigStorage {// extends NodeStorage<DataBaseConfig>
	private Log log = LogFactory.getLogger("DataBaseConfigStorage");

	/**
	 * 根据id获取db配置
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return
	 * @throws Exception
	 */
	public DataBaseConfig getById(String id) throws Exception {
		List<DataBaseConfig> list = this.getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库配置[" + id + "]不唯一");
			throw new Exception("数据库配置[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	public DataBaseConfig getByName(String name) throws Exception {
		List<DataBaseConfig> list = this.getByProperty("name", name);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("数据库配置[" + name + "]不唯一");
			throw new Exception("数据库配置[" + name + "]不唯一");
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param attr
	 * @param val
	 * @return
	 */
	public List<DataBaseConfig> getByProperty(String attr, String val) {
		String match = "select * from DataBaseConfig where `" + attr + "` = '"
				+ val + "'";
		return query(match);
	}

	public List<DataBaseConfig> getAll() {
		String match = "select * from DataBaseConfig";
		return query(match);
	}

	public int delete(DataBaseConfig config) {
		String del = "delete from DataBaseConfig where _id = '" + config.getId()
				+ "'";
		try {
			return MongoConnect.executeUpdate(del);
			// return execute(del);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int deleteAll() {
		String del = "delete from DataBaseConfig";
		try {
			return MongoConnect.executeUpdate(del);
			// return execute(del);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean exist(DataBaseConfig config) {
		return exist(config.getId());
	}

	/**
	 * 不是neo4jid
	 * 
	 * @param id
	 * @return
	 */
	public boolean exist(String id) {
		List<DataBaseConfig> configs = query("select * from DataBaseConfig where _id = '"
				+ id + "'");
		if (configs.size() == 0)
			return false;
		return true;
	}

	public DataBaseConfig save(DataBaseConfig config) throws Exception {
		try {
			log.dLog("数据库配置[" + config.getId() + "]开始创建!");
			Map<String, String> asMap = config.asMap();
			Connection conn = MongoConnect.getConnection();
			StringBuffer crt = new StringBuffer();
			asMap.remove("id");
			Set<String> keySet = asMap.keySet();
			crt.append("insert into DataBaseConfig(");
			StringBuffer v = new StringBuffer();
			for (String key : keySet) {
				crt.append("`");
				crt.append(key);
				crt.append("`,");

				v.append("?,");
			}
			crt.delete(crt.length() - 1, crt.length());
			v.delete(v.length() - 1, v.length());
			crt.append(") values (");
			crt.append(v);
			v = null;
			crt.append(")");

			PreparedStatement pstmt = conn.prepareStatement(crt.toString());
			int i = 0;
			for (String key : keySet) {
				pstmt.setString(++i, asMap.get(key));
			}
			pstmt.executeUpdate();
			// return update(config);
			return getByName(asMap.get("name"));
		} catch (Exception e) {
			delete(config);
			throw e;
		}
	}

	public DataBaseConfig update(DataBaseConfig config) throws Exception {
		long stime = System.currentTimeMillis();
		StringBuffer upd = new StringBuffer();
		upd.append("update DataBaseConfig set ");
		Map<String, String> asMap = config.asMap();
		Set<String> keySet = asMap.keySet();
		for (String key : keySet) {
			upd.append(" `");
			upd.append(key);
			upd.append("` = ?,");
		}
		upd.delete(upd.length() - 1, upd.length());
		upd.append(" where _id = '");
		upd.append(asMap.get("id"));
		upd.append("'");
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pstmt = conn.prepareStatement(upd.toString());
		int i = 0;
		for (String key : keySet) {
			pstmt.setString(++i, asMap.get(key));
		}
		pstmt.executeUpdate();
		List<DataBaseConfig> configs = query("select * from DataBaseConfig where _id = '"
				+ asMap.get("id") + "'");
		if (configs.size() == 1) {
			log.dLog("数据库配置[" + config.getId() + "]属性更新成功!");
			System.out.println("耗时:" + (System.currentTimeMillis() - stime));
			return configs.get(0);
		} else if (configs.size() > 1) {
			log.eLog("数据库配置[" + config.getId() + "]不唯一");
			throw new Exception("数据库配置[" + config.getId() + "]不唯一");
		} else {
			log.eLog("数据库配置[" + config.getId() + "]不唯一");
			throw new Exception("数据库配置[" + config.getId() + "]不存在");
		}
	}

	public List<DataBaseConfig> query(String sql) {
		List<DataBaseConfig> ret = null;
		ResultSet resultSet = null;
		try {
			Connection conn = MongoConnect.getConnection();
			Statement stmt = conn.createStatement();
			resultSet = stmt.executeQuery(sql);
			ret = new ArrayList<DataBaseConfig>();
			while (resultSet.next()) {
				ResultSetMetaData metaData = resultSet.getMetaData();
				int count = metaData.getColumnCount();
				Map<String, String> data = new HashMap<String, String>();
				for (int i = 1; i <= count; i++) {
					String key = metaData.getColumnLabel(i);
					String value = resultSet.getString(key);
					data.put(key, value);
				}
				ret.add(nodeToObject(data));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	// public int execute(String sql) throws Exception {
	// Connection conn = MongoConnect.getConnection();
	// Statement stmt;
	// try {
	// stmt = conn.createStatement();
	// return stmt.executeUpdate(sql);
	// } catch (Exception e) {
	// throw e;
	// }
	// }

	public DataBaseConfig nodeToObject(Map<String, String> data) {
		try {
			boolean isRac = Boolean.parseBoolean(data.get("isRac"));

			String id = data.get("_id");

			Map<String, String> dataMap = new HashMap<String, String>();
			for (Iterator<String> keys = data.keySet().iterator(); keys
					.hasNext();) {
				String key = keys.next();
				String value = data.get(key);
				dataMap.put(key, value);
			}//??
			DataBaseConfig config = new DataBaseConfig(isRac, dataMap);
			config.setId(id);
			config.setOwner(data.get("owner"));
			return config;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
