package com.mmdb.model.icon.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import com.mmdb.model.icon.ViewIcon;
import com.mmdb.mongo.MongoConnect;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository("viewIconStorage")
public class ViewIconStorage {
	private DB db = MongoConnect.getDb();
	private DBCollection viewIcon = db.getCollection("ViewIcon");
	private DBCollection mapping = db.getCollection("ViewIconMapping");

	public ViewIcon save(ViewIcon icon) {
		mapping.insert(new BasicDBObject().append("md5", icon.getMd5())
				.append("username", icon.getUsername())
				.append("contentType", icon.getContentType())
				.append("name", icon.getName()));
		DBObject iconData = viewIcon.findOne(new BasicDBObject().append("md5",
				icon.getMd5()));
		if (iconData == null) {
			viewIcon.insert(new BasicDBObject().append("md5", icon.getMd5())
					.append("content", icon.getContent()));
		}
		DBObject result = mapping.findOne(new BasicDBObject().append(
				"username", icon.getUsername()).append("name", icon.getName()));
		String _id = ((ObjectId) result.get("_id")).toHexString();
		icon.setId(_id);
		return icon;
	}

	/**
	 * 使用mongo的id
	 * 
	 * @param id
	 * @return
	 */
	public ViewIcon getById(String id) throws Exception {
		DBObject result = mapping.findOne(new ObjectId(id));
		if (result == null) {
			return null;
		}
		String username = (String) result.get("username");
		String md5 = (String) result.get("md5");
		String contentType = (String) result.get("contentType");
		String name = (String) result.get("name");
		DBObject iconData = viewIcon.findOne(new BasicDBObject().append("md5",
				md5));
		if (iconData == null) {
			throw new Exception("icon不存在");
		}
		Object content = iconData.get("content");
		ViewIcon icon = new ViewIcon(name, username, contentType, content, md5);
		icon.setId(id);
		return icon;
	}

	/**
	 * 
	 * @param username
	 * @param iconName
	 * @return
	 * @throws Exception
	 */
	public ViewIcon getByName(String iconName, String username)
			throws Exception {
		DBObject result = mapping.findOne(new BasicDBObject().append(
				"username", username).append("name", iconName));
		if (result == null) {
			return null;
		}
		String id = ((ObjectId) result.get("_id")).toHexString();
		String md5 = (String) result.get("md5");
		String contentType = (String) result.get("contentType");
		String name = (String) result.get("name");
		DBObject iconData = viewIcon.findOne(new BasicDBObject().append("md5",
				md5));
		if (iconData == null) {
			throw new Exception("icon不存在");
		}
		Object content = iconData.get("content");
		ViewIcon icon = new ViewIcon(name, username, contentType, content, md5);
		icon.setId(id);
		return icon;
	}

	public int getCount(String userName) {
		DB db = MongoConnect.getDb();
		DBCollection mapping = db.getCollection("ViewIconMapping");
		return (int) mapping.count(new BasicDBObject().append("username",
				userName));
	}

	public List<ViewIcon> getByUser(String username) throws Exception {
		List<ViewIcon> icons = new ArrayList<ViewIcon>();
		Map<String, Object> cache = new HashMap<String, Object>();
		DBCursor find = mapping.find(new BasicDBObject().append("username",
				username));
		for (DBObject result : find) {
			String id = ((ObjectId) result.get("_id")).toHexString();
			String md5 = (String) result.get("md5");
			String contentType = (String) result.get("contentType");
			String name = (String) result.get("name");
			Object content = cache.get(md5);
			if (content == null) {
				DBObject iconData = viewIcon.findOne(new BasicDBObject()
						.append("md5", md5));
				if (iconData == null) {
					throw new Exception("icon不存在");
				}
				content = iconData.get("content");
				cache.put(md5, content);
			}

			ViewIcon icon = new ViewIcon(name, username, contentType, content,
					md5);
			icon.setId(id);
			icons.add(icon);
		}

		return icons;
	}

	public List<ViewIcon> getAll() throws Exception {
		List<ViewIcon> icons = new ArrayList<ViewIcon>();
		Map<String, Object> cache = new HashMap<String, Object>();
		DBCursor find = mapping.find(new BasicDBObject());
		for (DBObject result : find) {
			String id = ((ObjectId) result.get("_id")).toHexString();
			String md5 = (String) result.get("md5");
			String contentType = (String) result.get("contentType");
			String name = (String) result.get("name");
			String username = (String) result.get("username");
			Object content = cache.get(md5);
			if (content == null) {
				DBObject iconData = viewIcon.findOne(new BasicDBObject()
						.append("md5", md5));
				if (iconData == null) {
					throw new Exception("icon不存在");
				}
				content = iconData.get("content");
				cache.put(md5, content);
			}

			ViewIcon icon = new ViewIcon(name, username, contentType, content,
					md5);
			icon.setId(id);
			icons.add(icon);
		}

		return icons;
	}

	public void deleteAll() {
		viewIcon.drop();
		mapping.drop();
	}

	public void deleteByUser(String username) {
		// DB db = MongoConnect.getDb();
		// GridFS gridFs = new GridFS(db, collectionName);
		// gridFs.remove(new BasicDBObject().append("username", username));
	}

	public void delete(String iconName, String username) {
		DBObject findOne = mapping.findOne(new BasicDBObject().append(
				"username", username).append("name", iconName));
		if (findOne != null) {
			String md5 = (String) findOne.get("md5");
			DBCursor find = mapping
					.find(new BasicDBObject().append("md5", md5));
			if (find.size() == 1) {
				viewIcon.remove(new BasicDBObject().append("md5", md5));
			}
			mapping.remove(new BasicDBObject().append("username", username)
					.append("name", iconName));
		}
	}

}
