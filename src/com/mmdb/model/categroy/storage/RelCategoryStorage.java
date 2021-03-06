package com.mmdb.model.categroy.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONObject;
import org.springframework.stereotype.Component;

import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.TypeFactory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.util.Neo4jStringUtils;

/**
 * 关系分类的存储仓库
 * 
 * @author XIE
 */
@Component("rCateStorage")
public class RelCategoryStorage {

	public int count() throws Exception {
		int count = 0;
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select * from RelCategory";
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

	public void delete(RelCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from RelCategory where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getId());
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

	public void deleteAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		try {
			String sql = "delete from RelCategory";
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

	public List<RelCategory> getAll() throws Exception {
		long stime = System.currentTimeMillis();
		List<RelCategory> result = new ArrayList<RelCategory>();
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select * from RelCategory";
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					RelCategory cate = new RelCategory(rs.getString("_id"),
							rs.getString("name"));
					cate.setImage(rs.getString("image"));
					String ownMajor = rs.getString("ownMajor");
					List<Attribute> attrs = new ArrayList<Attribute>();
					JSONArray attrsJson = new JSONArray(
							rs.getString("attributes"));
					for (int i = 0; i < attrsJson.length(); i++) {
						JSONObject attrJson = attrsJson.getJSONObject(i);
						Attribute attr = new Attribute(
								attrJson.getString("name"),
								TypeFactory.getType(attrJson.getString("type")),
								attrJson.getBoolean("hide"), attrJson
										.getBoolean("required"), attrJson
										.getBoolean("level"), attrJson
										.getString("defaultValue"));
						attrs.add(attr);
						if (attr.getName().equals(ownMajor)) {
							cate.setOwnMajor(attr);
						}
					}
					cate.setAttributes(attrs);
					cate.setParentId(rs.getString("parent"));
					String owner = rs.getString("owner");
					cate.setOwner(owner);
					result.add(cate);
				}

				for (RelCategory cate1 : result) {
					for (RelCategory cate2 : result) {
						if (!cate1.getId().equals(cate2.getId())) {
							if (cate1.getParentId().equals(cate2.getId())) {
								List<RelCategory> children = cate2
										.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<RelCategory>();
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
			if (st != null) {
				st.close();
			}
			st = null;
			if (rs != null) {
				rs.close();
			}
			rs = null;
		}
		System.out.println("getAllRelCate查询所有节点耗时："
				+ (System.currentTimeMillis() - stime));
		return result;
	}

	public RelCategory save(RelCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "insert into RelCategory(name, parent, image, ownMajor, attributes,owner) values(?,?,?,?,?,?)";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getName());
			String parent = "";
			if (t.getParent() != null && t.getParent().getId() != null) {
				parent = t.getParent().getId();
			}
			pst.setString(2, parent);
			pst.setString(3, t.getImage());
			String ownMajor = "";
			if (t.getOwnMajor() != null && t.getOwnMajor().getName() != null) {
				ownMajor = t.getOwnMajor().getName();
			}
			pst.setString(4, ownMajor);
			JSONArray attrsJson = new JSONArray();
			List<Attribute> attrs = t.getAttributes();
			if (attrs != null && attrs.size() > 0) {
				for (Attribute attr : attrs) {
					JSONObject attrJson = new JSONObject();
					attrJson.put("name", attr.getName());
					attrJson.put("type", attr.getType().getName());
					attrJson.put("hide", attr.getHide());
					attrJson.put("level", attr.getLevel());
					attrJson.put("required", attr.getRequired());
					attrJson.put("defaultValue", attr.getDefaultValue());
					attrsJson.put(attrJson);
				}
			}
			pst.setString(5, attrsJson.toString());
			pst.setString(6, t.getOwner());
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
		String id = getIdByName(t.getName());
		t.setId(id);
		return t;
	}

	public RelCategory update(RelCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update RelCategory set name=?,parent=?,image=?,ownMajor=?,attributes=? where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(6, t.getId());
			pst.setString(1, t.getName());
			String parent = "";
			if (t.getParent() != null && t.getParent().getId() != null) {
				parent = t.getParent().getId();
			}
			pst.setString(2, parent);
			pst.setString(3, t.getImage());
			String ownMajor = "";
			if (t.getOwnMajor() != null && t.getOwnMajor().getName() != null) {
				ownMajor = t.getOwnMajor().getName();
			}
			pst.setString(4, ownMajor);
			JSONArray attrsJson = new JSONArray();
			List<Attribute> attrs = t.getAttributes();
			if (attrs != null && attrs.size() > 0) {
				for (Attribute attr : attrs) {
					JSONObject attrJson = new JSONObject();
					attrJson.put("name", attr.getName());
					attrJson.put("type", attr.getType().getName());
					attrJson.put("hide", attr.getHide());
					attrJson.put("level", attr.getLevel());
					attrJson.put("required", attr.getRequired());
					attrJson.put("defaultValue", attr.getDefaultValue());
					attrsJson.put(attrJson);
				}
			}
			pst.setString(5, attrsJson.toString());
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

	// /**
	// * 根据id获取一条关系分类
	// *
	// * @param uid 分类UID（当前分类中唯一）
	// * @return RelCategory
	// * @throws Exception
	// */
	public RelCategory getById(String id) throws Exception {
		id = Neo4jStringUtils.cypherESC(id);
		List<RelCategory> list = this.getAll();
		for (RelCategory ci : list) {
			if (ci.getId().equals(id)) {
				return ci;
			}
		}
		return null;
	}

	/**
	 * 通过name返回mongo的id
	 * 
	 * @param name
	 *            name是不能够重复的
	 * @return
	 * @throws Exception
	 */
	public String getIdByName(String name) throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select _id from RelCategory where name = '" + name
					+ "'";
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					String resultId = rs.getString("_id");
					return resultId;
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
		return null;
	}

//	public RelCategory getByidLazy(String id) throws Exception {
//		RelCategory relCate = null;
//		Connection conn = MongoConnect.getConnection();
//		Statement st = null;
//		ResultSet rs = null;
//		try {
//			String sql = "select * from RelCategory where _id = '" + id
//					+ "' or parent='" + id + "'";
//			st = conn.createStatement();
//			rs = st.executeQuery(sql);
//			List<RelCategory> children = new ArrayList<RelCategory>();
//			if (rs != null) {
//				while (rs.next()) {
//					String resultId = rs.getString("_id");
//					if (resultId.equals(id)) {
//						relCate = new RelCategory(rs.getString("_id"),
//								rs.getString("name"));
//						relCate.setImage(rs.getString("image"));
//						String ownMajor = rs.getString("ownMajor");
//						List<Attribute> attrs = new ArrayList<Attribute>();
//						JSONArray attrsJson = new JSONArray(
//								rs.getString("attributes"));
//						for (int i = 0; i < attrsJson.length(); i++) {
//							JSONObject attrJson = attrsJson.getJSONObject(i);
//							Attribute attr = new Attribute(
//									attrJson.getString("name"),
//									TypeFactory.getType(attrJson
//											.getString("type")),
//									attrJson.getBoolean("hide"),
//									attrJson.getBoolean("required"),
//									attrJson.getBoolean("level"),
//									attrJson.getString("defaultValue"));
//							attrs.add(attr);
//							if (attr.getName().equals(ownMajor)) {
//								relCate.setOwnMajor(attr);
//							}
//						}
//						relCate.setAttributes(attrs);
//						relCate.setParentId(rs.getString("parent"));
//					} else {
//						RelCategory child = new RelCategory();
//						children.add(child);
//					}
//				}
//			}
//			if (relCate != null)
//				relCate.setChildren(children);
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw e;
//		} finally {
//			if (st != null) {
//				st.close();
//			}
//			st = null;
//			if (rs != null) {
//				rs.close();
//			}
//			rs = null;
//		}
//		return relCate;
//	}

	public void saveHasId(RelCategory rCate) {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "insert into RelCategory(name, parent, image, ownMajor, attributes,_id,owner) values(?,?,?,?,?,?,?)";
			pst = conn.prepareStatement(sql);
			pst.setString(1, rCate.getName());
			String parent = "";
			if (rCate.getParent() != null && rCate.getParent().getId() != null) {
				parent = rCate.getParent().getId();
			}
			pst.setString(2, parent);
			pst.setString(3, rCate.getImage());
			String ownMajor = "";
			if (rCate.getOwnMajor() != null
					&& rCate.getOwnMajor().getName() != null) {
				ownMajor = rCate.getOwnMajor().getName();
			}
			pst.setString(4, ownMajor);
			JSONArray attrsJson = new JSONArray();
			List<Attribute> attrs = rCate.getAttributes();
			if (attrs != null && attrs.size() > 0) {
				for (Attribute attr : attrs) {
					JSONObject attrJson = new JSONObject();
					attrJson.put("name", attr.getName());
					attrJson.put("type", attr.getType().getName());
					attrJson.put("hide", attr.getHide());
					attrJson.put("level", attr.getLevel());
					attrJson.put("required", attr.getRequired());
					attrJson.put("defaultValue", attr.getDefaultValue());
					attrsJson.put(attrJson);
				}
			}
			pst.setString(5, attrsJson.toString());
			pst.setObject(6, rCate.getId());
			pst.setObject(7, rCate.getOwner());
			pst.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (SQLException e) {
				}
			}
			pst = null;
		}
	}

}
