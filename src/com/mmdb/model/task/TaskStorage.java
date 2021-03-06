package com.mmdb.model.task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.mongo.MongoConnect;

/**
 * 任务 - 存储仓库
 * 
 * @author XIE
 * @author Edit by tz 2015-05-29
 * 
 */
@Service("taskStorage")
public class TaskStorage {
	private Log log = LogFactory.getLogger("TaskStorage");

	/**
	 * 根据id获取一条任务
	 * 
	 * @param uName
	 *            关系映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public Task getById(String id) throws Exception {
		List<Task> list = getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("任务对象[" + id + "]不唯一");
			throw new Exception("任务对象[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	/**
	 * 根据name获取一条任务
	 * 
	 * @param uName
	 *            关系映射名称(不可重复)
	 * @return
	 * @throws Exception
	 */
	public Task getByName(String uName) throws Exception {
		List<Task> list = getByProperty("name", uName);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("任务对象[" + uName + "]不唯一");
			throw new Exception("任务对象[" + uName + "]不唯一");
		} else {
			return null;
		}
	}

	public List<Task> getByProperty(String key, String value)
			throws SQLException {
		String match = "select * from Task where `" + key + "` = '" + value
				+ "'";
		return query(match);
	}

	/**
	 * 批量删除任务
	 * 
	 * @param list
	 *            List<Task>
	 * 
	 * */
	public void delete(List<Task> list) throws Exception {
		if (list.size() > 0) {
			for (Task cate : list) {
				this.delete(cate);
			}
		}
	}

	/**
	 * 删除单个任务
	 * 
	 * @param t
	 *            Task
	 * 
	 * */
	public void delete(Task t) throws Exception {
		String del = "delete from Task where _id ='" + t.getId() + "'";
		MongoConnect.executeUpdate(del);
	}

	/**
	 * 删除所有任务（包括和它建立联系的内部映射，外部映射）
	 * 
	 * */
	public void deleteAll() throws Exception {
		String del = "delete from Task";
		MongoConnect.executeUpdate(del);
	}

	/**
	 * 检查任务是否存在
	 * 
	 * @param nodeid
	 *            Long
	 * @throws Exception
	 * */
	public boolean exist(String id) {
		try {
			if (getById(id) == null) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 获取所有的任务
	 * 
	 * */
	public List<Task> getAll() throws Exception {
		String match = "select * from Task";
		return query(match);
	}

	/**
	 * 保存任务
	 * 
	 * @param t
	 *            Task
	 * 
	 * */
	public Task save(Task t) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("insert into Task(name,`open`,timeOut,timing,dbCiCateMapIds,outCiCateMapIds,inCiCateMapIds,perfDbMapIds,kpiMapIds,owner) values(?,?,?,?,?,?,?,?,?,?)");
			pstmt.setString(1, t.getName() == null ? "" : t.getName());
			pstmt.setString(2, t.getOpen() + "");
			pstmt.setString(3, t.getTimeOut() + "");
			pstmt.setString(4, t.getTiming() == null ? "{}" : JSONObject
					.fromObject(t.getTiming()).toString());
			pstmt.setString(5, t.getDbCiCateMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getDbCiCateMapIds()).toString());
			pstmt.setString(6, t.getOutCiCateMapIds() == null ? "[]"
					: JSONArray.fromObject(t.getOutCiCateMapIds()).toString());
			pstmt.setString(7, t.getInCiCateMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getInCiCateMapIds()).toString());
			pstmt.setString(8, t.getPerfDbMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getPerfDbMapIds()).toString());
			pstmt.setString(9, t.getKpiSyncMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getKpiSyncMapIds()).toString());
			pstmt.setString(10, t.getOwner() == null ? "" : t.getOwner());
			pstmt.executeUpdate();
			List<Task> retDp = getByProperty("name", t.getName());
			if (retDp.size() == 1) {
				return retDp.get(0);
			} else {
				throw new Exception("保存失败");
			}
		} catch (Exception e) {
			delete(t);
			throw e;
		}
	}

	/**
	 * 更新任务
	 * 
	 * @param t
	 *            Task
	 * */
	public Task update(Task t) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("update Task set name=?,`open`=?,timeOut=?,timing=?,dbCiCateMapIds=?,outCiCateMapIds=?,inCiCateMapIds=?,perfDbMapIds=?,kpiMapIds=? where _id=?");
			pstmt.setString(1, t.getName() == null ? "" : t.getName());
			pstmt.setString(2, t.getOpen() + "");
			pstmt.setString(3, t.getTimeOut() + "");
			pstmt.setString(4, t.getTiming() == null ? "{}" : JSONObject
					.fromObject(t.getTiming()).toString());
			pstmt.setString(5, t.getDbCiCateMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getDbCiCateMapIds()).toString());
			pstmt.setString(6, t.getOutCiCateMapIds() == null ? "[]"
					: JSONArray.fromObject(t.getOutCiCateMapIds()).toString());
			pstmt.setString(7, t.getInCiCateMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getInCiCateMapIds()).toString());
			pstmt.setString(8, t.getPerfDbMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getPerfDbMapIds()).toString());
			pstmt.setString(9, t.getKpiSyncMapIds() == null ? "[]" : JSONArray
					.fromObject(t.getKpiSyncMapIds()).toString());
			pstmt.setString(10, t.getId());
			pstmt.executeUpdate();
			List<Task> retDp = getByProperty("name", t.getName());
			if (retDp.size() == 1) {
				return retDp.get(0);
			} else {
				throw new Exception("修改失败");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public List<Task> query(String sql) throws SQLException {
		List<Task> ret = new ArrayList<Task>();
		Statement pstmt = null;
		ResultSet rs = null;
		Connection conn = MongoConnect.getConnection();
		try {
			pstmt = conn.createStatement();
			rs = pstmt.executeQuery(sql);
			while (rs.next()) {
				Task task = new Task();
				task.setId(rs.getString("_id"));
				task.setName(rs.getString("name"));
				String owner = null;
				try {
					owner = rs.getString("owner");
				} catch (Exception e) {
					owner = "";
				}
				task.setOpen(new Boolean(rs.getString("open")));
				task.setTimeOut(new Boolean(rs.getString("timeOut")));
				JSONObject timing = null;
				try {
					timing = JSONObject.fromObject(rs.getString("timing"));
				} catch (Exception e) {
					timing = new JSONObject();
				}
				task.setTiming(timing);
				JSONArray cateList = null;
				try {
					cateList = JSONArray.fromObject(rs
							.getString("dbCiCateMapIds"));
				} catch (Exception e) {
					cateList = new JSONArray();
				}
				task.setDbCiCateMapIds(cateList);
				JSONArray relList = null;
				try {
					relList = JSONArray.fromObject(rs
							.getString("outCiCateMapIds"));
				} catch (Exception e) {
					relList = new JSONArray();
				}
				task.setOutCiCateMapIds(relList);
				JSONArray ciList = null;
				try {
					ciList = JSONArray.fromObject(rs
							.getString("inCiCateMapIds"));
				} catch (Exception e) {
					ciList = new JSONArray();
				}
				task.setInCiCateMapIds(ciList);
				JSONArray perfList = null;
				try {
					perfList = JSONArray.fromObject(rs
							.getString("perfDbMapIds"));
				} catch (Exception e) {
					perfList = new JSONArray();
				}
				task.setPerfDbMapIds(perfList);
				JSONArray kpiList = null;
				try {
					kpiList = JSONArray.fromObject(rs.getString("kpiMapIds"));
				} catch (Exception e) {
					kpiList = new JSONArray();
				}
				task.setKpiSyncMapIds(kpiList);
				task.setOwner(owner);
				ret.add(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return ret;
	}

}
