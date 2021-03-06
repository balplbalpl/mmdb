package com.mmdb.rest.category;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.engine.io.IoUtils;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.util.FileManager;
import com.mmdb.util.XmlUtil;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class KpiCateRest extends BaseRest {
	private Log log = LogFactory.getLogger("KpiCateRest");
	private IKpiCateService cateService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(IKpiCateService.class);
		setExisting(true);
	}

	@Override
	public Representation getHandler()  throws Exception{
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		}else if ("owner".equals(param1)) {
			return getByUser();
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity)  throws Exception{
		String param1 = getValue("param1");

		if ("import".equals(param1)) {
			return importData(entity);
		}
		JSONObject params = parseEntity(entity);
		if ("getbyname".equals(param1)) {
			return getByName(params);
		}else{
			return saveKpiCategory(params);
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception{
		JSONObject params = parseEntity(entity);
		return editKpiCategory(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception{
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			//return deleteAll(); 界面上没有提供删除ALL的接口
			return new JsonRepresentation("");
		} else {
			return deleteCateById(param1);
		}
	}
	
	/**
	 * 保存KPI分类信息
	 * 
	 * @param obj
	 * @return 
	 */
	private Representation saveKpiCategory(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		log.dLog("saveKpiCategory");

		String image = null;
		String name = null;
		String parentId = null;
		try{
			name = obj.getString("name");
		} catch (Exception e1) {
			log.eLog("save", e1);
			throw new MException("必须参数[name]不存在");
		}
		try {
			parentId = obj.getString("parent");
		} catch (Exception e1) {
			log.eLog("save", e1);
			throw new MException("必须参数[parent]不存在");
		}
		try {
			image = obj.getString("image");
		} catch (Exception e1) {
			log.eLog("save", e1);
			throw new MException("必须参数[image]不存在");
		}

		if (image != null) {
			int t = image.lastIndexOf('/');
			if (t != -1)
				image = image.substring(t + 1, image.length());
		}

		try {	
			KpiCategory k = cateService.getByName(name);
			if(k!=null){
				throw new MException(name+"分类已存在");
			}
			KpiCategory kpiCate = new KpiCategory();
			kpiCate.setName(name);

			if (parentId != null && !("".equals(parentId))) {
				kpiCate.setParentId(parentId);
			}else{
				kpiCate.setParentId("");
			}
			kpiCate.setImage(image);
			//modify at 2015-9-10，新增创建者字段
			kpiCate.setOwner(this.getUsername());
			
			kpiCate = cateService.save(kpiCate);
			Map<String, Object> asMap = kpiCate.toMap();
			ret.put("data", asMap);
			ret.put("message", "保存成功");
		} catch (Exception e) { 
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception{
		JSONObject ret = new JSONObject();
		try {
			List<KpiCategory> all = cateService.getAll();
			
			//String prefix = Tool.findPath("admin", "resource");
			//String type = SysProperties.get("svg.base");
			
			JSONArray list = new JSONArray();
			for (KpiCategory kpiCate : all) {
				Map<String, Object> asMap = kpiCate.toMapForRest();
				
/*				asMap.put("text", asMap.get("name"));
				asMap.put("icon",
						prefix + "/svg/" + type + "/" + asMap.get("icon"));*/
				
				list.add(asMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部KPI分类数据成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getByUser() throws Exception{
		JSONObject ret = new JSONObject();
		String userName = this.getUsername();
		try {
			List<KpiCategory> kpiCateList =
					cateService.getKpiCateByUserName(userName);
			JSONArray list = new JSONArray();
			for (KpiCategory kpiCate : kpiCateList) {
				Map<String, Object> asMap = kpiCate.toMapForRest();
				
				list.add(asMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部KPI分类数据成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取kpiCategory
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id)  throws Exception{
		JSONObject ret = new JSONObject();
		try {
			KpiCategory kpiCate = cateService.getById(id);
			if (kpiCate != null) {
				Map<String, Object> asMap = kpiCate.toMap();
				
				ret.put("data", asMap);
				ret.put("message", "获取KPI分类[" + id + "]成功");
			} else {
				//ret.put("message", "获取KPI分类[" + id + "]失败");
				//getResponse().setStatus(new Status(600));
				throw new Exception("获取KPI分类[" + id + "]失败");
			}
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	private Representation getByName(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		String name = "";
		try {
			name = obj.getString("name");
			KpiCategory kpiCate = cateService.getByName(name);
			if (kpiCate != null) {
				Map<String, Object> asMap = kpiCate.toMap();
				ret.put("data", asMap);
				ret.put("message", "获取KPI分类[" + name + "]成功");
			} else {
/*				ret.put("message", "获取KPI分类[" + name + "]失败");
				getResponse().setStatus(new Status(600));*/
				throw new Exception("获取KPI分类[" + name + "]失败");
			}
		} catch (Exception e) {
			log.eLog(e);
			throw e;
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
	private Representation exportData() throws Exception{
		JSONObject ret = new JSONObject();
		log.dLog("exportXML");

		File file = null;
		InputStream inStream = null;
		FileOutputStream outputStream = null;

		try {
			List<KpiCategory> ncs = cateService.getAll();
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for (KpiCategory nc : ncs) {
				list.add(nc.toMap());
			}
			inStream = XmlUtil.createKpiCateXml(list);
			file = FileManager.getInstance().createFile("Kpi-配置项分类-", "xml");
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
			throw e;
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
	 */
	private JsonRepresentation importData(Representation entity) throws Exception{
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
		String userName = this.getUsername();
		String filename = "";
		FileItem fi = items.get(0);
		
		try {
			filename = fi.getName();
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf("xml") == -1) {
				log.eLog("文件格式有误");
				throw new Exception("文件格式有误");
			}
			int num = 0;
			
			// 先删除kpi category 以及 kpi
			cateService.deleteAll();
			cateService.deleteAllKpi();
			Map<String, Map<String, Object>> xMap = XmlUtil.parserKpiCateXml(fi
					.getInputStream());
			int size = xMap.size();
			final double flag = 1d / size;
			double progress = 0d;
			WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
			if(wsm!=null){
				wsm.broadcast("正在解析XML，请等待...", 0);
			}
//			MessageResult.broadcast("正在解析XML，请等待...", 0);
			KpiCategory category;
			Iterator<Entry<String, Map<String, Object>>> iter = xMap.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<String, Map<String, Object>> entry = iter.next();
				String key = entry.getKey();
				Map<String, Object> val = entry.getValue();
				String name = String.valueOf(val.get("name"));
				String image = String.valueOf(val.get("image"));
				String parentId = String.valueOf(val.get("parent"));
				String owner = String.valueOf(val.get("owner"));
				
				//如果owner为空，以当前用户作为owner
				if(owner==null || "".equals(owner)){
					owner = userName;
				}
				
				if(wsm!=null){
					wsm.broadcast("解析分类[" + name + "]...", progress);
				}
//				MessageResult.broadcast("解析分类[" + name + "]...", progress);
				progress += flag;
				category = new KpiCategory();
				category.setId(key);
				category.setName(name);
				category.setImage(image);
				category.setOwner(owner);
				if (parentId != null && !("".equals(parentId))) {
					category.setParentId(parentId);
				}else{
					category.setParentId("");
				}
				cateService.save(category);
				num++;
			}
			if(wsm!=null){
				wsm.broadcast("上传分类完成", 1d);
				wsm.broadcast("成功导入分类[" + num + "]条", 1d);
			}
//			MessageResult.broadcast("上传分类完成", 1d);
//			MessageResult.broadcast("成功导入分类[" + num + "]条", 1d);
			log.dLog("importXML success");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation editKpiCategory(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.dLog("editKpiCategory");
			if (obj == null || obj.size() == 0) {
				throw new Exception("editKpiCategory参数不能空");
			}
			String id = obj.getString("id");
			KpiCategory kpiCate = cateService.getById(id);
			if (kpiCate == null) {
				throw new Exception("KpiCategory[" + id + "]不存在");
			}
			String image = obj.getString("image");
			//名称和父类不能编辑
			//kpiCate.setName(obj.containsKey("name") ? obj.getString("name"):"");
			if (image != null) {
				int t = image.lastIndexOf('/');
				if (t != -1)
					image = image.substring(t + 1, image.length());
			}
			kpiCate.setImage(image);
			kpiCate = cateService.update(kpiCate);
			Map<String, Object> asMap = kpiCate.toMap();
			ret.put("data", asMap);
			ret.put("message", "修改成功");
		} catch (Exception e) { 
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unused")
	private Representation deleteAll() throws Exception{
		JSONObject ret = new JSONObject();
		try{
			cateService.deleteAll();
			ret.put("message", "删除全部成功");
		}catch(Exception e){
			//e.printStackTrace();
			log.eLog(e.getMessage());
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 通过ID删除KPI分类
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteCateById(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			String userName = this.getUsername();
			//管理员可以删除所有
			boolean isAdmin = this.isAdmin();
			log.dLog("deleteCateById");
			if (id == null || id.equals("")) {
				throw new MException("参数不能空");
			}
			KpiCategory kpiCate = cateService.getById(id);
			if (kpiCate == null) {
				throw new Exception("KpiCategory[" + id + "]不存在");
			}
			if(!isAdmin){ //非管理员用户只能删除自己的KPI
				String owner = kpiCate.getOwner();
				if(!owner.equals(userName)){
					throw new MException("没有权限删除其他用户的分类!");
				}
			}
			cateService.delete(kpiCate, true);
			ret.put("message", "删除成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
}
