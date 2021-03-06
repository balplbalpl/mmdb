package com.mmdb.model.categroy.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.mongo.MongoConnect;

/**
 * 视图分类 - 存储仓库
 * 
 * @author XIE
 */
@Component("viewCateStorage")
public class ViewCateStorage {
	private Log log = LogFactory.getLogger("ViewCateStorage");

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
			String sql = "select * from ViewCategory";
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
	public void delete(List<ViewCategory> list) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from ViewCategory where id=?";
			for (ViewCategory cate : list) {
				pst = conn.prepareStatement(sql);
				pst.setString(1, cate.getId());
				pst.executeUpdate();
			}
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
	 * 单个删除
	 * 
	 * @param t
	 * @throws Exception
	 */
	public void deleteById(String id) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from ViewCategory where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, id);
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
			String sql = "delete from ViewCategory";
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

	public void deleteByUser(User user) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from ViewCategory where userName = ?";
			st = conn.prepareStatement(sql);
			st.setString(1, user.getLoginName());
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

	public void deleteAllOpenVeiwCate() throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			String sql = "delete from ViewCategory where open = ?";
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

	/**
	 * 检查是否存在
	 * 
	 * @param nodeid
	 * @return
	 */
	public boolean exists(String id) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ViewCategory where id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, id);
			rs = pst.executeQuery();
			if (rs != null) {
				if (rs.next()) {
					return true;
				}
			}
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
		return false;
	}

	public List<ViewCategory> getAllOpenViewCate() throws Exception {
		long stime = System.currentTimeMillis();
		List<ViewCategory> result = new ArrayList<ViewCategory>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ViewCategory where isOpen=?";
			pst = conn.prepareStatement(sql);
			pst.setObject(1, true);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String id = rs.getString("_id");
					String name = rs.getString("name");
					String userName = rs.getString("userName");
					boolean open = rs.getBoolean("isOpen");
					long updateTime = rs.getLong("updateTime");
					long createTime = rs.getLong("createTime");
					ViewCategory cate = new ViewCategory(id, name, userName,
							open);
					cate.setUpdateTime(updateTime);
					cate.setCreateTime(createTime);
					cate.setParentName(rs.getString("parent"));
					result.add(cate);
				}

				for (ViewCategory cate1 : result) {
					for (ViewCategory cate2 : result) {
						if (!cate1.getId().equals(cate2.getId())) {
							if (cate1.getParentName().equals(cate2.getId())) {
								List<ViewCategory> children = cate2
										.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<ViewCategory>();
									children.add(cate1);
									cate2.setChildren(children);
								}
								cate1.setParent(cate2);
							}
						}
					}
				}
			}
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
		System.out.println("getAllCate查询所有节点耗时："
				+ (System.currentTimeMillis() - stime));
		return result;
	}

	/**
	 * 取出所有的私有视图数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ViewCategory> getAllByUser(User user) throws Exception {
		long stime = System.currentTimeMillis();
		List<ViewCategory> result = new ArrayList<ViewCategory>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ViewCategory where isOpen=? and userName=?";
			pst = conn.prepareStatement(sql);

			pst.setObject(1, false);
			pst.setString(2, user.getLoginName());
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String id = rs.getString("_id");
					String name = rs.getString("name");
					String userName = rs.getString("userName");
					boolean open = rs.getBoolean("isOpen");
					long updateTime = rs.getLong("updateTime");
					long createTime = rs.getLong("createTime");
					ViewCategory cate = new ViewCategory(id, name, userName,
							open);
					cate.setUpdateTime(updateTime);
					cate.setCreateTime(createTime);
					cate.setParentName(rs.getString("parent"));
					result.add(cate);
				}

				for (ViewCategory cate1 : result) {
					for (ViewCategory cate2 : result) {
						if (!cate1.getId().equals(cate2.getId())) {
							if (cate1.getParentName().equals(cate2.getId())) {
								List<ViewCategory> children = cate2
										.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<ViewCategory>();
									children.add(cate1);
									cate2.setChildren(children);
								}
								cate1.setParent(cate2);
							}
						}
					}
				}
			}
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
		System.out.println("getAllCate查询所有节点耗时："
				+ (System.currentTimeMillis() - stime));
		return result;
	}

	public List<ViewCategory> getAll() throws Exception {
		long stime = System.currentTimeMillis();
		List<ViewCategory> result = new ArrayList<ViewCategory>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ViewCategory ";
			pst = conn.prepareStatement(sql);
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String id = rs.getString("_id");
					String name = rs.getString("name");
					String userName = rs.getString("userName");
					boolean open = rs.getBoolean("isOpen");
					long updateTime = rs.getLong("updateTime");
					long createTime = rs.getLong("createTime");
					ViewCategory cate = new ViewCategory(id, name, userName,
							open);
					cate.setUpdateTime(updateTime);
					cate.setCreateTime(createTime);
					cate.setParentName(rs.getString("parent"));
					result.add(cate);
				}

				for (ViewCategory cate1 : result) {
					for (ViewCategory cate2 : result) {
						if (!cate1.getId().equals(cate2.getId())) {
							if (cate1.getParentName().equals(cate2.getId())) {
								List<ViewCategory> children = cate2
										.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<ViewCategory>();
									children.add(cate1);
									cate2.setChildren(children);
								}
								cate1.setParent(cate2);
							}
						}
					}
				}
			}
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
		System.out.println("getAllCate查询所有节点耗时："
				+ (System.currentTimeMillis() - stime));
		return result;
	}

	public List<ViewCategory> query(String sql, List<String> params)
			throws Exception {
		List<ViewCategory> result = new ArrayList<ViewCategory>();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = conn.prepareStatement(sql);
			int i = 1;
			for (String param : params) {
				pst.setString(i, param);
				i++;
			}
			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String id = rs.getString("_id");
					String name = rs.getString("name");
					String userName = rs.getString("userName");
					boolean open = rs.getBoolean("isOpen");
					long updateTime = rs.getLong("updateTime");
					long createTime = rs.getLong("createTime");
					ViewCategory cate = new ViewCategory(id, name, userName,
							open);
					cate.setUpdateTime(updateTime);
					cate.setCreateTime(createTime);
					cate.setParentName(rs.getString("parent"));
					result.add(cate);
				}

				for (ViewCategory cate1 : result) {
					for (ViewCategory cate2 : result) {
						if (!cate1.getId().equals(cate2.getId())) {
							if (cate1.getParentName().equals(cate2.getId())) {
								List<ViewCategory> children = cate2
										.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<ViewCategory>();
									children.add(cate1);
									cate2.setChildren(children);
								}
								cate1.setParent(cate2);
							}
						}
					}
				}
			}
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
		return result;
	}

	/**
	 * 保存视图分类
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public ViewCategory save(ViewCategory t) throws Exception {
		String id = t.getId();
		String name = t.getName();
		boolean open = t.getOpen();
		String userId = t.getUserId();
		String parentId = t.getParent() != null ? t.getParent().getId() : "";
		long updateTime = t.getUpdateTime();
		long createTime = t.getCreateTime();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		String sql = null;
		try {
			if (id == null) {
				sql = "insert into ViewCategory(name, parent, isOpen, userName,createTime,updateTime) values(?,?,?,?,?,?)";
			} else {
				sql = "insert into ViewCategory(name, parent, isOpen, userName,createTime,updateTime, _id) values(?,?,?,?,?,?,?)";
			}
			pst = conn.prepareStatement(sql);
			pst.setString(1, name);
			pst.setString(2, parentId);
			pst.setObject(3, open);
			pst.setString(4, userId);
			pst.setObject(5, createTime);
			pst.setObject(6, updateTime);
			if (id != null) {
				pst.setString(7, id);
			}
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
		return getByName(open,userId, name, parentId);
	}

	/**
	 * 修改视图分类
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public ViewCategory update(ViewCategory t) throws Exception {
		String id = t.getId();
		String name = t.getName();
		boolean open = t.getOpen();
		String userId = t.getUserId();
		String parentId = t.getParent() != null ? t.getParent().getId() : "";
		long updateTime = t.getUpdateTime();
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update ViewCategory set `name`=?,`parent`=?,`isOpen`=?,`userName`=?,`updateTime`=? where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, name);
			pst.setString(2, parentId);
			pst.setObject(3, open);
			pst.setString(4, userId);
			pst.setObject(5, updateTime);
			pst.setString(6, id);
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
		return t;
	}

	/**
	 * 查询视图分类
	 * 
	 * @param id
	 *            视图分类id
	 * @return ViewCategory
	 * @throws Exception
	 */
	public ViewCategory getById(String id) throws Exception {
		log.dLog("getById");
		long sTime = System.currentTimeMillis();
		ViewCategory ret = null;
		List<ViewCategory> allL = this.getAll();
		for (ViewCategory vc : allL) {// 这个方法写得很无奈啊，有机会一定改了
			if (vc.getId().equals(id)) {
				ret = vc;
				break;
			}
		}
		System.out.println("getById: " + (System.currentTimeMillis() - sTime));
		log.dLog("getById success");
		return ret;
	}

	public ViewCategory getByName(boolean open, String username,String name, String parentId)
			throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			String sql = "select * from ViewCategory where `name` = ? and `isOpen` = ? and parent = ? and `userName` = ?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, name);
			pst.setObject(2, open);
			pst.setString(3, parentId);
			pst.setString(4, username);

			rs = pst.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String id = rs.getString("_id");
					name = rs.getString("name");
					String userName = rs.getString("userName");
					open = rs.getBoolean("isOpen");
					long updateTime = rs.getLong("updateTime");
					long createTime = rs.getLong("createTime");

					ViewCategory cate = new ViewCategory(id, name, userName,
							open);
					cate.setUpdateTime(updateTime);
					cate.setCreateTime(createTime);
					cate.setParentName(rs.getString("parent"));
					return cate;
				}
			}
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
		return null;
	}

	public boolean exists(boolean open,String username, String name, String parentId) {
		try {
			ViewCategory viewCate = getByName(open,username, name, parentId);
			if (viewCate != null)
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}
