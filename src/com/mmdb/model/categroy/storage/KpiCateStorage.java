package com.mmdb.model.categroy.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.mongo.MongoConnect;

/**
 * KPI分类 - 存储仓库
 * 
 * @author yuhao.guan
 * @version 2015-7-10
 */
@Component("kpiCateStorage")
public class KpiCateStorage {

	/**
	 * 通过ID删除分类
	 * 
	 * @param kpiCategory 对象
	 * @throws Exception
	 */
	public void delete(KpiCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from KpiCategory where _id=?";
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

	/**
	 * 批量删除分类
	 * 
	 * @param list
	 * @throws Exception
	 */
	public void delete(List<KpiCategory> list) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "delete from KpiCategory where _id=?";
			for (KpiCategory cate : list) {
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
	 * 删除全部分类
	 * 
	 * @throws Exception
	 */
	public void deleteAll() throws Exception {
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		try {
			String sql = "delete from KpiCategory";
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

	/**
	 * 获取到全部的KPI分类
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiCategory> getAll() throws Exception {
		String sql = "select * from KpiCategory";
		List<KpiCategory> ret = query(sql);
		if(ret!=null){
			return ret;
		}else{
			throw new Exception("获取数据异常");
		}
	}

	/**
	 * 获取全部分类，分类信息中包含子类信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiCategory> getAllHasChildren() throws Exception {
		List<KpiCategory> allList = this.getAll();
		for(KpiCategory cate1:allList){
			for(KpiCategory cate2:allList){
				if (!cate1.getId().equals(cate2.getId())) {
					if (cate1.getParentId().equals(cate2.getId())) {
						List<KpiCategory> children = cate2.getChildren();
						if (children != null) {
							if (!children.contains(cate1)) {
								children.add(cate1);
							}
						} else {
							children = new ArrayList<KpiCategory>();
							children.add(cate1);
							cate2.setChildren(children);
						}
						cate1.setParent(cate2);
					}
				}
			}
		}
		
		return allList;
	}
	
	/**
	 * 保存KPI分类
	 * 
	 * @param KpiCategory 对象
	 * @return
	 * @throws Exception
	 */
	public KpiCategory save(KpiCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		String id = t.getId();
		boolean isHasId = !(id==null || "".equals(id.trim()));
		try {
			String sql = "insert into KpiCategory(name" + (isHasId?", _id":"") + ",parent,image,owner) values(?" + (isHasId?", ?":"") + ",?,?,?)";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getName());
			if (isHasId){
				pst.setString(2, id);
				pst.setString(3, t.getParentId());
				pst.setString(4, t.getImage());
				pst.setString(5, t.getOwner());
			}else{
				pst.setString(2, t.getParentId());
				pst.setString(3, t.getImage());
				pst.setString(4, t.getOwner());
			}
			pst.executeUpdate();
			return getByName(t.getName());
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
	 * 更新KPI分类
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public KpiCategory update(KpiCategory t) throws Exception {
		Connection conn = MongoConnect.getConnection();
		PreparedStatement pst = null;
		try {
			String sql = "update KpiCategory set name=?,parent=?,image=? where _id=?";
			pst = conn.prepareStatement(sql);
			pst.setString(1, t.getName());
			pst.setString(2, t.getParentId());
			pst.setString(3, t.getImage());
			pst.setString(4, t.getId());
			pst.executeUpdate();
			return getById(t.getId());
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
	 * 根据id获取一条配置项分类
	 * 
	 * @param id
	 *            分类id（当前分类中唯一）
	 * @return KpiCategory
	 * @throws Exception
	 */
	public KpiCategory getById(String id) throws Exception {
		List<KpiCategory> ret = getByProperty("_id", id);
		if(ret!=null){
			if(ret.size()==1){
				return ret.get(0);
			}else if(ret.size()>1){
				throw new Exception("数据["+id+"]不唯一");
			}else{
				return null;
			}
		}else{
			throw new Exception("获取数据异常");
		}
	}
	
	/**
	 * 通过名称获取KPI分类
	 * 
	 * @param name
	 * @return KpiCategory
	 * @throws Exception
	 */
	public KpiCategory getByName(String name) throws Exception {
		List<KpiCategory> ret = getByProperty("name", name);
		if(ret!=null){
			if(ret.size()==1){
				return ret.get(0);
			}else if(ret.size()>1){
				throw new Exception("数据["+name+"]不唯一");
			}else{
				return null;
			}
		}else{
			throw new Exception("获取数据异常");
		}
	}

	/**
	 * 通过用户获取KPI分类
	 * 
	 * @param userName
	 * @return List<KpiCategory>
	 * 
	 * @throws Exception
	 */
	public List<KpiCategory> getByUser(String userName) throws Exception {
		List<KpiCategory> ret = getByProperty("owner", userName);
		return ret;
	}
	
	/**
	 * 通过指定的属性获取KPI分类
	 * 
	 * @param key 属性key
	 * @param val 属性value
	 * 
	 * @return List<KpiCategory>
	 */
	private List<KpiCategory> getByProperty(String key, String val){
		String sql = "select * from KpiCategory where `"+key+"`='"+val+"'";
		return query(sql);
	}
	
	/**
	 * 通过sql查询KPI分类
	 * 
	 * @param sql
	 * @return List<KpiCategory>
	 */
	private List<KpiCategory> query(String sql){
		List<KpiCategory> result = new ArrayList<KpiCategory>();
		Connection conn = MongoConnect.getConnection();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs != null) {
				while (rs.next()) {
					KpiCategory cate = new KpiCategory();
					cate.setId(rs.getString("_id"));
					cate.setName(rs.getString("name"));
					cate.setParentId(rs.getString("parent"));
					cate.setImage(rs.getString("image"));
					cate.setOwner(rs.getString("owner"));
					result.add(cate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			st = null;
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			rs = null;
		}
		return result;
	}
}
