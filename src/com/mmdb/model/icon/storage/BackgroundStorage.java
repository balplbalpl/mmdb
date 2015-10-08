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

@Repository("backgroundStorage")
public class BackgroundStorage {
	DB db = MongoConnect.getDb();
	DBCollection viewIcon = db.getCollection("ViewIcon");
	DBCollection background = db.getCollection("BackgroundMapping");

	public ViewIcon save(ViewIcon icon) {
		background.insert(new BasicDBObject().append("md5", icon.getMd5())
				.append("username", icon.getUsername())
				.append("contentType", icon.getContentType())
				.append("name", icon.getName())
				.append("width", icon.getWidth())
				.append("height", icon.getHeight()));

		DBObject iconData = viewIcon.findOne(new BasicDBObject().append("md5",
				icon.getMd5()));

		if (iconData == null) {
			viewIcon.insert(new BasicDBObject().append("md5", icon.getMd5())
					.append("content", icon.getContent()));
		}
		DBObject result = background.findOne(new BasicDBObject().append(
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
		DBObject result = background.findOne(new ObjectId(id));
		return toObject(result, null);
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
		DBObject result = background.findOne(new BasicDBObject().append(
				"username", username).append("name", iconName));
		return toObject(result, null);
	}

	public int getCount(String userName) {
		return (int) background.count(new BasicDBObject().append(
				"username", userName));
	}

	public List<ViewIcon> getByUser(String username) throws Exception {
		List<ViewIcon> icons = new ArrayList<ViewIcon>();
		Map<String, Object> cache = new HashMap<String, Object>();
		DBCursor find = background.find(new BasicDBObject().append("username",
				username));
		for (DBObject result : find) {
			icons.add(toObject(result, cache));
		}
		return icons;
	}

	public List<ViewIcon> getAll() throws Exception {
		List<ViewIcon> icons = new ArrayList<ViewIcon>();
		Map<String, Object> cache = new HashMap<String, Object>();
		DBCursor find = background.find(new BasicDBObject());
		for (DBObject result : find) {
			icons.add(toObject(result, cache));
		}
		return icons;
	}

	public int[] getWidthAndHeight(String iconName, String username) {
		DBObject result = background.findOne(new BasicDBObject().append(
				"username", username).append("name", iconName));
		int[] ret = new int[2];
		if (result != null) {
			ret[0] = (Integer) result.get("width");
			ret[1] = (Integer) result.get("height");
		}
		return ret;
	}

	private ViewIcon toObject(DBObject result, Map<String, Object> cache)
			throws Exception {
		if (result == null) {
			return null;
		}
		String id = ((ObjectId) result.get("_id")).toHexString();
		String md5 = (String) result.get("md5");
		String contentType = (String) result.get("contentType");
		String name = (String) result.get("name");
		String username = (String) result.get("username");
		int width = (Integer) result.get("width");
		int height = (Integer) result.get("height");
		Object content = null;
		if (cache != null)
			content = cache.get(md5);
		if (content == null) {
			DBObject iconData = viewIcon.findOne(new BasicDBObject().append(
					"md5", md5));
			if (iconData == null) {
				throw new Exception("icon不存在");
			}
			content = iconData.get("content");
			if (cache != null)
				cache.put(md5, content);
		}

		ViewIcon icon = new ViewIcon(name, username, contentType, content, md5);
		icon.setWidth(width);
		icon.setHeight(height);
		icon.setId(id);
		return icon;
	}

	public void deleteAll() {
		viewIcon.drop();
		background.drop();
	}

	public void deleteByUser(String username) {
		DBObject findOne = background.findOne(new BasicDBObject().append(
				"username", username));
		String md5 = (String) findOne.get("md5");
		DBCursor find = background.find(new BasicDBObject().append("md5", md5));
		if (find.size() == 1) {
			viewIcon.remove(new BasicDBObject().append("md5", md5));
		}
		background.remove(new BasicDBObject().append("username", username));
	}

	public void delete(String iconName, String username) {
		DBObject findOne = background.findOne(new BasicDBObject().append(
				"username", username).append("name", iconName));
		if (findOne != null) {
			String md5 = (String) findOne.get("md5");
			DBCursor find = background.find(new BasicDBObject().append("md5",
					md5));
			if (find.size() == 1) {
				viewIcon.remove(new BasicDBObject().append("md5", md5));
			}
			background.remove(new BasicDBObject().append("username", username)
					.append("name", iconName));
		}
	}

}
