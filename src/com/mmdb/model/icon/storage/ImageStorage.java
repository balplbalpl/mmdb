package com.mmdb.model.icon.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mmdb.model.bean.Page;
import com.mmdb.model.icon.ViewIcon;
import com.mmdb.mongo.MongoConnect;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

@Repository("imageStorage")
public class ImageStorage {
	private DB db = MongoConnect.getDb();
	DBCollection svg = db.getCollection("image.svg");

	/**
	 * 获取全部的主题图片
	 * 
	 * @return
	 */
	public List<ViewIcon> getAll() {
		List<ViewIcon> ret = new ArrayList<ViewIcon>();
		DBCursor cursor = svg.find();
		for (DBObject dbObject : cursor) {
			if(!dbObject.containsKey("timestamp")){
				ret.add(transform(dbObject));
			}
		}
		return ret;
	}

	public ViewIcon getById(String id) {
		DBObject data = svg.findOne(new BasicDBObject("_id", id));
		return transform(data);
	}

	public ViewIcon getByName(String theme, String name) {
		DBObject data = svg.findOne(new BasicDBObject("name", name).append(
				"theme", theme));
		return transform(data);
	}

	public Page<ViewIcon> fuzzyQuery(String theme, String name, int page,
			int pageSize) {
		Page<ViewIcon> content = new Page<ViewIcon>();

		DBCursor cursor = svg
				.find(new BasicDBObject("name", new BasicDBObject("$regex",
						name)).append("theme", theme))
				.skip((page - 1) * pageSize).limit(pageSize);
		cursor.count();
		List<ViewIcon> ret = new ArrayList<ViewIcon>();
		for (DBObject dbObject : cursor) {
			ret.add(transform(dbObject));
		}
		content.setCount(ret.size());
		content.setPageSize(pageSize);
		content.setStart(page);
		content.setTotalCount(cursor.count());
		content.setDatas(ret);
		return content;
	}

	private BasicDBObject asMap(ViewIcon img) {
		BasicDBObject data = new BasicDBObject();
		data.put("name", img.getName());
		data.put("theme", img.getUsername());
		data.put("contentType", img.getContentType());
		data.put("content", img.getContent());
		data.put("md5", img.getMd5());
		return data;
	}

	private ViewIcon transform(DBObject data1) {
		if (data1 == null || data1.keySet().size() == 0) {
			return null;
		}
		BasicDBObject data = (BasicDBObject) data1;
		ViewIcon img = new ViewIcon();
		img.setContent(data.get("content"));
		img.setContentType((String) data.get("contentType"));
		img.setId((data.getObjectId("_id").toString()));
		img.setMd5((String) data.get("md5"));
		img.setName((String) data.get("name"));
		img.setUsername((String) data.get("theme"));
		return img;
	}

	public void save(List<ViewIcon> icons) {
		DBObject[] datas = new DBObject[icons.size()];
		for (int i = 0; i < icons.size(); i++) {
			BasicDBObject asMap = asMap(icons.get(i));
			datas[i] = asMap;
		}
		svg.insert(datas);
		saveVersion();
	}

	public void saveVersion() {
		svg.remove(new BasicDBObject("timestamp", new BasicDBObject("$exists",
				true)));
		BasicDBObject timestamp = new BasicDBObject();
		timestamp.put("timestamp", new Date().getTime()+"");
		svg.insert(timestamp);
	}

	public String getVersion() {
		DBObject findOne = svg.findOne(new BasicDBObject("timestamp",
				new BasicDBObject("$exists", true)));
		if(findOne!=null){
			return (String) findOne.get("timestamp");
		}
		return null;
	}

	public void clear() {
		svg.drop();
	}
	
	public void deleteByName(String name){
		svg.remove(new BasicDBObject("name", name));
	}
}
