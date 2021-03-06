package com.mmdb.rest.category;

import java.io.File;
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
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.utils.JsonUtil;
//import com.mmdb.core.utils.XmlUtil;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.TypeFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.relation.ICiRelService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;
import com.mmdb.util.XmlUtil;
//
///**
// * 关系分类的存储仓库
// * 
// * @author XIE
// */
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class RelCateRest extends BaseRest {
	private IRelCateService relCateService;
	private ICiRelService relService;
	private IInCiCateMapService imService;
	private ISourceRelationMapService ocmService;
	private ITaskService taskService;

	@Override
	public void ioc(ApplicationContext context) {
		relCateService = context.getBean(IRelCateService.class);

		relService = context.getBean(ICiRelService.class);

		imService = context.getBean(IInCiCateMapService.class);

		ocmService = context.getBean(ISourceRelationMapService.class);

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
		if ("import".equals(param1)) {
			return new JsonRepresentation(importData(entity));
		}
		JSONObject params = parseEntity(entity);
		if (params.containsKey("name") && params.containsKey("parent")
				&& params.containsKey("attributes")
				&& params.containsKey("image")) {
			return saveRelCategory(params);
		}
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		if (params.containsKey("id") && params.containsKey("parent")
				&& params.containsKey("attributes")
				&& params.containsKey("image")) {
			return editRelCategory(params);
		}
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll();
		} else {// 删除一个属性
			return deleteCateById(param1);
		}
	}

	private Representation deleteCateById(String rCateId) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteCategory");
		if (rCateId == null || rCateId.equals("")) {
			throw new Exception("参数分类ID不能为空");
		}
		RelCategory nc = relCateService.getById(rCateId);
		if (nc == null) {
			throw new MException("关系分类[" + rCateId + "不存在]");
		}
		if (nc.getChildren().size() > 0) {
			throw new MException("此分类有继承的子类，需要先删除子类");
		}
		String nid = nc.getName();
		List<String> irs = imService.getRelCateIds();
		if (irs.contains(nid)) {
			throw new MException("此分类已被配置项外键映射，需要先删除映射");
		}
		List<String> ocs = ocmService.getRelCateIds();
		if (ocs.contains(nid)) {
			throw new MException("此分类已被配置项外表映射，需要先删除映射");
		}
		relCateService.delete(nc);// 再删除RelCate

		ret.put("message", "删除分类成功");

		log.dLog("deleteCategory success");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll() throws Exception {
		JSONObject ret = new JSONObject();
		imService.deleteAll();
		ocmService.deleteAll();
		relCateService.deleteAll();
		ret.put("message", "删除全部分类成功");
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
		try {
			List<RelCategory> rcs = relCateService.getAll();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (RelCategory nc : rcs) {
				list.add(nc.asMap());
			}
			// 创建xml文件流
			inStream = XmlUtil.createRelXml(list);

			file = FileManager.getInstance()
					.createFile(inStream, "关系分类", "xml");

			ret.put("message", "下载关系分类成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
		} catch (Exception e) {
			log.eLog("获取全部配置项失败", e);
			if (file != null) {
				file.delete();
			}
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
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
	 */
	@SuppressWarnings("unchecked")
	private String importData(Representation entity) {
		log.dLog("importXML");
		JSONObject ret = new JSONObject();

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
		try {
			filename = fi.getName();
			// filename = new String(filename.getBytes("GBK"), "utf-8");
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf("xml") == -1) {
				log.eLog("文件格式有误");
				throw new Exception("文件格式有误");
			}
			int num = 0;
			taskService.deleteAll();
			imService.deleteAll();
			ocmService.deleteAll();
			relCateService.deleteAll();
			relService.deleteAll();
			WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
			if(wsm!=null){
				wsm.broadcast("正在解析XML，请等待...", 0d);
			}
//			MessageResult.broadcast("正在解析XML，请等待...", 0d);
			Map<String, Map<String, Object>> xMap = XmlUtil.parserRelCateXml(fi
					.getInputStream());

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
			String username = getUsername();
			// key:用户名,value:true存在,false不存在
			Map<String, Boolean> users = new HashMap<String, Boolean>();
			users.put(username, true);
			
			RelCategory category;
			Iterator<Entry<String, Map<String, Object>>> iter = xMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<String, Map<String, Object>> entry = iter.next();
				String key = entry.getKey();
				Map<String, Object> val = entry.getValue();
				String name = String.valueOf(val.get("name"));
				String owner = String.valueOf(val.get("owner"));
				if(wsm!=null){
					wsm.broadcast("解析分类[" + name + "]...", progress);
				}
//				MessageResult.broadcast("解析分类[" + name + "]...", progress);
				progress += flag;
				String parentId = String.valueOf(val.get("parent"));
				Map<String, Map<String, Object>> selfAttrs = (Map<String, Map<String, Object>>) val
						.get("selfAttrs");
				if (old) {
					category = new RelCategory(null, name);
				} else {
					category = new RelCategory(key, name);
				}

				if (parentId != null && !"".equals(parentId)) {
					RelCategory parent = new RelCategory(parentId, parentId);
					category.setParent(parent);
					category.setParentId(parentId);
				}
				List<Attribute> attrs = category.getAttributes();
				Attribute attr;
				Iterator<Entry<String, Map<String, Object>>> iter2 = selfAttrs
						.entrySet().iterator();
				while (iter2.hasNext()) {
					Entry<String, Map<String, Object>> sentry = iter2.next();
					String skey = sentry.getKey();
					Map<String, Object> attribute = sentry.getValue();
					String atype = attribute.get("type").toString();
					if (!TypeFactory.getTypes().containsKey(atype)) {
						throw new Exception("分类[" + name + "]" + "属性[" + skey
								+ "]属性类型[" + atype + "]不符合要求");
					}
					boolean hide = Boolean.valueOf(attribute.get("hide")
							.toString());
					boolean required = Boolean.valueOf(attribute
							.get("required").toString());
					String le = attribute.get("level") != null ? attribute.get(
							"level").toString() : "";
					boolean level = Boolean.valueOf(le);
					String defaultVal = attribute.get("defaultVal").toString();
					List<String> sources = JsonUtil.decodeByJackSon(attribute
							.get("sources").toString(), List.class);
					attr = new Attribute(skey, TypeFactory.getType(atype),
							hide, required, level, defaultVal, sources);
					if (!attrs.contains(attr)) {
						attrs.add(attr);
					}
				}
				category.setAttributes(attrs);
				
				
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
				
				
				if (old) {
					relCateService.save(category);
				} else {
					relCateService.saveHasId(category);
				}
				num++;
			}

			if(wsm!=null){
				wsm.broadcast("上传分类完成", 1d);
				wsm.broadcast("成功导入分类[" + num + "]条", 1d);
			}
//			MessageResult.broadcast("上传分类完成", 1d);
//			MessageResult.broadcast("成功导入分类[" + num + "]条", 1d);
			if (old) {
				List<RelCategory> all = relCateService.getAll();
				Map<String, RelCategory> cache = new HashMap<String, RelCategory>();
				for (RelCategory relCategory : all) {
					cache.put(relCategory.getName(), relCategory);
				}
				for (RelCategory relCategory : all) {
					String parentId = relCategory.getParentId();
					if (parentId != null && !"".equals(parentId)) {
						RelCategory relCategory2 = cache.get(parentId);
						relCategory.setParent(relCategory2);
						relCategory.setParentId(relCategory2.getId());
					}
					relCateService.update(relCategory);
				}
			}

			log.dLog("importXML success");
		} catch (Exception e) {
			log.eLog(e);
			getResponse().setStatus(new Status(600));
			ret.put("message", e.getMessage());
		}
		return ret.toString();
	}

	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getById [" + id + "]");
		RelCategory relCategory = relCateService.getById(id);
		if (relCategory != null) {
			ret.put("data", relCategory.asMapForRest());
			ret.put("message", "获取关系分类成功");
		} else {
			throw new MException("获取关系分类失败");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getAll");
		List<RelCategory> list = relCateService.getAll();
		JSONArray data = new JSONArray();
		for (RelCategory relCate : list) {
			data.add(relCate.asMapForRest());
		}
		ret.put("data", data);
		ret.put("message", "获取全部关系分类成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation saveRelCategory(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String cateName = null;
		String parentId = null;
		String image = null;
		JSONArray jsonArray = null;
		try {
			cateName = data.getString("name");
			parentId = data.getString("parent");
			image = data.getString("image");
			jsonArray = data.getJSONArray("attributes");
		} catch (Exception e1) {
			throw e1;
		}

		String relType = null;
		try {
			relType = data.getString("type");
		} catch (Exception e) {
		}
		if (!"direct".equals(relType) && !"indirect".equals(relType)) {
			relType = "direct";
		}

		List<Attribute> attrs = new ArrayList<Attribute>();
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
			attrs.add(tmp);
		}

		log.dLog("save");
		if (cateName == null || "".equals(cateName)) {
			throw new MException("分类名称不能为空");
		} else {
			RelCategory nCategory;
			if (relCateService.getByName(cateName) != null) {
				throw new MException("分类名称[" + cateName + "]已存在");
			}
			if (parentId == null || parentId.equals("")) {
				nCategory = new RelCategory(cateName, cateName);
			} else {
				RelCategory parentCate = relCateService.getById(parentId);
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
				nCategory = new RelCategory(cateName, cateName, parentCate);
				if (image == null || "".equals(image)) {
					image = parentCate.getImage();
				}
			}
			nCategory.setImage(image);
			nCategory.setAttributes(attrs);
			nCategory.setType(relType);
			nCategory.setOwner(getUsername());
			RelCategory nc = relCateService.save(nCategory);

			ret.put("data", nc.asMap());
			ret.put("message", "保存配置项分类[" + cateName + "]成功");
		}
		log.dLog("save success");
		return new JsonRepresentation(ret.toString());
	}

	private Representation editRelCategory(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String cateId = null;
		String parentId = null;
		JSONArray jsonArray = null;
		String image = null;

		try {
			cateId = data.getString("id");
			parentId = data.getString("parent");
			image = data.getString("image");
			jsonArray = data.getJSONArray("attributes");
		} catch (Exception e1) {
			throw e1;
		}

		String relType = null;
		try {
			relType = data.getString("type");
		} catch (Exception e) {
		}

		if (!"direct".equals(relType) && !"indirect".equals(relType)) {
			relType = "direct";
		}

		List<Attribute> attrs = new ArrayList<Attribute>();
		List<Attribute> addAttrs = new ArrayList<Attribute>();
		List<Attribute> delAttrs = new ArrayList<Attribute>();
		Map<String, Attribute> editAttribute = new HashMap<String, Attribute>();

		for (Object object : jsonArray) {
			JSONObject attr = (JSONObject) object;
			boolean hide = attr.getBoolean("hide");
			boolean level = attr.getBoolean("level");
			boolean required = attr.getBoolean("required");
			String name = attr.getString("name");
			String type = attr.getString("type");
			String defaultVal = attr.getString("defaultVal");
			JSONArray aSources = attr.getJSONArray("sources");

			String oldName = null;
			try {
				oldName = attr.getString("edit");
			} catch (Exception e) {
			}

			List<String> sources = new ArrayList<String>();
			for (int i = 0; i < aSources.size(); i++) {
				sources.add(aSources.getString(i));
			}
			Attribute tmp = null;
			tmp = new Attribute(name, TypeFactory.getType(type), hide,
					required, level, defaultVal, sources);
			attrs.add(tmp);

			if (oldName != null) {
				editAttribute.put(oldName, tmp);
			}
		}
		// 准备工作

		log.dLog("edit");
		if (cateId == null || "".equals(cateId)) {
			throw new MException("分类名称不能为空");
		} else {
			RelCategory nCategory = relCateService.getById(cateId);
			if (nCategory == null) {
				throw new MException("分类不存在");
			}
			// RelCategory parent = nCategory.getParent();
			if (parentId != null && !"".equals(parentId))
				if (!parentId.equals(nCategory.getParent().getId())) {
					throw new MException("分类的父类id不能修改");
				}

			Set<String> keySet = editAttribute.keySet();

			List<Attribute> selfAttributes = nCategory.getAttributes();
			List<Attribute> allAttributes = nCategory.getAllAttributes();

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
							throw new MException("属性[" + nAttr.getName()
									+ "]已存在");
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
			nCategory.setImage(image);
			RelCategory update = relCateService.update(nCategory);

			List<CiRelation> rels = relService.qureyByAdvanced(nCategory, null,
					null, true);
			Set<String> keySet2 = editAttribute.keySet();

			for (CiRelation rel : rels) {
				Dynamic<String, Object> infoData = rel.getRelValue();

				for (String oldName : keySet2) {
					Attribute attribute = editAttribute.get(oldName);

					Object object = infoData.get(oldName);
					infoData.remove(oldName);
					infoData.put(attribute.getName(), object);
				}

				for (Attribute attr : addAttrs) {
					infoData.put(attr.getName(), attr.getDefaultValue());
				}
				for (Attribute attr : delAttrs) {
					infoData.remove(attr.getName());
				}
			}

			relService.update(rels);

			List<InCiCateMap> ims = imService.getByRelCate(nCategory);
			for (InCiCateMap im : ims) {
				Map<String, Object> rv = im.getRelValue();

				for (String oldName : keySet2) {
					Attribute attribute = editAttribute.get(oldName);

					Object object = rv.get(oldName);
					rv.remove(oldName);
					rv.put(attribute.getName(), object);
				}

				for (Attribute attr : delAttrs) {
					rv.remove(attr.getName());
				}
				for (Attribute attr : addAttrs) {
					rv.put(attr.getName(), attr.getDefaultValue());
				}
				im.setRelValue(rv);
			}
			imService.update(ims);

			List<SourceToRelationMapping> oms = ocmService
					.getByRelCate(nCategory);
			for (SourceToRelationMapping om : oms) {
				Map<String, Object> rv = om.getRelValue();

				for (String oldName : keySet2) {
					Attribute attribute = editAttribute.get(oldName);

					Object object = rv.get(oldName);
					rv.remove(oldName);
					rv.put(attribute.getName(), object);
				}

				for (Attribute attr : delAttrs) {
					rv.remove(attr.getName());
				}
				for (Attribute attr : addAttrs) {
					rv.put(attr.getName(), attr.getDefaultValue());
				}
				om.setRelValue(rv);
				ocmService.update(om);
			}
			ret.put("message", "编辑关系分类[" + update.getName() + "]成功");
		}
		return new JsonRepresentation(ret.toString());
	}

}