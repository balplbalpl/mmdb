package com.mmdb.model.categroy.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONException;
import org.neo4j.shell.util.json.JSONObject;
import org.springframework.stereotype.Component;

import com.mmdb.core.exception.MException;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.TypeFactory;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.mongo.MongoConnect;
import com.mmdb.util.Neo4jStringUtils;

/**
 * 配置项分类 - 存储仓库
 * 
 * @author XIE
 */
@Component("ciCateStorage")
public class CiCateStorage {

	public void delete(CiCategory t) throws Exception {
		MongoConnect.executeUpdate("delete from CiCategory where _id=?",
				t.getId());
	}

	public void delete(List<CiCategory> list) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from CiCategory where _id=?";
			for (CiCategory cate : list) {
				pst = conn.prepareStatement(sql);
				pst.setString(1, cate.getId());
				pst.executeUpdate();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			MongoConnect.colse(pst, null);
		}
	}

	public void deleteAll() throws Exception {
		MongoConnect.executeUpdate("delete from CiCategory");
	}

	public List<CiCategory> getAll() throws Exception {
		List<CiCategory> result = new ArrayList<CiCategory>();
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "select * from CiCategory";
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					CiCategory cate = new CiCategory(rs.getString("_id"),
							rs.getString("name"));
					cate.setImage(rs.getString("image"));
					String ownMajor = rs.getString("ownMajor");
					String clientId = rs.getString("clientId");
					String owner = rs.getString("owner");
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
						if (attr.getName().equals(clientId)) {
							cate.setClientId(attr);
						}
					}
					if(cate.getClientId()==null){
						Attribute t = new Attribute();
						t.setName(clientId);
						cate.setClientId(t);
					}
					cate.setAttributes(attrs);
					cate.setParentId(rs.getString("parent"));
					cate.setOwner(owner);
					result.add(cate);
				}

				for (CiCategory cate1 : result) {
					for (CiCategory cate2 : result) {
						if (!cate1.getName().equals(cate2.getName())) {
							if (cate2.getId().equals(cate1.getParentId())) {
								List<CiCategory> children = cate2.getChildren();
								if (children != null) {
									if (!children.contains(cate1)) {
										children.add(cate1);
									}
								} else {
									children = new ArrayList<CiCategory>();
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
			MongoConnect.colse(st, rs);
		}
		return result;
	}

	public CiCategory save(CiCategory t) throws Exception {
		String ownMajor = "";
		String clientId = "";
		String owner = t.getOwner();
		owner = owner == null ? "" : owner;
		if (t.getOwnMajor() != null && t.getOwnMajor().getName() != null) {
			ownMajor = t.getOwnMajor().getName();
		}
		if (t.getClientId() != null && t.getClientId().getName() != null) {
			clientId = t.getClientId().getName();
		}
		String sql = null;
		String id = t.getId();
		boolean isHasId = !(id == null || "".equals(id));
		if (isHasId) {
			sql = "insert into CiCategory(`_id`, `name`, `parent`, `image`, `ownMajor`, `clientId`, `attributes`,`owner`) values(?,?,?,?,?,?,?,?)";
		} else {
			sql = "insert into CiCategory( `name`, `parent`, `image`, `ownMajor`, `clientId`, `attributes`,`owner`) values(?,?,?,?,?,?,?)";
		}

		List<Object> params = new ArrayList<Object>();
		if (isHasId) {
			params.add(id);
		}
		params.add(t.getName());
		params.add(t.getParentId());
		params.add(t.getImage());
		params.add(ownMajor);
		params.add(clientId);
		params.add(attrsToString(t));
		params.add(owner);
		
		MongoConnect.executeUpdate(sql, params);
		if (!isHasId) {
			id = getIdByName(t.getName());
		}
		t.setId(id);
		return t;
	}

	public Map<String, CiCategory> getAllMap() {
		Map<String, CiCategory> map = new HashMap<String, CiCategory>();
		try {
			List<CiCategory> all = getAll();
			for (CiCategory cate : all) {
				map.put(cate.getId(), cate);
			}
		} catch (Exception e) {
		}
		return map;
	}

	public CiCategory update(CiCategory t) throws Exception {
		String ownMajor = "";
		String clientId = "";
		String owner = t.getOwner();
		if (t.getOwnMajor() != null && t.getOwnMajor().getName() != null) {
			ownMajor = t.getOwnMajor().getName();
		}

		if (t.getClientId() != null && t.getClientId().getName() != null) {
			clientId = t.getClientId().getName();
		}
		String sql = "update CiCategory set parent=?,image=?,ownMajor=?,clientId=?,attributes=?,owner=? where name=?";

		List<Object> params = new ArrayList<Object>();
		params.add(t.getParentId());
		params.add(t.getImage());
		params.add(ownMajor);
		params.add(clientId);
		params.add(attrsToString(t));
		params.add(owner);
		params.add(t.getName());

		MongoConnect.executeUpdate(sql, params);
		return t;
	}

	/**
	 * 根据id获取一条配置项分类
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return CiCategory
	 * @throws Exception
	 */
	public CiCategory getById(String id) throws Exception {
		id = Neo4jStringUtils.cypherESC(id);
		List<CiCategory> list = this.getAll();
		for (CiCategory ci : list) {
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
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			String sql = "select _id from CiCategory where name = ?";
			st = conn.prepareStatement(sql);
			st.setObject(1, name);
			rs = st.executeQuery();
			if (rs != null) {
				while (rs.next()) {
					String resultId = rs.getString("_id");
					return resultId;
				}
			}
		} finally {
			MongoConnect.colse(st, rs);
		}
		return null;
	}

	private String attrsToString(CiCategory t) throws MException {
		JSONArray attrsJson = new JSONArray();
		List<Attribute> attrs = t.getAttributes();
		try {
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
		} catch (JSONException e) {
			throw new MException("配置项分类属性参数异常!");
		}
		return attrsJson.toString();
	}
}
