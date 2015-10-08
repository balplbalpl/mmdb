package com.mmdb.rest.relation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

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
import com.mmdb.core.utils.ExcleJxlReadUtil;
import com.mmdb.core.utils.ExcleJxlReadUtil.Sublist;
import com.mmdb.core.utils.ExcleJxlReadUtil.Table;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.relation.ICiKpiRelService;
import com.mmdb.util.FileManager;
import com.mmdb.util.HexString;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class CiKpiRest extends BaseRest {

	private ICiKpiRelService ciKpiRelService;

	private ICiInfoService infoService;

	private ICiCateService cateService;

	@Override
	public void ioc(ApplicationContext context) {
		ciKpiRelService = context.getBean(ICiKpiRelService.class);
		infoService = context.getBean(ICiInfoService.class);
		cateService = context.getBean(ICiCateService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		String id = getValue("param2");

		if ("kpiCate".equals(param1)) {
			return getKpiCateById(id);
		} else if ("kpi".equals(param1)) {
			String kpiCateId = getValue("param3");
			return getKpiByKpiCate(id, kpiCateId);
		} else {
			return getById(id);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("import".equals(param1)) {
			return new JsonRepresentation(importData(entity));
		}
		JSONObject params = parseEntity(entity);
		if ("delete".equals(param1)) {
			return delete(params);
		} else if ("export".equals(param1)) {
			return exportData(params);
		} else if ("find".equals(param1)) {
			return getAllRelation(params);
		} else {
			return save(params);
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Representation delHandler(Representation entity) throws Exception {
		// String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		if (params != null) {
			return deleteByIds(params.getJSONArray("ids"));
		}
		return notFindMethod(entity);
	}

	/**
	 * 获取到指定CI下的KPI信息列表
	 * 
	 * @param id
	 *            CI的ID
	 * @return jsonArray
	 * @throws Exception
	 */
	private Representation getById(String ciId) throws Exception {
		JSONObject ret = new JSONObject();
		JSONArray list = new JSONArray();
		// 取出符合指定ci的关系Map放入到返回列表中
		List<Map<String, String>> relList = ciKpiRelService.getRelByCiId(ciId);
		for (Map<String, String> ciKpiMap : relList) {
			list.add(ciKpiMap);
		}
		ret.put("data", list);
		ret.put("message", "获取CI KPI分类数据成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取到指定CI下的KPI分类
	 * 
	 * @param id
	 *            CI的ID
	 * @return jsonArray
	 * @throws Exception
	 */
	private Representation getKpiCateById(String ciId) throws Exception {
		JSONObject ret = new JSONObject();
		JSONArray list = new JSONArray();
		// 取出符合指定ci的关系Map放入到返回列表中
		List<KpiCategory> relList = ciKpiRelService.getKpiCateByCiId(ciId);
		for (KpiCategory kpiCate : relList) {
			list.add(kpiCate.toMap());
		}
		ret.put("data", list);
		ret.put("message", "获取CI KPI分类数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取到指定CI下的KPI分类
	 * 
	 * @param ciId
	 *            CI的ID
	 * @param kpiCateId
	 *            kpi分类
	 * @return jsonArray
	 * @throws Exception
	 */
	private Representation getKpiByKpiCate(String ciId, String kpiCateId)
			throws Exception {
		JSONObject ret = new JSONObject();
		JSONArray list = new JSONArray();
		// 取出符合指定ci的关系Map放入到返回列表中
		List<KpiInformation> relList = ciKpiRelService.getKpiByKpiCate(ciId,
				kpiCateId);
		for (KpiInformation kpi : relList) {
			list.add(kpi.toMap());
		}
		ret.put("data", list);
		ret.put("message", "获取KPI数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取到指所有的CI和KPI关系列表
	 * 
	 * @param JSONObject
	 *            data
	 * @return
	 * @throws Exception
	 */
	private Representation getAllRelation(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String cateId = data.getString("categoryId");
		Boolean extend = data.getBoolean("all");
		JSONObject orExp = data.getJSONObject("like");
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");

		long t1 = System.currentTimeMillis();
		String major = "";
		String ciCateName = "";
		List<CiInformation> ciList = new ArrayList<CiInformation>();;
		if (orExp == null) {
			throw new Exception("查询条件不能为空");
		}
		if (cateId == null || cateId.equals("")) {
			int pageIndex = 1;
			Map<String, Object> m = infoService.qureyByAdvanced(null, null, orExp, extend == null ? true : extend, null, (pageIndex-1)*Tool.getBuff, Tool.getBuff);
			List<CiInformation> cis = (List<CiInformation>)m.get("data");
			int count = (Integer)m.get("count");
			int index = 0;
			while(true){
				for(CiInformation info:cis){
					ciList.add(info);
					index++;
				}
				if(index>=count){
					break;
				}
				pageIndex++;
				m = infoService.qureyByAdvanced(null, null, orExp, extend == null ? true : extend, null, (pageIndex-1)*Tool.getBuff, Tool.getBuff);
				cis = (List<CiInformation>)m.get("data");
				count = (Integer)m.get("count");
			}
		}else if(orExp.containsKey("*") && extend==false){ 
			//如果只是选了分类，没有填其他查询条件的情况下，可以直接通过分类名称查询关系列表，这样速度会快很多
			CiCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new MException("分类不存在");
			}
			major = nc.getMajor().getName();
			ciCateName = nc.getName();
		}else {
			CiCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new MException("分类不存在");
			}

			int pageIndex = 1;
			Map<String, Object> m = infoService.qureyByAdvanced(nc, null, orExp, extend == null ? true : extend, null, (pageIndex-1)*Tool.getBuff, Tool.getBuff);
			List<CiInformation> cis = (List<CiInformation>)m.get("data");
			int count = (Integer)m.get("count");
			int index = 0;
			while(true){
				for(CiInformation info:cis){
					ciList.add(info);
					index++;
				}
				if(index>=count){
					break;
				}
				pageIndex++;
				m = infoService.qureyByAdvanced(nc, null, orExp, extend == null ? true : extend, null, (pageIndex-1)*Tool.getBuff, Tool.getBuff);
				cis = (List<CiInformation>)m.get("data");
				count = (Integer)m.get("count");
			}
			major = nc.getMajor().getName();
		}
		
		long t2 = System.currentTimeMillis();
		
		log.dLog("Query Ci :"+(t2-t1)+"ms");
		
		int count = 0;
		List<Map<String, String>> ciKpiRelList =  new LinkedList<Map<String, String>>();
		
		
		if(orExp.containsKey("*") && extend==false){
			count = ciKpiRelService.getCountByCiCate(ciCateName);
			// 通过CiHex查询到ci kpi关联关系
			ciKpiRelList = ciKpiRelService.getRelbyCiCate(ciCateName, page, pageSize);
		}else{
			// 获取的所有的ciHex列表
			List<String> ciHexs = new ArrayList<String>();
			for (CiInformation ci : ciList) {
				ciHexs.add(ci.getCiHex());
			}
			
			count = ciKpiRelService.getCountByCiHexs(ciHexs);
			
			long t3 = System.currentTimeMillis();
			
			log.dLog("Count Ci :"+(t3-t2)+"ms");
			// 通过CiHex查询到ci kpi关联关系
			ciKpiRelList = ciKpiRelService
					.getAllRelbyCiHexs(ciHexs, page, pageSize);
			

			long t4 = System.currentTimeMillis();
			
			log.dLog("Query rel :"+(t4-t3)+"ms");
		}
		
		int t = count / pageSize;
		if (count % pageSize != 0) {
			t++;
		}
		if (page > t) {
			page = t;
		}
		if (page < 1) {
			page = 1;
		}

		// 完善返回列表信息
		List<Map<String, Object>> retList = new LinkedList<Map<String, Object>>();
		for (Map<String, String> relMap : ciKpiRelList) {
			Map<String, Object> dataMap = new HashMap<String, Object>();
			// 解析ciHexId
			String ciHexArray = HexString.decode((String) relMap.get("ciId"));
			JSONArray ciNames = JSONArray.fromObject(ciHexArray);
			dataMap.put("id", relMap.get("id"));
			dataMap.put("ciName", ciNames.getString(1));
			dataMap.put("ciMajorName", major);
			dataMap.put("ciCategoryName", ciNames.getString(0));
			dataMap.put("ciHex", relMap.get("ciId"));
			dataMap.put("kpiName", relMap.get("kpiName"));
			dataMap.put("kpiCategoryName", relMap.get("kpiCategoryName"));
			dataMap.put("kpiHex", relMap.get("kpiId"));
			dataMap.put("autoRelation",
					Boolean.parseBoolean((String) relMap.get("autoRelation")));
			dataMap.put("hasData",
					Boolean.parseBoolean((String) relMap.get("hasData")));
			retList.add(dataMap);
		}

		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("count", count);
		retMap.put("page", page);
		retMap.put("pageSize", pageSize);
		retMap.put("datas", retList);

		ret.put("data", retMap);
		ret.put("message", "查询成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 保存CI和KPI的关系
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private Representation save(JSONObject obj) throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("save");
		if (obj == null || obj.size() == 0) {
			throw new Exception("save参数不能空");
		}
		String ciId = obj.getString("ciId");
		if (ciId == null || ciId.equals("")) {
			throw new Exception("CI不能空");
		}

		String kpiIds = obj.getString("kpiIds");
		if (kpiIds == null || kpiIds.equals("")) {
			throw new Exception("KPI不能空");
		}

		List<String> ciIdList = JSONArray.fromObject(ciId);

		List<String> kpiIdList = JSONArray.fromObject(kpiIds);

		ciKpiRelService.saveCiKpiRel(ciIdList, kpiIdList);

		ret.put("message", "保存成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除CI KPI关系
	 * 
	 * @param obj
	 *            {ciId:'',kpiIds:[]}
	 * @return
	 * @throws Exception
	 */
	private Representation delete(JSONObject obj) throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("delete");
		if (obj == null || obj.size() == 0) {
			throw new Exception("删除参数不能空");
		}
		String ciId = obj.getString("ciId");
		if (ciId == null || ciId.equals("")) {
			throw new Exception("CI不能空");
		}
		String kpiIds = obj.getString("kpiIds");
		if (kpiIds == null || kpiIds.equals("")) {
			throw new Exception("KPI不能空");
		}
		List<String> kpiIdList = JSONArray.fromObject(kpiIds);

		ciKpiRelService.delCiKpiRel(ciId, kpiIdList);

		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过关系ID列表删除CI KPI关系
	 * 
	 * @param 关系ID列表
	 * @return
	 * @throws Exception
	 */
	private Representation deleteByIds(List<String> relIds) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteByRelId");
		ciKpiRelService.delCiKpiRelByIds(relIds);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportData(JSONObject params) {
		JSONObject ret = new JSONObject();
		log.dLog("exportKpiCi");
		File file = FileManager.getInstance().createFile("CIKPI映射", "xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			WritableWorkbook wb = Workbook.createWorkbook(file);
			WritableSheet ws = wb.createSheet("Sheet1", 0);
			Label ciCateL = new Label(0, 0, "CI分类");
			ws.addCell(ciCateL);
			Label ciAttrL = new Label(1, 0, "CI字段");
			ws.addCell(ciAttrL);
			Label ciL = new Label(2, 0, "CI值");
			ws.addCell(ciL);
			Label kpiCateL = new Label(3, 0, "KPI分类");
			ws.addCell(kpiCateL);
			Label kpiL = new Label(4, 0, "KPI");
			ws.addCell(kpiL);
			Label autoRelationL = new Label(5, 0, "自动关联");
			ws.addCell(autoRelationL);
			Label hasDataL = new Label(6, 0, "是否有数据");
			ws.addCell(hasDataL);

			List<String> cateIds = params.containsKey("ids") ? (params
					.getJSONArray("ids").size() == 0 ? null : params
					.getJSONArray("ids")) : null;
			Boolean hasChildren = params.containsKey("hasData") ? params
					.getBoolean("hasData") : false;
			List<Map<String, String>> rels = ciKpiRelService.getAllCiKpiRel(
					cateIds, hasChildren);
			int i = 1;
			for (Map<String, String> rel : rels) {
				Label ciCate_l = new Label(0, i, rel.get("ciCate"));
				ws.addCell(ciCate_l);
				Label ciAttr_l = new Label(1, i, rel.get("ciAttr"));
				ws.addCell(ciAttr_l);
				Label ci_l = new Label(2, i, rel.get("ci"));
				ws.addCell(ci_l);
				Label kpiCate_l = new Label(3, i, rel.get("kpiCate"));
				ws.addCell(kpiCate_l);
				Label kpi_l = new Label(4, i, rel.get("kpi"));
				ws.addCell(kpi_l);
				Label autoRelation_l = new Label(5, i, rel.get("autoRelation"));
				ws.addCell(autoRelation_l);
				Label hasData_l = new Label(6, i, rel.get("hasData"));
				ws.addCell(hasData_l);
				i++;
			}

			wb.write();
			wb.close();
			ret.put("message", "下载CIKPI映射数据成功");
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

	private String importData(Representation entity) {
		log.dLog("importKpiCi");
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
			WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
			if(wsm!=null){
				wsm.broadcast("正在解析EXCEL，请等待...", 0.1d);
			}
//			MessageResult.broadcast("正在解析EXCEL，请等待...", 0.1d);
			// Integer count = ciKpiRelService.getCiKpiRelCount();
			ExcleJxlReadUtil ejr = new ExcleJxlReadUtil(fi.getInputStream());
			Table table = ejr.getTable();
			List<Sublist> sublist = table.getSublist();
			Map<String, CiCategory> ciCateMap = ciKpiRelService.getCiCateMap();
			if(wsm!=null){
				wsm.broadcast("开始建立CI-KPI关联映射，请等待...", 0.2d);
			}
//			MessageResult.broadcast("开始建立CI-KPI关联映射，请等待...", 0.2d);

			/*
			 * int totle = 0; for (Sublist sl : sublist) { totle +=
			 * sl.getData().size(); } double flag = 0.8d / totle; double
			 * progress = 0.2d;
			 */
			int saveCount = 0;
			int updateCount = 0;
			Double i = 1D;
			for (Sublist sl : sublist) {
				List<Map<String, Object>> data = sl.getData();
				for (Map<String, Object> datum : data) {
					Map<String, Integer> countObj = ciKpiRelService
							.addCiKpiRel(datum, ciCateMap);
					// MessageResult.broadcast("", progress+flag*i);
					i++;
					if (countObj != null) {
						saveCount = saveCount + countObj.get("save");
						updateCount = updateCount + countObj.get("update");
					}
				}
			}
			// Integer overCount = ciKpiRelService.getCiKpiRelCount();
			if(wsm!=null){
				wsm.broadcast("上传数据完成", 1d);
				wsm.broadcast("新建(" + saveCount + ")条数据", 1d);
				wsm.broadcast("更新(" + updateCount + ")条数据", 1d);
			}
//			MessageResult.broadcast("上传数据完成", 1d);
//			MessageResult.broadcast("新建(" + saveCount + ")条数据", 1d);
//			MessageResult.broadcast("更新(" + updateCount + ")条数据", 1d);
			ret.put("message", "上传完成");
			log.dLog("importKpiCi success");
		} catch (Exception e) {
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return ret.toString();
	}
}
