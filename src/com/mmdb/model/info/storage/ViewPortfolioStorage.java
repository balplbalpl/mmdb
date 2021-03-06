package com.mmdb.model.info.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.info.ViewPortfolio;
import com.mmdb.mongo.MongoConnect;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 组合视图存储
 * 
 * @author XIE
 * 
 */
@Component("viewPortfolioStorage")
public class ViewPortfolioStorage {
	Log log = LogFactory.getLogger("ViewPortfolioStorage");
	private DBCollection viewColl;

	public ViewPortfolioStorage() {
		DB db = MongoConnect.getDb();
		viewColl = db.getCollection("ViewPtfl");
	}

	/**
	 * 获取总数
	 * 
	 * @return
	 * @throws Exception
	 */
	public int count() throws Exception {
		return (int) viewColl.count();
	}

	/**
	 * 批量删除
	 * 
	 * @param list
	 * @throws Exception
	 */
	public void delete(List<ViewPortfolio> list) {
		BasicDBObject del = new BasicDBObject();
		for (ViewPortfolio viewPortfolio : list) {
			del.append("_id", new ObjectId(viewPortfolio.getId()));
		}
		viewColl.remove(new BasicDBObject().append("$or", del));
	}

	public void deleteById(String id) {
		viewColl.remove(new BasicDBObject().append("_id", new ObjectId(id)));
	}

	public void deleteAll() {
		viewColl.remove(new BasicDBObject());
	}

	public void deleteByUser(String userName) {
		viewColl.remove(new BasicDBObject("userName", userName).append(
				"isOpen", false));
	}

	public void deleteAllOpenViewPort() {
		viewColl.remove(new BasicDBObject("isOpen", true));
	}

	/**
	 * 取出所有的视图数据
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ViewPortfolio> getAll() {
		List<ViewPortfolio> ret = new ArrayList<ViewPortfolio>();
		DBCursor find = viewColl.find(new BasicDBObject());
		for (DBObject dbObject : find) {
			ViewPortfolio viewp = transToObject(dbObject);
			ret.add(viewp);
		}
		return ret;
	}

	/**
	 * 根据id查询
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public ViewPortfolio getById(String id) throws Exception {
		try {
			DBObject findOne = viewColl.findOne(new BasicDBObject("_id",
					new ObjectId(id)));
			return transToObject(findOne);
		} catch (Exception e) {
			
		}
		return null;
	}
	public ViewPortfolio getByName(String name,String username) throws Exception {
		try {
			DBObject findOne = viewColl.findOne(new BasicDBObject("name",
					name).append("userName", username));
			return transToObject(findOne);
		} catch (Exception e) {
			
		}
		return null;
	}

	public List<ViewPortfolio> getAllByUser(String userName) {
		List<ViewPortfolio> ret = new ArrayList<ViewPortfolio>();
		DBCursor find = viewColl.find(new BasicDBObject("userName", userName));
		for (DBObject dbObject : find) {
			ViewPortfolio viewp = transToObject(dbObject);
			ret.add(viewp);
		}
		return ret;
	}

	public List<ViewPortfolio> getAllOpenViewPort() {
		List<ViewPortfolio> ret = new ArrayList<ViewPortfolio>();
		DBCursor find = viewColl.find(new BasicDBObject("isOpen", true));
		for (DBObject dbObject : find) {
			ViewPortfolio viewp = transToObject(dbObject);
			ret.add(viewp);
		}
		return ret;
	}

	/**
	 * 保存组合视图
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public ViewPortfolio save(ViewPortfolio t) throws Exception {
		BasicDBObject data = new BasicDBObject();
		data.append("content", t.getContent());
		data.append("name", t.getName());
		data.append("time", t.getTime());
		data.append("userName", t.getUserName());
		data.append("isOpen", t.isOpen());
		String id = t.getId();
		if (id != null)
			data.append("_id", new ObjectId(id));
		viewColl.save(data);
		return getByName(t.getName(), t.getUserName());
	}

	/**
	 * 修改组合视图
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public ViewPortfolio update(ViewPortfolio t) throws Exception {
		BasicDBObject data = new BasicDBObject();
		data.append("content", t.getContent());
		data.append("name", t.getName());
		data.append("time", t.getTime());
		data.append("userName", t.getUserName());
		data.append("isOpen", t.isOpen());
		DBObject db = viewColl.findAndModify(new BasicDBObject("_id",
				new ObjectId(t.getId())), data);
		return transToObject(db);
	}

	public boolean exists(boolean open, String name, String userName) {
		BasicDBObject find = new BasicDBObject("isOpne", open).append("name",
				name);
		if (userName != null) {
			find.append("userName", userName);
		}
		DBObject findOne = viewColl.findOne(find);
		if (findOne != null)
			return true;
		return false;
	}

	public ViewPortfolio transToObject(DBObject db) {
		ViewPortfolio viewp = new ViewPortfolio();
		String id = ((ObjectId) db.get("_id")).toHexString();
		String content = (String) db.get("content");
		String name = (String) db.get("name");
		Long time = (Long) db.get("time");
		String userName = (String) db.get("userName");
		boolean open = (Boolean) db.get("isOpen");

		viewp.setContent(content);
		viewp.setId(id);
		viewp.setName(name);
		viewp.setOpen(open);
		viewp.setTime(time);
		viewp.setUserName(userName);
		return viewp;
	}

	/**
	 * 获取所有拥有共有视图的用户名称
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllOpenViewPortfolioAuthor() throws Exception {
		Set<String> names = new HashSet<String>();
		String sql = "select `userName` from ViewPtfl where `isOpen` = ?";
		Connection conn = MongoConnect.getConnection();
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement(sql);
			st.setObject(1, true);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("userName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (st != null) {
				st.close();
			}
			st = null;
		}
		return new ArrayList<String>(names);
	}

	public List<ViewPortfolio> getAllOpenViewPortByUser(String user) {
		List<ViewPortfolio> ret = new ArrayList<ViewPortfolio>();
		DBCursor find = viewColl.find(new BasicDBObject("userName", user)
				.append("isOpen", true));
		for (DBObject dbObject : find) {
			ViewPortfolio viewp = transToObject(dbObject);
			ret.add(viewp);
		}
		return ret;
	}

}
