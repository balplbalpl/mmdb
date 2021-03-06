package com.mmdb.rest.relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jxl.Workbook;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.utils.ExcleJxlReadUtil;
import com.mmdb.core.utils.SysProperties;
import com.mmdb.core.utils.ExcleJxlReadUtil.Sublist;
import com.mmdb.core.utils.ExcleJxlReadUtil.Table;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.TypeFactory;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.relation.CiRelation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.relation.ICiRelService;
import com.mmdb.util.FileManager;
import com.mmdb.util.HexString;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

/**
 * 
 * @author xj
 * 
 */
public class CiRelRest extends BaseRest {

	private ICiRelService ciRelService;
	private ICiCateService cateService;
	private ICiInfoService infoService;
	private IRelCateService rCateService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(ICiCateService.class);
		infoService = context.getBean(ICiInfoService.class);
		rCateService = context.getBean(IRelCateService.class);
		ciRelService = context.getBean(ICiRelService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData(null, null, true);
		} else {
			return getById(param1);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("import".equals(param1)) {
			return new JsonRepresentation(importData(entity));
		}
		JSONObject params = parseEntity(entity);
		if ("export".equals(param1)) {
			if (params.containsKey("ids") && params.containsKey("hasData")) {
				JSONArray ids = params.getJSONArray("ids");
				boolean hasData = params.getBoolean("hasData");
				return exportData(ids, null, hasData);
			} else {
				String cateId = params.getString("cateId");
				JSONArray ciIds = params.getJSONArray("relIds");

				List<String> cateIds = new ArrayList<String>();
				cateIds.add(cateId);

				return exportData(cateIds, ciIds, true);
			}
		} else if ("query".equals(param1)) {
			if (params.size() == 1 && params.containsKey("cis")) {
				JSONArray ciIds = params.getJSONArray("cis");
				return queryByCiId(ciIds);
			} else if (params.size() == 4 && params.containsKey("ci")
					&& params.containsKey("ciRels")
					&& params.containsKey("like")
					&& params.containsKey("dirDepth")) {
				String id = params.getString("ci");
				List<String> rs = params.getJSONArray("ciRels");
				JSONObject term = params.getJSONObject("like");
				Map<String, String> dirDepth = params.getJSONObject("dirDepth");
				return queryCiRelationById(id, rs, term, dirDepth);
			}
		}

		if (params.size() == 6 && params.containsKey("ciId")
				&& params.containsKey("relCateId")
				&& params.containsKey("page") && params.containsKey("all")
				&& params.containsKey("pageSize") && params.containsKey("like")) {
			return qureyByAdvanced(params);
		} else
			return save(params);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return update(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		if (param1 == null || "".equals(param1)) {
			if (params != null) {
				if (params.containsKey("ids")) {
					return deleteByIds(params);
				} else if (params.containsKey("cateIds")) {
					return deleteByCateIds(params);
				}
			} else {
				return deleteAll();
			}
		} else {// 删除一个属性
			return deleteById(param1);// param1应该是jsonId
		}
		return notFindMethod(entity);
	}

	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		List<CiRelation> all = ciRelService.getAll();

		JSONArray list = new JSONArray();
		for (CiRelation rel : all) {
			Map<String, Object> asMap = rel.asMap();
			list.add(asMap);
		}
		ret.put("data", list);
		ret.put("message", "获取全部关系分类成功");
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
		CiRelation rel = ciRelService.getById(id);
		if (rel != null) {
			Map<String, Object> asMap = rel.asMap();

			ret.put("data", asMap);
			ret.put("message", "获取关系分类[" + id + "]成功");
		} else {
			ret.put("message", "获取关系分类[" + id + "]失败");
			getResponse().setStatus(new Status(600));
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
	private Representation exportData(List<String> cateIds, List<String> ids,
			boolean hasData) {
		JSONObject ret = new JSONObject();
		log.dLog("exportXML");
		File file = FileManager.getInstance().createFile("关系分类数据", "xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final int pageSize = 4;

		try {
			int i = 0;
			WritableWorkbook wb = Workbook.createWorkbook(file);
			List<RelCategory> crs = rCateService.getAll();
			for (RelCategory nc : crs) {
				String id = nc.getId();
				String name = nc.getName();
				List<Attribute> items = nc.getAllAttributes();
				if (cateIds == null || cateIds.size() == 0
						|| cateIds.contains(id)) {
					String pName = nc.getParent() != null ? nc.getParent()
							.getName() + "-" : "";
					WritableSheet ws = wb.createSheet(pName + name, i);
					List<String> title = this.createXlsSheet(ws, items);
					if (hasData) {
						List<CiRelation> datas = ciRelService.qureyByAdvanced(
								nc, null, null, false);
						List<CiRelation> addDatas = new ArrayList<CiRelation>();

						if (ids == null || ids.size() == 0) {
							addDatas.addAll(datas);
						} else {
							for (CiRelation rel : datas) {
								if (ids.contains(rel.getId())) {
									addDatas.add(rel);
								}
							}
						}
						int t = addDatas.size() / pageSize;
						if (addDatas.size() % pageSize != 0) {
							t++;
						}
						for (int j = 0; j < t; j++) {
							if (j > 0) {
								ws = wb.createSheet(pName + name + "(" + j + ")",
										++i);
							}
							int toIndex = (j + 1) * pageSize;
							if (toIndex > addDatas.size()) {
								toIndex = addDatas.size();
							}
							List<CiRelation> subList = addDatas.subList(j
									* pageSize, toIndex);
							title = this.createXlsSheet(ws, items);
							this.addCell(ws, title, subList);
						}
//						this.addCell(ws, title, addDatas);
					}
					i++;
				}
			}
			if (wb.getNumberOfSheets() == 0) {
				wb.createSheet("Sheet1", i);
			}
			wb.write();
			wb.close();
			ret.put("message", "下载分类数据成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
		} catch (Exception e) {
			e.printStackTrace();
			if (file != null && file.exists()) {
				file.delete();
			}
			log.eLog(e);
			ret.put("message", "创建文件失败");
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入XML
	 * 
	 * @param entity
	 * @return
	 */
	private String importData(Representation entity) {
		log.dLog("importExcel");
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
		try {
			filename = fi.getName();
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf("xls") == -1) {
				log.eLog("文件格式有误");
				throw new Exception("文件格式有误");
			}
			int num = 0;
			WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
			if(wsm!=null){
				wsm.broadcast("正在解析EXCEL，请等待...", 0.1d);
			}
//			MessageResult.broadcast("正在解析EXCEL，请等待...", 0.1d);
			ExcleJxlReadUtil ejr = new ExcleJxlReadUtil(fi.getInputStream());
			Table table = ejr.getTable();
			List<Sublist> sublist = table.getSublist();
			if(wsm!=null){
				wsm.broadcast("准备分类，请等待...", 0.2d);
			}
//			MessageResult.broadcast("准备分类，请等待...", 0.2d);

			List<RelCategory> all = rCateService.getAll();
			Map<String, RelCategory> cache = new HashMap<String, RelCategory>();
			for (RelCategory relCategory : all) {
				cache.put(relCategory.getName(), relCategory);
			}

			int totle = 0;
			for (Sublist sl : sublist) {
				totle += sl.getData().size();
			}
			double flag = 0.8d / totle;
			double progress = 0.2d;
			
			String username = getUsername();
			// key:用户名,value:true存在,false不存在
			Map<String, Boolean> users = new HashMap<String, Boolean>();
			users.put(username, true);
			
			for (Sublist sl : sublist) {
				String name = sl.getName(); // 父分类Name-分类Name
				if (name.endsWith(")")) {
					int s = name.lastIndexOf("(");
					int e = name.lastIndexOf(")");
					if (s > 0 && e > 0 && s < e && (e - s) <= 3) {
						name = name.substring(0, s);
					}
				}
				log.iLog("解析sheet[" + sl.getName() + "]...");
				// pushInfoMsg为javascript函数
				if(wsm!=null){
					wsm.broadcast("解析sheet[" + sl.getName() + "]...", progress);
				}
//				MessageResult.broadcast("解析sheet[" + sl.getName() + "]...", progress);
				if (name.indexOf("-") != -1) {
					String[] cs = name.split("-");
					name = cs[cs.length - 1]; // 得到分类Name
				}
				RelCategory rc = cache.get(name);
				if (rc == null) {
					if(wsm!=null){
						wsm.broadcast("分类[" + sl.getName() + "]不存在,无法新建",
								progress);
					}
//					MessageResult.broadcast("分类[" + sl.getName() + "]不存在,无法新建",
//							progress);
				} else {
					Set<CiRelation> datas = new HashSet<CiRelation>();
					List<Map<String, Object>> data = sl.getData();
					datas = this.getCiRelsBySheet(data, rc,users,username);
					if (datas.size() > 0) {
						log.iLog("新建/更新数据开始...");
						if(wsm!=null){
							wsm.broadcast("新建/更新数据开始...", progress);
						}
//						MessageResult.broadcast("新建/更新数据开始...", progress);
						Map<String, Long> rm = ciRelService.saveOrUpdate(rc,
								datas);
						if(wsm!=null){
							wsm.broadcast("分类[" + name + "]", progress);
						}
//						MessageResult.broadcast("分类[" + name + "]", progress);
						progress += flag * datas.size();

						if (rm.get("save") != 0) {
							if(wsm!=null){
								wsm.broadcast("新建(" + rm.get("save")
										+ ")条新数据 ", progress);
							}
//							MessageResult.broadcast("新建(" + rm.get("save")
//									+ ")条新数据 ", progress);
						}
						if (rm.get("update") != 0) {
							if(wsm!=null){
								wsm.broadcast("更新(" + rm.get("update")
										+ ")条数据", progress);
							}
//							MessageResult.broadcast("更新(" + rm.get("update")
//									+ ")条数据", progress);
						}
					} else {
						if(wsm!=null){
							wsm.broadcast("分类[" + name + "]无数据", progress);
						}
//						MessageResult
//								.broadcast("分类[" + name + "]无数据", progress);
					}
					num += data.size();
				}
			}
			if(wsm!=null){
				wsm.broadcast("上传关系完成", 1d);
				wsm.broadcast("成功导入关系[" + num + "]条", 1d);
			}
//			MessageResult.broadcast("上传关系完成", 1d);
//			MessageResult.broadcast("成功导入关系[" + num + "]条", 1d);
			ret.put("message", "上传完成");
			log.dLog("importXML success");
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return ret.toString();
	}

	private JsonRepresentation save(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();

		String sJsonId = null;
		String eJsonId = null;
		String rCateId = null;
		JSONObject relValue = null;
		try {
			sJsonId = data.getString("sCiId");
			eJsonId = data.getString("eCiId");
			rCateId = data.getString("relCateId");
			relValue = data.getJSONObject("relValue");
		} catch (Exception e1) {
			throw e1;
		}
		if (sJsonId == null || sJsonId.equals("")) {
			throw new MException("起点参数不能为空");
		}
		if (eJsonId == null || eJsonId.equals("")) {
			throw new MException("终点参数不能为空");
		}
		if (rCateId == null || rCateId.equals("")) {
			throw new MException("关系参数不能为空");
		}
		// sJsonId = HexString.decode(sJsonId);
		CiInformation sci = infoService.getById(sJsonId);
		if (sci == null) {
			sJsonId = HexString.decode(sJsonId);
			throw new MException("起点CI[" + sJsonId + "]不存在");
		}
		// eJsonId = HexString.decode(eJsonId);
		CiInformation eci = infoService.getById(eJsonId);
		if (eci == null) {
			eJsonId = HexString.decode(eJsonId);
			throw new MException("终点CI[" + eJsonId + "]不存在");
		}
		RelCategory rc = rCateService.getById(rCateId);
		if (rc == null) {
			throw new MException("关系类型[" + rCateId + "]不存在");
		}
		
		CiRelation infoRel = new CiRelation(sci, eci, rc, this.paddingRelValue(
				rc, relValue));
		infoRel.setOwner(getUsername());
		CiRelation iRelation = ciRelService.getById(infoRel.getId());
		if (iRelation != null) {
			throw new MException("关系已存在");
		} else {
			ciRelService.save(infoRel);
		}
		ret.put("message", "保存关系成功");

		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation update(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("update");
		String id = null;
		JSONObject relValue = null;

		try {
			id = data.getString("id");
			relValue = data.getJSONObject("relValue");
		} catch (Exception e1) {
			throw e1;
		}

		if (id == null || id.equals("")) {
			throw new MException("映射名称参数为空");
		}
		CiRelation ciRel = ciRelService.getById(id);
		Dynamic<String, Object> oldValue = ciRel.getRelValue();
		Set keySet = relValue.keySet();
		for (Object key : keySet) {
			if (oldValue.containsKey(key)) {
				oldValue.put(key.toString(), relValue.get(key));
			}
		}
		CiRelation update = ciRelService.update(ciRel);
		ret.put("message", update.asMap());
		ret.put("message", "编辑成功");
		log.dLog("save success");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除全部的ciCate和ci
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteAll() {
		JSONObject ret = new JSONObject();
		log.dLog("deleteAll");
		ret.put("message", "不支持删除全部");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除ciCate和ciCate下的全部ci
	 * 
	 * @param cateId
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteById");
		CiRelation rel = ciRelService.getById(id);
		ciRelService.delete(rel);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteByIds(JSONObject ids) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteByIds");
		JSONArray jsonIds = null;
		try {
			jsonIds = ids.getJSONArray("ids");
		} catch (Exception e) {
		}
		if (jsonIds != null && jsonIds.size() > 0) {
			List<CiRelation> rels = new ArrayList<CiRelation>();
			for (Object id : jsonIds) {
				// id = HexString.decode(id);
				CiRelation rel = ciRelService.getById((String) id);
				if (rel != null)
					rels.add(rel);
			}
			if (rels.size() > 0)
				ciRelService.delete(rels);
			ret.put("message", "删除成功");
		} else {
			throw new MException("参数有空值");
		}

		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteByCateIds(JSONObject ids) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteByIds");
		JSONArray jsonIds = null;
		try {
			jsonIds = ids.getJSONArray("cateIds");
		} catch (Exception e) {
		}
		if (jsonIds != null && jsonIds.size() > 0) {
			List<CiRelation> rels = new ArrayList<CiRelation>();
			for (Object id : jsonIds) {
				RelCategory rCate = rCateService.getById((String) id);
				if (rCate == null) {
					throw new MException("分类不存在");
				}
				ciRelService.delete(rCate);
			}
			ret.put("message", "删除成功");
		} else {
			throw new Exception("参数有空值");
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 
	 * @param ciId
	 * @param rCateid
	 * @param must
	 * @param orExp
	 * @param page
	 * @param pageSize
	 * @param extend
	 * @return
	 * @throws Exception
	 */
	public Representation qureyByAdvanced(JSONObject data) throws Exception {

		JSONObject ret = new JSONObject();
		String ciId = null;
		String rCateid = null;
		int page = 1;
		int pageSize = 10;
		Map<String, String> like = null;
		Boolean extend = null;

		try {
			ciId = data.getString("ciId");
			rCateid = data.getString("relCateId");
			page = data.getInt("page");
			pageSize = data.getInt("pageSize");
			like = data.getJSONObject("like");
		} catch (Exception e1) {
			throw e1;
		}

		try {
			extend = data.getBoolean("all");
		} catch (Exception e1) {
		}

		RelCategory cate = rCateService.getById(rCateid);
		if (cate == null) {
			throw new MException("关系分类不存在");
		}
		List<CiRelation> cRels = ciRelService.qureyByAdvanced(cate, null,
				like, extend);

		if (ciId != null && !ciId.trim().equals("")) {
			ciId = ciId.trim();
			Iterator<CiRelation> iter = cRels.iterator();
			while (iter.hasNext()) {
				CiRelation rel = iter.next();
				CiInformation c1 = rel.getStartInfo(), c2 = rel.getEndInfo();
				if (c1.getId().indexOf(ciId) == -1
						&& c2.getId().indexOf(ciId) == -1) {
					iter.remove();
				}
			}
		}
		List<Object> datas = new ArrayList<Object>();
		int count = cRels.size();
		int start = (page - 1) * pageSize;
		start = start < 0 ? 0 : start;
		int end = page * pageSize;
		start = start > count ? count : start;
		end = end > count ? count : end;
		cRels = cRels.subList(start, end);
		for (CiRelation rel : cRels) {
			Map<String, Object> relData = new HashMap<String, Object>();
			CiInformation c1 = rel.getStartInfo(), c2 = rel.getEndInfo();
			 CiCategory nc = c1.getCategory(), nc2 = c2.getCategory();
			relData.put("起点分类id", c1.getCategoryId());
			relData.put("起点id", c1.getId() + "");
			relData.put("起点分类名称", c1.getCategoryName());
			relData.put("起点主键", nc.getMajor().getName());
			relData.put("起点对象", c1.getData().get(nc.getMajor().getName()));

			relData.put("终点分类id", c2.getCategoryId());
			relData.put("终点id", c2.getId() + "");
			relData.put("终点分类名称", c2.getCategoryName());
			relData.put("终点主键", nc2.getMajor().getName());
			relData.put("终点对象", c2.getData().get(nc2.getMajor().getName()));

			relData.put("关系类名", rCateService.getById(rel.getRelCateId())
					.getName());
			relData.put("关系属性值", rel.getRelValue());
			relData.put("_id_", rel.getId());
			relData.put("_neo4jid_", rel.getRelationId() + "");
			relData.put("_relCateId_", rel.getRelCateId());
			datas.add(relData);

		}
		Map<String, Object> retData = new HashMap<String, Object>();
		retData.put("page", page);
		retData.put("pageSize", pageSize);
		retData.put("count", count);
		retData.put("datas", datas);
		ret.put("data", retData);
		ret.put("message", "查询成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过一组ci查询这组内ci之间相互的关系
	 * 
	 * @param ciIds
	 *            ci的Hexid
	 * 
	 * @return
	 * @throws Exception 
	 */
	public Representation queryByCiId(List<String> ciIds) throws Exception {
		JSONObject ret = new JSONObject();
		List<Map<String, Object>> queryCiInRel = ciRelService
				.queryCiInRel(ciIds);
//		System.out.println(queryCiInRel);
		ret.put("data", queryCiInRel);
		ret.put("message", "查询成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 处理关系属性空值
	 * 
	 * @param rc
	 *            关系分类
	 * @param relValue2
	 *            关系值
	 */
	private Dynamic<String, Object> paddingRelValue(RelCategory rc,
			Map<String, Object> relValue2) {
		List<String> atrrs = rc.getAttributeNames();
		Dynamic<String, Object> relValue = new Dynamic<String, Object>();
		for (String attr : atrrs) {
			if (relValue2.containsKey(attr)) {
				relValue.put(attr, relValue2.get(attr));
			} else {
				relValue.put(attr, "");
			}
		}
		return relValue;
	}

	/**
	 * 新建XLS表单标题
	 * 
	 * @param ws
	 *            分类sheet
	 * @param items
	 *            分类的属性
	 * @return
	 * @throws Exception
	 */
	private List<String> createXlsSheet(WritableSheet ws, List<Attribute> items)
			throws Exception {
		String[] source = { "CMDB,EXCEL,PAGE" };
		List<String> ls = Arrays.asList(source);
		com.mmdb.model.bean.Type type = TypeFactory.getType("String");
		Attribute sc = new Attribute("源分类", type, false, true, false, "", ls);
		if (!items.contains(sc)) {
			items.add(0, sc);
		}
		Attribute sf = new Attribute("源字段", type, false, true, false, "", ls);
		if (!items.contains(sf)) {
			items.add(1, sf);
		}
		Attribute si = new Attribute("源对象", type, false, true, false, "", ls);
		if (!items.contains(si)) {
			items.add(2, si);
		}
		Attribute ec = new Attribute("目标分类", type, false, true, false, "", ls);
		if (!items.contains(ec)) {
			items.add(ec);
		}
		Attribute ef = new Attribute("目标字段", type, false, true, false, "", ls);
		if (!items.contains(ef)) {
			items.add(ef);
		}
		Attribute ei = new Attribute("目标对象", type, false, true, false, "", ls);
		if (!items.contains(ei)) {
			items.add(ei);
		}
	 
		WritableFont wf_color = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		WritableCellFormat wff_color = new WritableCellFormat(wf_color);
		wff_color.setBackground(Colour.GRAY_25);
		WritableFont wf_color2 = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE);
		WritableCellFormat wff_color2 = new WritableCellFormat(wf_color2);
		List<String> title = new ArrayList<String>();
		for (int j = 0; j < items.size(); j++) {
			Attribute map = items.get(j);
			String required = map.getRequired().toString();
			Label label = null;
			if (Boolean.valueOf(required)) {
				label = new Label(j, 0, map.getName(), wff_color);
			} else {
				label = new Label(j, 0, map.getName(), wff_color2);
			}
			title.add(map.getName());
			WritableCellFeatures wf = new WritableCellFeatures();
			// info = "数据类型：" + map.getType().getName() + "\n";
			String info = "是否必填：" + map.getRequired() + "\n";
			if (map.getRequired()) {
				info += "缺省数值：" + map.getDefaultValue() + "\n";
			}
			info += "数据来源：" + map.getSources() + "\n";
			wf.setComment(info);
			label.setCellFeatures(wf);
			// label 可以设置宽度
			ws.addCell(label);
		}
		
		title.add("owner");
		Label ownerLabel = new Label(title.size()-1, 0,"owner", wff_color2);
		WritableCellFeatures wf = new WritableCellFeatures();
		// info = "数据类型：" + map.getType().getName() + "\n";
		String info = "是否必填：false \n 缺省数值：当前用户 \n";
		wf.setComment(info);
		ownerLabel.setCellFeatures(wf);	
		ws.addCell(ownerLabel);
		return title;
	}

	/**
	 * 把关系数据写入EXCEL
	 * 
	 * @param ws
	 *            WritableSheet
	 * @param title
	 *            EXCEL标题
	 * @param rels
	 *            关系数组
	 * @throws WriteException
	 */
	private void addCell(WritableSheet ws, List<String> title,
			List<CiRelation> rels) throws WriteException {
		int v = 1;
		for (CiRelation rel : rels) {
			CiInformation si = rel.getStartInfo(), ei = rel.getEndInfo();
			 CiCategory sn = rel.getStartInfo().getCategory(), en = rel
			 .getEndInfo().getCategory();
			Map<String, Object> data = rel.getRelValue();
			data.put("源分类", si.getCategoryName());
			data.put("源字段", sn.getMajor().getName());
			data.put("源对象", rel.getStartInfo().getName());
			data.put("目标分类", ei.getCategoryName());
			data.put("目标字段", en.getMajor().getName());
			data.put("目标对象", rel.getEndInfo().getName());
			data.put("owner", rel.getOwner()==null?"":rel.getOwner());
			for (int k = 0; k < title.size(); k++) {
				String value = data.containsKey(title.get(k)) ? String
						.valueOf(data.get(title.get(k))) : "";
				Label label = new Label(k, v, value);
				ws.addCell(label);
			}
			v++;
		}
	}

	/**
	 * 遍历一个ci相关联的ci,有向上关联(in),向下关联(out)
	 * 
	 * @param id
	 *            ci的id
	 * @param rs
	 *            要查询的关系id
	 * @param term
	 *            实际上是个复杂的map
	 * @param dirDepth
	 *            方向有两个参数 up 和 down
	 * @return 返回被关联的ci
	 */
	@SuppressWarnings("unchecked")
	public Representation queryCiRelationById(String id, List<String> rs,
			JSONObject term, Map<String, String> dirDepth) {
		JSONObject ret = new JSONObject();
		// Return ret = new Return();
		try {
			log.dLog("queryCiRelationById");
			if (id == null || id.equals("")) {
				throw new Exception("参数JSON不能为空");
			}
			CiInformation info = infoService.getById(id);
			if (info == null) {
				throw new Exception("关系[" + id + "]不存在");
			}
			if (rs == null || rs.size() == 0) {
				rs = rCateService.getCateNames();
			}
			Map<String, Map<String, String>> tMap = null;
			if (term != null) {
				tMap = term;
			} else {
				tMap = new HashMap<String, Map<String, String>>();
			}

			if (dirDepth != null && dirDepth.size() > 0) {
				Set<CiInformation> cisSet = ciRelService.newTraversal(info, rs,
						tMap, dirDepth);

				List<Map<String, Object>> ciss = new ArrayList<Map<String, Object>>();
				if (cisSet != null) {
					for (CiInformation ci : cisSet) {
						Map<String, Object> asMap = ci.asMapForRest();
						ciss.add(asMap);
					}
				}

				ret.put("data", ciss);
			}
			log.dLog("queryCiRelationById success");
		} catch (Exception me) {
			log.eLog(me);
			ret.put("message", me.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取单个sheet中的关系数据 分类必须存在,字段为空时默认id,字段和对象(值)都为空全建上关系
	 * <p -----+------+------+------+------+------+--->
	 * 分三种情况,其中分类都不能为null或""
	 * <P>
	 * <p 第一种>
	 * 字段和值(源对象,目标对象)都为空时,将这个分类下的全部ci进行匹配
	 * <p 第二种>
	 * 字段为空值不为空,将这个分类下的ci全部字段与值进行匹配(不是模糊的)拿出ci,然后进行关系映射
	 * <p 第三中>
	 * 字段和值都不为空,匹配分类下的ci指定ci和值,然后将选择出的数据进行关系匹配.
	 * @param users 存放用户的缓存
	 * @param username 当前用户
	 * @return
	 * @throws Exception
	 */
	private Set<CiRelation> getCiRelsBySheet(List<Map<String, Object>> objs,
			RelCategory rc,Map<String,Boolean> users,String username) throws Exception {
		Set<CiRelation> datas = new HashSet<CiRelation>();
		for (Map<String, Object> obj : objs) {
			String sCateName = obj.get("源分类") == null ? "" : obj.get("源分类")
					.toString(); // 源分类名
			String sField = obj.get("源字段") == null ? "" : obj.get("源字段")
					.toString();
			String sValue = obj.get("源对象") == null ? "" : obj.get("源对象")
					.toString();

			String eCateName = obj.get("目标分类") == null ? "" : obj.get("目标分类")
					.toString(); // 目标分类名
			String eField = obj.get("目标字段") == null ? "" : obj.get("目标字段")
					.toString();
			String eValue = obj.get("目标对象") == null ? "" : obj.get("目标对象")
					.toString();
			if (sCateName.equals("") || eCateName.equals(""))
				continue;
			
			Object owner = obj.get("owner");
			if (owner == null || "".equals(owner)) {
				owner = username;
			} else {
				Boolean boolean1 = users.get(owner);
				if (boolean1 == null) {
					User user = userService
							.getUserByLoginName(owner.toString());
					boolean1 = user != null;
					users.put(owner.toString(), boolean1);
				}
				if (!boolean1)
					owner = username;
			}
			
			CiCategory sCate = cateService.getByName(sCateName);
			CiCategory eCate = cateService.getByName(eCateName);

			List<CiInformation> scis = new ArrayList<CiInformation>();
			List<CiInformation> ecis = new ArrayList<CiInformation>();

			if (sField.equals("")) {
				if (sValue.equals("")) {
					// 匹配全部
					scis = infoService.getByCategory(sCate);
				} else {
					// 字段为null时默认使用id作为匹配字段
					scis = infoService.getByProperty("data$"
							+ sCate.getMajor().getName(), sValue);
				}
			} else {
				scis = infoService.getByProperty("data$" + sField, sValue);
			}

			if (eField.equals("")) {
				if (eValue.equals("")) {
					// 匹配全部
					ecis = infoService.getByCategory(eCate);
				} else {
					// 字段为null时默认使用id作为匹配字段
					ecis = infoService.getByProperty("data$"
							+ eCate.getMajor().getName(), eValue);
				}
			} else {
				ecis = infoService.getByProperty("data$" + eField, eValue);
			}

			for (CiInformation sci : scis) {
				for (CiInformation eci : ecis) {
					// if (!sci.getHexId().equals(eci.getHexId())) {//可以自己到自己
					CiRelation rel = new CiRelation(sci, eci, rc,
							this.paddingRelValue(rc, obj));
					rel.setOwner(owner.toString());
					datas.add(rel);
					// }
				}
			}
		}
		return datas;
	}
}
