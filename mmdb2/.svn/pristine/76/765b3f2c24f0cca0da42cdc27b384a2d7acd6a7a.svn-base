package com.mmdb.rest.category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.bson.types.ObjectId;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.utils.JsonUtil;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.TypeFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.mapping.ISourceCategoryMapService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;
import com.mmdb.util.XmlUtil;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class CiCateRest extends BaseRest {

	private ICiCateService cateService;
	private ICiInfoService infoService;
	private ISourceCategoryMapService dbCateMapService;
	private IInCiCateMapService imService;
	private ISourceRelationMapService omService;
	private ITaskService taskService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(ICiCateService.class);

		infoService = context.getBean(ICiInfoService.class);

		dbCateMapService = context.getBean(ISourceCategoryMapService.class);

		imService = context.getBean(IInCiCateMapService.class);

		omService = context.getBean(ISourceRelationMapService.class);

		taskService = context.getBean(ITaskService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		if ("import".equals(param1)) {
			return new JsonRepresentation(importData(entity));
		}
		return saveCiCategory(params);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return editCiCategory(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll();
		} else {
			return deleteCateById(param1);
		}
	}

	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		List<CiCategory> all = cateService.getAll();
		JSONArray list = new JSONArray();
		for (CiCategory ciCate : all) {
			list.add(ciCate.asMapForRest());
		}
		ret.put("data", list);
		ret.put("message", "获取全部配置项分类成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取ciCategory
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		CiCategory ciCate = cateService.getById(id);
		if (ciCate != null) {
			ret.put("data", ciCate.asMapForRest());
			ret.put("message", "获取配置项数据[" + id + "]成功");
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导出XMl
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private Representation exportData() {
		JSONObject ret = new JSONObject();
		log.dLog("exportXML");
		File file = null;
		InputStream inStream = null;
		FileOutputStream outputStream = null;

		try {
			List<CiCategory> ncs = cateService.getAll();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (CiCategory nc : ncs) {
				list.add(nc.asMap());
			}
			inStream = XmlUtil.createCiCateXml(list);
			file = FileManager.getInstance().createFile("配置项分类-", "xml");
			file.createNewFile();
			outputStream = new FileOutputStream(file);
			IoUtils.copy(inStream, outputStream);
			ret.put("message", "下载配置项分类成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);

		} catch (Exception e) {
			e.printStackTrace();
			if (file != null && file.exists()) {
				file.delete();
			}
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入XML
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private String importData(Representation entity) throws Exception {
		log.dLog("importXML");
		JSONObject ret = new JSONObject();
		//
		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);
		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			log.eLog(e);
		}
		String filename = "";
		FileItem fi = items.get(0);
		filename = fi.getName();
		if (filename == null || filename.equals("")
				|| filename.toLowerCase().trim().indexOf("xml") == -1) {
			log.eLog("文件格式有误");
			throw new Exception("文件格式有误");
		}
		int num = 0;
		taskService.deleteAll();
		imService.deleteAll();
		dbCateMapService.deleteAll();
		cateService.clearAll();

		String username = getUsername();
		// key:用户名,value:true存在,false不存在
		Map<String, Boolean> users = new HashMap<String, Boolean>();
		users.put(username, true);
		WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
		if(wsm!=null){
			wsm.broadcast("正在解析XML，请等待...", 0);
		}
//		MessageResult.broadcast("正在解析XML，请等待...", 0);
		Map<String, Map<String, Object>> xMap = XmlUtil.parserCiCateXml(fi
				.getInputStream());
		// 确认上传的文件是否为老的版本
		boolean old = false;
		int size = xMap.size();
		final double flag = 1d / size;
		double progress = 0d;
		try {
			String key = (String) xMap.keySet().toArray()[0];
			new ObjectId(key);
		} catch (Exception e) {
			old = true;
		}

		CiCategory category;
		Iterator<Entry<String, Map<String, Object>>> iter = xMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, Object>> entry = iter.next();
			String key = entry.getKey();
			Map<String, Object> val = entry.getValue();
			String name = String.valueOf(val.get("name"));
			if(wsm!=null){
				wsm.broadcast("解析分类[" + name + "]...", progress);
			}
//			MessageResult.broadcast("解析分类[" + name + "]...", progress);
			progress += flag;
			String image = String.valueOf(val.get("image"));
			String clientId = String.valueOf(val.get("clientId"));
			String parentId = String.valueOf(val.get("parent"));
			String owner = String.valueOf(val.get("owner"));
			Map<String, Map<String, Object>> selfAttrs = (Map<String, Map<String, Object>>) val
					.get("selfAttrs");
			if (old) {
				category = new CiCategory(null, name);
			} else {
				category = new CiCategory(key, name);
			}
			if (parentId != null && !"".equals(parentId)) {
				CiCategory pc = new CiCategory(parentId, parentId);
				category.setParent(pc);
				category.setParentId(pc.getId());
			}

			List<Attribute> attrs = category.getAttributes();
			Attribute attr;

			Object ownMajor = val.get("ownMajor");

			Iterator<Entry<String, Map<String, Object>>> iter2 = selfAttrs
					.entrySet().iterator();
			while (iter2.hasNext()) {
				Entry<String, Map<String, Object>> sentry = iter2.next();
				String skey = sentry.getKey();
				Map<String, Object> attribute = sentry.getValue();
				String atype = attribute.get("type").toString();
				if (!TypeFactory.getTypes().containsKey(atype)) {
					throw new Exception("分类[" + key + "]中属性[" + skey
							+ "]的属性类型[" + atype + "]不符合要求");
				}
				String le = attribute.get("level") != null ? attribute.get(
						"level").toString() : "";
				boolean level = Boolean.valueOf(le);
				boolean hide = Boolean
						.valueOf(attribute.get("hide").toString());
				boolean required = Boolean.valueOf(attribute.get("required")
						.toString());
				String defaultVal = attribute.get("defaultVal").toString();
				List<String> sources = JsonUtil.decodeByJackSon(
						attribute.get("sources").toString(), List.class);
				attr = new Attribute(skey, TypeFactory.getType(atype), hide,
						required, level, defaultVal, sources);
				if (!attrs.contains(attr)) {
					attrs.add(attr);
				}
				if (skey.equals(clientId)) {
					category.setClientId(attr);
				}
				if (ownMajor != null && !ownMajor.equals("")
						&& ownMajor.equals(skey)) {
					category.setOwnMajor(attr);
				}
			}
			category.setAttributes(attrs);
			category.setImage(image);
			if (owner == null || "".equals(owner)) {
				owner = username;
			} else {
				Boolean boolean1 = users.get(owner);
				if (boolean1 == null) {
					User ownerUser = userService.getUserByLoginName(owner);
					boolean1 = ownerUser != null;
					users.put(owner, boolean1);
				}
				if (!boolean1) {
					log.dLog("添加分类[" + category.getName() + "],所有者[" + owner
							+ "]不存在,默认使用当前用户[" + username + "]");
					owner = username;
				}
			}
			category.setOwner(owner);
			cateService.save(category);
			num++;
		}

		if (old) {
			// 老数据将父类修改下
			List<CiCategory> all = cateService.getAll();

			Map<String, CiCategory> a = new HashMap<String, CiCategory>();
			for (CiCategory ciCategory : all) {
				a.put(ciCategory.getName(), ciCategory);
			}
			for (CiCategory ciCategory : all) {
				String parentId = ciCategory.getParentId();
				if (parentId != null && !"".equals(parentId)) {
					CiCategory ciCategory2 = a.get(parentId);
					ciCategory.setParentId(ciCategory2.getId());
					ciCategory.setParent(ciCategory2);
				}
				cateService.update(ciCategory);
			}
		}
		if(wsm!=null){
			wsm.broadcast("上传分类完成", 1d);
			wsm.broadcast("成功导入分类[" + num + "]条", 1d);
		}
//		MessageResult.broadcast("上传分类完成", 1d);
//		MessageResult.broadcast("成功导入分类[" + num + "]条", 1d);
		return ret.toString();
	}

	private JsonRepresentation saveCiCategory(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String cateName = null;
		String parentId = null;
		String ownMajorName = null;
		String image = null;
		String clientId = null;
		String owner = getUsername();
		JSONArray jsonArray = null;
		try {
			cateName = data.getString("name");
			parentId = data.getString("parent");
			ownMajorName = data.getString("ownMajor");
			image = data.getString("image");
			clientId = data.getString("clientId");
			jsonArray = data.getJSONArray("attributes");
		} catch (Exception e1) {
			throw e1;
		}

		if (image != null) {
			int t = image.lastIndexOf('/');
			if (t != -1)
				image = image.substring(t + 1, image.length());
		}

		List<Attribute> attrs = new ArrayList<Attribute>();
		Attribute ownMajor = null;
		Attribute clientIdAttr = null;
		for (Object object : jsonArray) {
			JSONObject attr = (JSONObject) object;
			boolean hide = attr.getBoolean("hide");
			boolean level = attr.getBoolean("level");
			boolean required = attr.getBoolean("required");
			String name = attr.getString("name");
			String type = attr.getString("type");
			String defaultVal = attr.getString("defaultVal");
			JSONArray aSources = attr.getJSONArray("sources");
			List<String> sources = new ArrayList<String>();
			for (int i = 0; i < aSources.size(); i++) {
				sources.add(aSources.getString(i));
			}
			Attribute tmp = null;
			tmp = new Attribute(name, TypeFactory.getType(type), hide,
					required, level, defaultVal, sources);
			if (name.equals(clientId)) {
				clientIdAttr = tmp;
			}
			if (name.equals(ownMajorName)) {
				ownMajor = tmp;
			} else {
				attrs.add(tmp);
			}
		}

		if (ownMajorName != null && !"".equals(ownMajorName)) {
			if (ownMajor == null) {
				throw new MException("属性[" + ownMajorName + "]在分类中不存在");
			}
		}
		if (cateName == null || "".equals(cateName)) {
			throw new MException("分类名称不能为空");
		} else {
			CiCategory nCategory;
			if (cateService.getByName(cateName) != null) {
				throw new MException("分类名称[" + cateName + "]已存在");
			}

			if (parentId == null || parentId.equals("")) {
				nCategory = new CiCategory(null, cateName);
				attrs.add(ownMajor);
				nCategory.setOwnMajor(ownMajor);
			} else {
				CiCategory parentCate = cateService.getById(parentId);
				if (parentCate == null) {
					throw new MException("父类不存在");
				}
				List<Attribute> allAttributes = parentCate.getAllAttributes();
				for (Attribute attribute : allAttributes) {
					for (Attribute nAttr : attrs) {
						if (nAttr.getName().equals(attribute.getName())) {
							throw new MException("分类[" + cateName + "]属性["
									+ nAttr.getName() + "]已存在");
						}
					}
				}
				nCategory = new CiCategory(null, cateName, parentCate);
				if (image == null || "".equals(image)) {
					image = parentCate.getImage();
				}
				if (clientIdAttr == null) {
					clientIdAttr = parentCate.getClientId();
				}
			}
			if (clientIdAttr == null) {
				clientIdAttr = ownMajor;
			}
			nCategory.setClientId(clientIdAttr);
			nCategory.setImage(image);
			nCategory.setAttributes(attrs);
			nCategory.setOwner(owner);
			CiCategory nc = cateService.save(nCategory);

			ret.put("data", nc.asMap());
			ret.put("message", "保存配置项分类[" + cateName + "]成功");
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation editCiCategory(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();

		String cateId = null;
		String parentId = null;
		String ownMajorName = null;
		String image = null;
		String clientId = null;
		JSONArray jsonArray = null;
		try {
			cateId = data.getString("id");
			parentId = data.getString("parent");
			ownMajorName = data.getString("ownMajor");
			image = data.getString("image");
			clientId = data.getString("clientId");
			jsonArray = data.getJSONArray("attributes");
		} catch (Exception e1) {
			throw e1;
		}

		if (image != null) {
			int t = image.lastIndexOf('/');
			if (t != -1)
				image = image.substring(t + 1, image.length());
		}

		List<Attribute> attrs = new ArrayList<Attribute>();

		List<Attribute> addAttrs = new ArrayList<Attribute>();
		List<Attribute> delAttrs = new ArrayList<Attribute>();
		Map<String, Attribute> editAttribute = new HashMap<String, Attribute>();

		Attribute ownMajor = null;
		Attribute clientIdAttr = null;
		// 传入的属性中有个edit字段,保存的老的名字,这时候用老属性名称作为key

		for (Object object : jsonArray) {
			JSONObject attr = (JSONObject) object;
			boolean hide = attr.getBoolean("hide");
			boolean level = attr.getBoolean("level");
			boolean required = attr.getBoolean("required");
			String name = attr.getString("name");
			String type = attr.getString("type");
			String defaultVal = attr.getString("defaultVal");
			String oldName = null;
			try {
				oldName = attr.getString("edit");
			} catch (Exception e) {
			}
			JSONArray aSources = attr.getJSONArray("sources");
			List<String> sources = new ArrayList<String>();
			for (int i = 0; i < aSources.size(); i++) {
				sources.add(aSources.getString(i));
			}
			Attribute tmp = null;
			tmp = new Attribute(name, TypeFactory.getType(type), hide,
					required, level, defaultVal, sources);
			if (name.equals(clientId)) {
				clientIdAttr = tmp;
			}
			if (name.equals(ownMajorName)) {
				ownMajor = tmp;
			}
			attrs.add(tmp);
			if (oldName != null) {
				editAttribute.put(oldName, tmp);
			}
		}// 准备参数结束

		if (ownMajorName != null && !"".equals(ownMajorName)) {
			if (ownMajor == null) {
				throw new MException("属性[" + ownMajorName + "]在分类中不存在");
			}
		}

		if (cateId == null || "".equals(cateId)) {
			throw new MException("分类名称不能为空");
		}
		CiCategory nCategory = cateService.getById(cateId);
		if (nCategory == null) {
			throw new MException("分类[" + cateId + "]不存在");
		}
		if (parentId != null && !"".equals(parentId)) {
			if (!parentId.equals(nCategory.getParent().getId())) {
				throw new MException("分类的父类不能修改");
			}
		}

		Set<String> keySet = editAttribute.keySet();

		List<Attribute> selfAttributes = nCategory.getAttributes();
		List<Attribute> allAttributes = nCategory.getAllAttributes();
		if (clientIdAttr == null) {
			for (Attribute attribute : allAttributes) {
				if (attribute.getName().equals(clientId)) {
					clientIdAttr = attribute;
					break;
				}
			}
		}
		for (String oldName : keySet) {
			Attribute attribute = editAttribute.get(oldName);
			for (Attribute oldAttr : selfAttributes) {
				if (oldName.equals(oldAttr.getName())
						&& !oldName.equals(attribute.getName())) {
					selfAttributes.remove(oldAttr);
					break;
				}
			}
		}

		for (Attribute nAttr : attrs) {
			boolean exist = false;
			for (int i = 0, len = selfAttributes.size(); i < len; i++) {
				Attribute selfAttr = selfAttributes.get(i);
				if (nAttr.getName().equals(selfAttr.getName())) {
					exist = true;
					selfAttributes.remove(i);
					break;
				}
			}
			if (!exist) {
				for (Attribute pa : allAttributes) {
					if (pa.getName().equals(nAttr.getName())) {
						throw new MException("属性[" + nAttr.getName() + "]已存在");
					}
				}
				addAttrs.add(nAttr);
			}
		}
		for (String oldName : keySet) {
			addAttrs.remove(editAttribute.get(oldName));
		}
		delAttrs.addAll(selfAttributes);

		nCategory.setAttributes(attrs);
		// 主键和父类不能修改所以不设置
		nCategory.setClientId(clientIdAttr);
		nCategory.setImage(image);

		CiCategory update = cateService.update(nCategory);

		if (editAttribute.size() > 0)
			infoService.alterAttr(update, editAttribute);
		if (addAttrs.size() > 0)
			infoService.addAttr(update, addAttrs);
		if (delAttrs.size() > 0)
			infoService.deleteAttr(update, delAttrs);

		ret.put("data", update.asMapForRest());
		ret.put("message", "编辑成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除全部的ciCate和ci
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteAll() throws Exception {
		JSONObject ret = new JSONObject();
		cateService.clearAll();
		ret.put("message", "清除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除ciCate和ciCate下的全部ci
	 * 
	 * @param cateId
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteCateById(String cateId) throws Exception {
		JSONObject ret = new JSONObject();
		if (cateId == null || cateId.equals("")) {
			throw new MException("参数分类ID不能为空");
		}
		CiCategory nc = cateService.getById(cateId);
		if (nc == null) {
			throw new MException("分类[" + cateId + "]不存在");
		}
		if (nc.getChildren().size() > 0) {
			throw new MException("此分类有继承的子类，需要先删除子类");
		}
		if (dbCateMapService.getMappingByCategory(nc).size() > 0) {
			throw new MException("该分类已被映射，需要先删除数据库映射");
		}
		if (imService.getMapsByCate(nc).size() > 0) {
			throw new MException("该分类已被映射，需要先删除内部映射");
		}
		if (omService.getMapsByCate(nc).size() > 0) {
			throw new MException("该分类已被映射，需要先删除外部映射");
		}
		cateService.delete(nc, true);
		ret.put("message", "删除分类[" + nc.getName() + "]成功");
		return new JsonRepresentation(ret.toString());
	}
}
