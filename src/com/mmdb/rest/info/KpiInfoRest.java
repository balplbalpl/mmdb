package com.mmdb.rest.info;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.ExcleJxlReadUtil;
import com.mmdb.core.utils.ExcleJxlReadUtil.Sublist;
import com.mmdb.core.utils.ExcleJxlReadUtil.Table;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.bean.PerformanceBean;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.event.IEventViewService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.service.performance.IPerformanceService;
import com.mmdb.service.performance.impl.PerformanceService;
import com.mmdb.util.FileManager;
import com.mmdb.util.HexString;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class KpiInfoRest extends BaseRest {
	private Log log = LogFactory.getLogger("KpiInfoRest");
	private IKpiCateService cateService;
	private IPerformanceService performanceService;
	private IEventViewService eventViewService;
	private ICiInfoService ciInfoService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService =  context.getBean(IKpiCateService.class);
		performanceService =  new PerformanceService();
		eventViewService = context.getBean(IEventViewService.class);
		ciInfoService =  context.getBean(ICiInfoService.class);
	}

	@Override
	public Representation getHandler() throws Exception{
		String param1 = getValue("param1");

		if (param1 == null || "".equals(param1)) {
			return getAll();
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
		if ("find".equals(param1)) {
			return find(params);
		} else if ("owner".equals(param1)) {
			return getByUser(params);
		}else if ("getbyhex".equals(param1)) {
			return getByHex(params);
		} else if ("simulate".equals(param1)) {
			return simulate(params);
		} else if ("getthresholdline".equals(param1)) {
			return getThresholdLine(params);
		} else if ("export".equals(param1)) {
			// 下载指定分类下的指定的几个数据
			// JSONArray ids = params.getJSONArray("ids");
			// boolean hasData = params.getBoolean("hasData");//
			// 这个没用永远是true
			if (params.containsKey("ids")) {
				JSONArray cateIds = params.getJSONArray("ids");
				return exportData(cateIds, null);
			} else {
				// 下载指定分类下的指定的几个数据
				String cateId = params.getString("cateId");
				JSONArray ciIds = params.getJSONArray("ciIds");
				// boolean hasData = params.getBoolean("hasData");//
				// 这个没用永远是true
				List<String> cateIds = new ArrayList<String>();
				cateIds.add(cateId);
				return exportData(cateIds, ciIds);
			}
		} else {
			return save(params);
		}
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception{
		JSONObject params = parseEntity(entity);
		return edit(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception{
		String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);

		if (param1 == null || "".equals(param1)) {
			if (params != null) {
				if (params.containsKey("jsonIds")) {
					return deleteByIds(params.getJSONArray("jsonIds"));
				} else if (params.containsKey("categoryId")) {
					/*
					 * return deleteByCateId(params
					 * .getJSONArray("categoryIds"));
					 */
					return deleteByCate(params.getString("categoryId"));
				}
			}
		} else {
			return delete(param1);
		}

		return new JsonRepresentation("");
	}

	private Representation getAll() { 
		JSONObject ret = new JSONObject();
		try {
			Map<String, KpiCategory> cache = new HashMap<String, KpiCategory>();

			List<Map<String, Object>> retData = new ArrayList<Map<String, Object>>();

			List<KpiInformation> kpis = cateService.getAllKpi();
			for (KpiInformation kpi : kpis) {

				KpiCategory kpiCate = cache.get(kpi.getKpiCategoryId());
				if (kpiCate == null) {
					kpiCate = cateService.getById(kpi.getKpiCategoryId());
					cache.put(kpiCate.getId(), kpiCate);
				}

				kpi.setKpiCategory(kpiCate);
				Map<String, Object> asMap = kpi.toMap();
				String threshold = (String) asMap.get("threshold");
				JSONArray thJs = JSONArray.fromObject(threshold);
				JSONArray thJsNew = new JSONArray();
				Long cur = System.currentTimeMillis();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat sdfNG = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");
				for (int i = 0; i < thJs.size(); i++) {
					JSONObject th = thJs.getJSONObject(i);
					if ("".equals(th.getString("startTime"))
							&& "".equals(th.getString("endTime"))) {
						th.remove("startTime");
						th.remove("endTime");
					} else {
						th.put("startTime", sdfNG.format(new Date(
								sdf.parse(
										sdfYMD.format(new Date(cur)) + " "
												+ th.getString("startTime"))
										.getTime() - 8L * 3600000L)));
						th.put("endTime", sdfNG.format(new Date(
								sdf.parse(
										sdfYMD.format(new Date(cur)) + " "
												+ th.getString("endTime"))
										.getTime() - 8L * 3600000L)));
					}
					thJsNew.add(th);
				}
				asMap.put("threshold", thJsNew);
				retData.add(asMap);
			}
			ret.put("data", retData);
			ret.put("message", "获取成功");
		} catch (Exception e) {
			e.printStackTrace();
			getResponse().setStatus(new Status(404));
			ret.put("message", "获取失败");
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private Representation getByCateId(String cateId) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			List<KpiInformation> all = cateService.getKpiByCategory(cateId);

			JSONArray list = new JSONArray();
			for (KpiInformation kpi : all) {
				Map<String, Object> asMap = kpi.toMap();
				list.add(asMap);
			}
			ret.put("data", list);
			ret.put("message", "获取全部KPI分类数据成功");
		} catch (Exception e) {
			log.eLog(e);
			//ret.put("message", "获取全部KPI分类数据失败");
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getById(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			KpiInformation kpi = cateService.getKpiById(id);
			if (kpi != null) {
				KpiCategory kpiCate = cateService.getById(kpi
						.getKpiCategoryId());
				kpi.setKpiCategory(kpiCate);
				Map<String, Object> asMap = kpi.toMap();
				String threshold = (String) asMap.get("threshold");
				JSONArray thJs = JSONArray.fromObject(threshold);
				JSONArray thJsNew = new JSONArray();
				Long cur = System.currentTimeMillis();
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				SimpleDateFormat sdfNG = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");
				for (int i = 0; i < thJs.size(); i++) {
					JSONObject th = thJs.getJSONObject(i);
					if ("".equals(th.getString("startTime"))
							&& "".equals(th.getString("endTime"))) {
						th.remove("startTime");
						th.remove("endTime");
					} else {
						th.put("startTime", sdfNG.format(new Date(
								sdf.parse(
										sdfYMD.format(new Date(cur)) + " "
												+ th.getString("startTime"))
										.getTime() - 8L * 3600000L)));
						th.put("endTime", sdfNG.format(new Date(
								sdf.parse(
										sdfYMD.format(new Date(cur)) + " "
												+ th.getString("endTime"))
										.getTime() - 8L * 3600000L)));
					}
					thJsNew.add(th);
				}
				asMap.put("threshold", thJsNew);
				System.out.println(asMap);
				ret.put("data", asMap);
				ret.put("message", "获取KPI成功");
			} else {
				//ret.put("message", "获取KPI失败");
				//getResponse().setStatus(new Status(600));
				throw new Exception("获取KPI失败");
			}
		} catch (Exception e) {
			log.eLog(e);
			/*ret.put("message", "获取KPI失败");
			getResponse().setStatus(new Status(600));*/
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByHex(JSONObject obj)  throws Exception{
		JSONObject ret = new JSONObject();
		String hexId = "";
		try {
			hexId = obj.getString("hexId");
			KpiInformation kpi = cateService.getKpiByHex(hexId);
			if (kpi != null) {
				KpiCategory kpiCate = cateService.getById(kpi
						.getKpiCategoryId());
				kpi.setKpiCategory(kpiCate);
				Map<String, Object> asMap = kpi.toMap();
				ret.put("data", asMap);
				ret.put("message", "获取KPI成功");
			} else {
				ret.put("message", "获取KPI失败");
				throw new Exception("获取KPI失败");
			}
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportData(List<String> cateIds, List<String> kpiIds) throws Exception{
		JSONObject ret = new JSONObject();

		log.dLog("getAllForXls");
		File file = FileManager.getInstance().createFile("KPI数据", "xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			List<KpiCategory> ncs = cateService.getAll();
			int i = 0;

			WritableWorkbook wb = Workbook.createWorkbook(file);
			for (KpiCategory nc : ncs) {
				List<KpiInformation> dataList = new LinkedList<KpiInformation>();
				String id = nc.getId();
				String name = nc.getName();
				List<Attribute> items = KpiCategory.attrList;
				if (cateIds == null || cateIds.size() == 0
						|| cateIds.contains(id)) {
					WritableSheet ws = wb.createSheet(name, i);
					List<String> title = this.createXlsSheet(ws, items);
					List<KpiInformation> kpiList = 
							cateService.find(id,null,null,-1,-1);

					// 对于选定的id进行过滤
					if (kpiIds != null && kpiIds.size() > 0) {
						for (KpiInformation info : kpiList) {

							String jsonId = String.valueOf(info.getId());
							if (kpiIds.contains(jsonId)) {
								dataList.add(info);
							}
						}
					} else {
						dataList.addAll(kpiList);
					}

					this.addCell(ws, title, dataList);
					// dataList.clear();
					i++;
				}
			}
			if (wb.getNumberOfSheets() == 0) {
				wb.createSheet("Sheet1", i);
			}
			wb.write();
			wb.close();
			ret.put("message", "下载KPI数据成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
		} catch (Exception e) {
			log.eLog("下载KPI数据发生异常"+e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private void addCell(WritableSheet ws, List<String> title,
			List<KpiInformation> infos) throws WriteException {
		int v = 1;
		for (KpiInformation info : infos) {
			//Map<String, Object> data = new HashMap<String, Object>();

			Label label = new Label(0, v, info.getName());
			ws.addCell(label);

			label = new Label(1, v, info.getKpiCategoryName());
			ws.addCell(label);

			label = new Label(2, v, info.getThreshold());
			ws.addCell(label);

			label = new Label(3, v, info.getUnit());
			ws.addCell(label);

			label = new Label(4, v, info.getOwner());
			ws.addCell(label);
			v++;
		}
	}

	private List<String> createXlsSheet(WritableSheet ws, List<Attribute> items)
			throws WriteException {
		WritableFont wf_color = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		WritableCellFormat wff_color = new WritableCellFormat(wf_color);
		wff_color.setBackground(Colour.GRAY_25);
		WritableFont wf_color2 = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE);
		WritableCellFormat wff_color2 = new WritableCellFormat(wf_color2);
		List<String> title = new ArrayList<String>();
		int temp = 0;

		for (int j = 0; j < items.size(); j++) {
			Attribute map = items.get(j);
			String required = map.getRequired().toString();
			Label label = null;
			if (Boolean.valueOf(required)) {
				label = new Label(j + temp, 0, map.getName(), wff_color);
			} else {
				label = new Label(j + temp, 0, map.getName(), wff_color2);
			}
			title.add(map.getName());
			WritableCellFeatures wf = new WritableCellFeatures();
			// String info = "数据类型：" + map.getType().getName() + "\n";
			String info = "是否必填：" + map.getRequired() + "\n";
			if (map.getRequired()) {
				info += "缺省数值：" + map.getDefaultValue() + "\n";
			}
			// info += "数据来源：" + map.getSources() + "\n";
			wf.setComment(info);
			label.setCellFeatures(wf);
			// label 可以设置宽度
			ws.addCell(label);
		}

		return title;
	}

	private Representation importData(Representation entity) throws Exception{
		log.dLog("importXls");
		JSONObject ret = new JSONObject();

		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			e.printStackTrace();
			throw e;
		}

		String filename = "";
		FileItem fi = items.get(0);
		try {
			String userName = this.getUsername();
			filename = fi.getName();
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf("xls") == -1) {
				log.eLog("文件格式有误");
				throw new Exception("文件格式有误");
			}

			String info = "";
			log.dLog("正在解析EXCEL，请等待...");
			WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
			if(wsm!=null){
				wsm.broadcast("正在解析EXCEL，请等待...", 0.1d);
			}
//			MessageResult.broadcast("正在解析EXCEL，请等待...", 0.1d);

			ExcleJxlReadUtil ejr = new ExcleJxlReadUtil(fi.getInputStream());
			Table table = ejr.getTable();
			List<Sublist> sublist = table.getSublist();
			if(wsm!=null){
				wsm.broadcast("准备分类，请等待...", 0.4d);
			}
//			MessageResult.broadcast("准备分类，请等待...", 0.4d);
			List<KpiCategory> all = cateService.getAll();
			Map<String, KpiCategory> allMap = new HashMap<String, KpiCategory>();
			for (KpiCategory cate : all) {
				allMap.put(cate.getName(), cate);
			}
			int totle = 0;
			for (Sublist sl : sublist) {
				totle += sl.getData().size();
			}
			double flag = 0.5d / totle;
			double progress = 0.5;
			if(wsm!=null){
				wsm.broadcast("开始解析sheet，请等待...", progress);
			}
//			MessageResult.broadcast("开始解析sheet，请等待...", progress);
			
			for (Sublist sl : sublist) {
				String name = sl.getName();
				log.iLog("解析sheet[" + name + "]...");
				if(wsm!=null){
					wsm.broadcast("解析sheet[" + name + "]...", progress);
				}
//				MessageResult.broadcast("解析sheet[" + name + "]...", progress);

				if (name.indexOf("-") != -1) {
					String[] cs = name.split("-");
					name = cs[cs.length - 1];
				}
				KpiCategory nc = allMap.get(name);
				if (nc == null) {
					info += "分类[" + name + "]不存在";
					String in = "分类[" + name + "]不存在,无法新建数据";
					if(wsm!=null){
						wsm.broadcast(in, progress);
					}
//					MessageResult.broadcast(in, progress);
				} else {
					Map<String, String> t = new HashMap<String, String>();
					List<KpiInformation> cis = new ArrayList<KpiInformation>();
					List<Map<String, Object>> data = sl.getData();
					if (data.size() == 0) {
						if(wsm!=null){
							wsm.broadcast("分类[" + name + "]无数据", progress);
						}
//						MessageResult
//								.broadcast("分类[" + name + "]无数据", progress);
					}
					for (Map<String, Object> obj : data) {
						try {

							
							KpiInformation kpi = new KpiInformation(nc, "XLS",
									obj);
							String hexId = HexString.encode(HexString.json2Str(
									kpi.getKpiCategoryName(), kpi.getName()));
							kpi.setKpiHex(hexId);
							
							//如果没有填写"所有者"字段，则以当前用户作为此KPI的所有者
							if(kpi.getOwner()==null || "".equals(kpi.getOwner())){
								kpi.setOwner(userName);
							}
							
							if (!t.containsKey(hexId)) {
								t.put(hexId, hexId);
								cis.add(kpi);
							}
						} catch (Exception e) {
							String in = e.getMessage();
							if(wsm!=null){
								wsm.broadcast(in, progress);
							}
//							MessageResult.broadcast(in, progress);
						}
					}
					if (cis.size() > 0) {
						log.iLog("新建/更新数据开始...");
						if(wsm!=null){
							wsm.broadcast("新建/更新数据开始...", progress);
						}
//						MessageResult.broadcast("新建/更新数据开始...", progress);
						Map<String, Long> rm = cateService
								.saveOrUpdate(nc, cis);
						String in = "分类[" + name + "]";
						if (!rm.get("save").equals("0")) {
							in += "新建(" + rm.get("save") + ")条新数据";
						}
						if (!rm.get("update").equals("0")) {
							in += "更新(" + rm.get("update") + ")条数据";
						}
						log.iLog(in);
						if(wsm!=null){
							wsm.broadcast(in, progress);
						}
//						MessageResult.broadcast(in, progress);
						progress += flag * cis.size();
						info = info.equals("") ? info : info + "<br/>";
						info += in;
					} else {
						info += "分类[" + name + "]数据不符合要求";
					}
				}
			}
			if(wsm!=null){
				wsm.broadcast("上传数据完成", 1);
			}
//			MessageResult.broadcast("上传数据完成", 1);
			ret.put("message", "上传完成");
			log.dLog("updateForXls success");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过分类ID和匹配条件查询KPI
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private Representation find(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		int page = -1;
		int pageSize = 1000;
		if(obj.containsKey("page")){
			page = obj.getInt("page");
			pageSize = obj.getInt("pageSize");
		}

		try {
			int count = 0 ;
			String kpiCategoryId = obj.containsKey("kpiCategoryId") ? obj
					.getString("kpiCategoryId") : null;
			String param = obj.containsKey("input") ? obj.getString("input")
					: null;
			Boolean extend = obj.containsKey("all") ? obj.getBoolean("all")
					: false;
			List<KpiInformation> kpis = new ArrayList<KpiInformation>();
			if (extend) {
				count = cateService.countFindAllByCate(kpiCategoryId, param, null);
				kpis = cateService.findAllByCate(kpiCategoryId, param, null,page,pageSize);
			} else {
				count = cateService.countFind(kpiCategoryId, param, null);
				kpis = cateService.find(kpiCategoryId, param, null,page,pageSize);
			}
			
			//用户下的KPI数量应该不会很多，因此内存分页
/*			int count = kpis.size();
			int start = (page - 1) * pageSize;
			start = start < 0 ? 0 : start;
			int end = page * pageSize;
			start = start > count ? count : start;
			end = end > count ? count : end;
			kpis = kpis.subList(start, end);*/
			Map<String, KpiCategory> cateMap = this.getKpiCateMap();
			JSONArray jsArr = new JSONArray();
			for (KpiInformation kpi : kpis) {
				kpi.setKpiCategory(cateMap.get(kpi.getKpiCategoryId()));
				jsArr.add(kpi.toMap());
			}
			
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("count", count);
			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("datas", jsArr);
			
			ret.put("data", retMap);
			ret.put("message", "获取KPI成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取到某个用户的KPI信息
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getByUser(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		int page = obj.getInt("page");
		int pageSize = obj.getInt("pageSize");
		String kpiCategoryId = obj.containsKey("kpiCategoryId") ? obj
				.getString("kpiCategoryId") : null;
		String param = obj.containsKey("input") ? obj.getString("input")
				: null;
		Boolean extend = obj.containsKey("all") ? obj.getBoolean("all")
				: false;
		
		try {
			int count = 0;
			String userName = this.getUsername();
			List<KpiInformation> kpis = new ArrayList<KpiInformation>();
			if (extend) {
				count = cateService.countFindAllByCate(kpiCategoryId, param, userName);
				kpis = cateService.findAllByCate(kpiCategoryId, param, userName,page,pageSize);
			} else {
				count = cateService.countFind(kpiCategoryId, param, userName);
				kpis = cateService.find(kpiCategoryId, param, userName,page,pageSize);
			}
			//用户下的KPI数量应该不会很多因此内存分页
			/*int count = kpis.size();
			int start = (page - 1) * pageSize;
			start = start < 0 ? 0 : start;
			int end = page * pageSize;
			start = start > count ? count : start;
			end = end > count ? count : end;
			kpis = kpis.subList(start, end);*/
			
			Map<String, KpiCategory> cateMap = this.getKpiCateMap();
			JSONArray jsArr = new JSONArray();
			for (KpiInformation kpi : kpis) {
				kpi.setKpiCategory(cateMap.get(kpi.getKpiCategoryId()));
				jsArr.add(kpi.toMap());
			}
			
			Map<String, Object> retMap = new HashMap<String, Object>();
			retMap.put("count", count);
			retMap.put("page", page);
			retMap.put("pageSize", pageSize);
			retMap.put("datas", jsArr);
			
			ret.put("data", retMap);
			ret.put("message", "获取KPI成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	private Representation save(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.dLog("save");
			if (obj == null || obj.size() == 0) {
				throw new MException("save参数不能空");
			}
			String name = obj.getString("name");
			if (name == null || name.equals("")) {
				throw new MException("名称不能空");
			}

			String kpiCategoryId = obj.containsKey("kpiCategoryId") ? obj
					.getString("kpiCategoryId") : "";

			KpiInformation k = cateService.getKpiByName(kpiCategoryId, name);
			if (k != null) {
				throw new MException("KPI [" + name + "] 已存在");
			}

			KpiInformation kpi = new KpiInformation();
			kpi.setName(name);
			kpi.setKpiCategoryId(kpiCategoryId);
			String hexId = "";
			if (kpiCategoryId.length() > 0) {
				KpiCategory kpiCate = cateService.getById(kpiCategoryId);
				hexId = HexString.encode(HexString.json2Str(kpiCate.getName(),
						name));
				kpi.setKpiCategoryName(kpiCate.getName());
			}
			kpi.setKpiHex(hexId);
			String threshold = obj.containsKey("threshold") ? obj
					.getString("threshold") : "";
			threshold = examineThreshold(threshold);
			if (threshold == null) {
				throw new Exception("阈值设定错误");
			}
			kpi.setThreshold(threshold);
			kpi.setUnit(obj.containsKey("unit") ? obj.getString("unit")
					.replace("@@@", "%") : "");
			kpi.setSource(obj.containsKey("source") ? obj.getString("source")
					: "");
			//创建者
			kpi.setOwner(getUsername());
			
			kpi = cateService.save(kpi);
			KpiCategory kpiCate = cateService.getById(kpi.getKpiCategoryId());
			kpi.setKpiCategory(kpiCate);
			
			Map<String, Object> asMap = kpi.toMap();
			ret.put("data", asMap);
			ret.put("message", "保存成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation edit(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.dLog("edit");
			if (obj == null || obj.size() == 0) {
				throw new MException("save参数不能空");
			}
			String id = obj.getString("id");
			if (id == null || id.equals("")) {
				throw new MException("id不能空");
			}
			KpiInformation kpi = cateService.getKpiById(id);
			if (kpi == null) {
				throw new MException(id + "KPI不存在");
			}

			String name = obj.containsKey("name") ? obj.getString("name") : "";
			kpi.setName(name);
			String kpiCategoryId = obj.containsKey("kpiCategoryId") ? obj
					.getString("kpiCategoryId") : "";
			kpi.setKpiCategoryId(kpiCategoryId);
			String hexId = "";
			if (kpiCategoryId.length() > 0) {
				KpiCategory kpiCate = cateService.getById(kpiCategoryId);
				hexId = HexString.encode(HexString.json2Str(kpiCate.getName(),
						name));
				kpi.setKpiCategoryName(kpiCate.getName());
			}
			kpi.setKpiHex(hexId);
			String threshold = obj.containsKey("threshold") ? obj
					.getString("threshold") : "";
			threshold = examineThreshold(threshold);
			if (threshold == null) {
				throw new MException("阈值设定错误");
			}
			kpi.setThreshold(threshold);
			kpi.setUnit(obj.containsKey("unit") ? obj.getString("unit")
					.replace("@@@", "%") : "");
			kpi.setSource(obj.containsKey("source") ? obj.getString("source")
					: "");
			kpi = cateService.update(kpi);
			KpiCategory kpiCate = cateService.getById(kpi.getKpiCategoryId());
			kpi.setKpiCategory(kpiCate);
			Map<String, Object> asMap = kpi.toMap();
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
		try {
			cateService.deleteAllKpi();
			ret.put("message", "删除全部成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.eLog(e.getMessage());
			throw e;
		}

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过KPI ID删除某个KPI
	 * 
	 * @param id （kpi的mongoId)
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation delete(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			//获取当前用户名
			String userName = this.getUsername();
			//是否为管理员
			boolean isAdmin = this.isAdmin();
			log.dLog("delete");
			if (id == null || id.equals("")) {
				throw new MException("参数不能空");
			}
			KpiInformation kpi = cateService.getKpiById(id);
			if (kpi == null) {
				throw new MException("KPI[" + id + "]不存在");
			}
			if(!isAdmin){ //非管理员用户只能删除自己的KPI
				String owner = kpi.getOwner();
				if(!owner.equals(userName)){
					throw new MException("没有权限删除其他用户的KPI!");
				}
			}
			cateService.delete(kpi);

			ret.put("message", "删除成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过kpiId 批量删除KPI
	 * 
	 * @param kpiIds
	 * @return
	 */
	private JsonRepresentation deleteByCate(String cateId) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			String userName = this.getUsername();
			//是否为管理员用户
			boolean isAdmin = this.isAdmin();
			log.dLog("deleteByCateId");
			KpiCategory cate = cateService.getById(cateId);
			//普通用户不能删除其他用户的分类数据
			if(!isAdmin){
				String owner = cate.getOwner();
				if(!owner.equals(userName)){
					throw new MException("没有权限删除其他用户的分类数据!");
				}
			}
			cateService.deleteByCategory(cate);
			ret.put("message", "删除成功");
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 通过kpiId 批量删除KPI
	 * 
	 * @param kpiIds
	 * @return
	 */
	private JsonRepresentation deleteByIds(List<String> kpiIds) throws Exception{
		JSONObject ret = new JSONObject();
		String userName = this.getUsername();
		//管理员可以删除所有
		boolean isAdmin = this.isAdmin();
		log.dLog("deleteById");
		if(!isAdmin){
			//普通用户只能删除自己的KPI
			List<KpiInformation> kpiList = cateService.getKpiByIds(kpiIds);
			for(KpiInformation kpi:kpiList){
				String owner = kpi.getOwner();
				if(!owner.equals(userName)){
					throw new MException("没有权限删除其他用户的KPI!");
				}
			}
		}
		cateService.deleteKpiByIds(kpiIds);
		ret.put("message", "删除成功");
		
		return new JsonRepresentation(ret.toString());
	}

	private Map<String, KpiCategory> getKpiCateMap() throws Exception {
		List<KpiCategory> cates = cateService.getAll();
		Map<String, KpiCategory> map = new HashMap<String, KpiCategory>();
		for (KpiCategory cate : cates) {
			map.put(cate.getId(), cate);
		}
		return map;
	}

	private Representation simulate(JSONObject obj) throws Exception{
		System.out.println(obj.toString());
		JSONObject ret = new JSONObject();
		try {
			if (obj == null || obj.size() == 0) {
				throw new MException("参数不能空");
			}
			String id = obj.getString("id");
			if (id == null || id.equals("")) {
				throw new MException("id不能空");
			}
			KpiInformation kpi = cateService.getKpiById(id);
			if (kpi == null) {
				throw new MException(id + "KPI不存在");
			}
			Long lastTime = obj.getLong("lastTime");
			// 阈值如果为空的情况
			if ("".equals(obj.getString("threshold"))) {
				ret.put("message", "阈值设置不能为空");
				getResponse().setStatus(new Status(600));
				return new JsonRepresentation(ret.toString());
			}
			List<Map<String, String>> severityMap = eventViewService
					.getSeverityMap();
			Map<Map<String, String>, Map<String, Long>> retMap = new HashMap<Map<String, String>, Map<String, Long>>();
			Map<Map<String, String>, Map<String, Object>> repeatCountMap = new HashMap<Map<String, String>, Map<String, Object>>();

			Long endTime = System.currentTimeMillis();
			Long startTime = endTime - lastTime;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfNG = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			Map<Integer, List<PerformanceBean>> perfMap = performanceService
					.getAllPerformanceDatasByInstance("", "", "", id, "",
							sdf.format(startTime), sdf.format(endTime), 0,
							50000); //modify at 2015-8-29，1000000条数据太大，Mongo会报错，这里改为50000条
			Set<Integer> perfSet = perfMap.keySet();
			for (Integer i : perfSet) {
				if (i > 0) {
					List<PerformanceBean> perfList = perfMap.get(i);

					Long repeat = obj.getLong("repeat");
					String st = obj.containsKey("startTime") ? obj
							.getString("startTime") : "";
					if (!st.equals("null") && st.length() > 0) {
						Date d = sdfNG.parse(st);
						st = sdf.format(new Date(d.getTime() + 8L * 3600000L))
								.substring(11);
					} else {
						st = "00:00:00";
					}
					String et = obj.containsKey("endTime") ? obj
							.getString("endTime") : "";
					if (!et.equals("null") && et.length() > 0) {
						Date d = sdfNG.parse(et);
						et = sdf.format(new Date(d.getTime() + 8L * 3600000L))
								.substring(11);
					} else {
						et = "23:59:59";
					}

					for (int k = perfList.size() - 1; k >= 0; k--) {
						PerformanceBean pb = perfList.get(k);
						Map<String, String> ciMap = new HashMap<String, String>();
						ciMap.put("ciCate", pb.getCiCate());
						ciMap.put("ci", pb.getCiName());
						ciMap.put("instance", pb.getInstance() == null ? ""
								: pb.getInstance());
						Map<String, Long> thMap = retMap.get(ciMap);
						Map<String, Object> rcMap = repeatCountMap.get(ciMap);
						if (thMap == null) {
							thMap = new HashMap<String, Long>();
							rcMap = new HashMap<String, Object>();
							for (Map<String, String> severityConfig : severityMap) {
								if (!severityConfig.get("id").equals("0")) {
									thMap.put(severityConfig.get("id"), 0L);
								}
							}
							retMap.put(ciMap, thMap);
							repeatCountMap.put(ciMap, rcMap);
						}
						String time = pb.getStartTime();
						String timeYMD = time.substring(0, 11);
						Long stL = sdf.parse(timeYMD + st).getTime();
						Long etL = sdf.parse(timeYMD + et).getTime();
						Long timeL = sdf.parse(time).getTime();
						if (timeL >= stL && timeL <= etL) {
							Double val = Double.parseDouble(pb.getValue());
							JSONArray thresholds = obj
									.getJSONArray("threshold");
							for (int j = 0; j < thresholds.size(); j++) {
								JSONObject th = thresholds.getJSONObject(j);
								if (val >= th.getDouble("lowLimit")
										&& val < th.getDouble("highLimit")) {
									if (rcMap.size() == 0) {
										rcMap.put("severity",
												th.getString("severity"));
										rcMap.put("repeat", 0L);
										rcMap.put("date", timeYMD);
									} else {
										if (th.getString("severity").equals(
												rcMap.get("severity"))) {
											if (timeYMD.equals(rcMap
													.get("date"))) {
												rcMap.put(
														"repeat",
														(Long) rcMap
																.get("repeat") + 1L);
											} else {
												rcMap.put("repeat", 0L);
												rcMap.put("date", timeYMD);
											}
										} else {
											rcMap.put("severity",
													th.getString("severity"));
											rcMap.put("repeat", 0L);
											rcMap.put("date", timeYMD);
										}
									}
									if ((Long) rcMap.get("repeat") >= repeat) {
										thMap.put(
												th.getString("severity"),
												thMap.get(th
														.getString("severity")) + 1L);
									}
								}
							}
						}
					}
				}
				break;
			}

			JSONArray data = new JSONArray();
			Set<Map<String, String>> ciSet = retMap.keySet();
			for (Map<String, String> ci : ciSet) {
				Map<String, Long> val = retMap.get(ci);
				JSONObject row = new JSONObject();
				JSONObject cv1 = new JSONObject();
				cv1.put("value", ci.get("ciCate"));
				cv1.put("color", "");
				row.put("CI分类", cv1);
				JSONObject cv2 = new JSONObject();
				cv2.put("value", ci.get("ci"));
				cv2.put("color", "");
				row.put("CI", cv2);
				JSONObject cv3 = new JSONObject();
				cv3.put("value", ci.get("instance"));
				cv3.put("color", "");
				row.put("实例", cv3);
				for (Map<String, String> severityConfig : severityMap) {
					if (!severityConfig.get("id").equals("0")) {
						JSONObject cv = new JSONObject();
						cv.put("value", val.get(severityConfig.get("id")));
						cv.put("color", severityConfig.get("color"));
						row.put(severityConfig.get("name"), cv);
					}
				}
				data.add(row);
			}
			JSONArray title = new JSONArray();
			title.add("CI分类");
			title.add("CI");
			title.add("实例");
			for (Map<String, String> severityConfig : severityMap) {
				if (!severityConfig.get("id").equals("0")) {
					title.add(severityConfig.get("name"));
				}
			}
			JSONObject r = new JSONObject();
			r.put("data", data);
			r.put("title", title);
			ret.put("data", r);
			ret.put("message", "模拟告警成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getThresholdLine(JSONObject obj) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			if (obj == null || obj.size() == 0) {
				throw new Exception("参数不能空");
			}
			String id = obj.getString("id");
			if (id == null || id.equals("")) {
				throw new Exception("id不能空");
			}
			KpiInformation kpi = cateService.getKpiById(id);
			if (kpi == null) {
				throw new Exception(id + "KPI不存在");
			}
			Long lastTime = obj.getLong("lastTime");
			String ci = obj.getString("ci");
			String ciCate = obj.getString("ciCate");
			String kpiInstance = obj.containsKey("kpiInstance") ? obj
					.getString("kpiInstance") : "";
			CiInformation info = ciInfoService.getById(HexString
					.encode(HexString.json2Str(ciCate, ci)));
			List<Map<String, String>> severityMap = eventViewService
					.getSeverityMap();
			List<Object> data = new ArrayList<Object>();
			List<String> x = new ArrayList<String>();

			Long endTime = System.currentTimeMillis();
			Long startTime = endTime - lastTime;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// SimpleDateFormat sdfNG = new
			// SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfNG = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			// System.out.println(info.getCategoryId() +"    "+info.getId());
			Map<Integer, List<PerformanceBean>> perfMap = performanceService
					.getAllPerformanceDatasByInstance(info.getCategoryId(),
							info.getId(), "", id, kpiInstance,
							sdf.format(startTime), sdf.format(endTime), 0,
							1000000);
			Set<Integer> perfSet = perfMap.keySet();
			for (Integer i : perfSet) {
				if (i > 0) {
					List<PerformanceBean> perfList = perfMap.get(i);
					String st = obj.containsKey("startTime") ? obj
							.getString("startTime") : "";
					if (!st.equals("null") && st.length() > 0) {
						Date d = sdfNG.parse(st);
						st = sdf.format(new Date(d.getTime() + 8L * 3600000L))
								.substring(11);
					} else {
						st = "00:00:00";
					}
					String et = obj.containsKey("endTime") ? obj
							.getString("endTime") : "";
					if (!et.equals("null") && et.length() > 0) {
						Date d = sdfNG.parse(et);
						et = sdf.format(new Date(d.getTime() + 8L * 3600000L))
								.substring(11);
					} else {
						et = "23:59:59";
					}

					data.add(kpi.getName());
					for (int j = perfList.size() - 1; j >= 0; j--) {
						PerformanceBean pb = perfList.get(j);
						String time = pb.getStartTime();
						String timeYMD = time.substring(0, 11);
						Long stL = sdf.parse(timeYMD + st).getTime();
						Long etL = sdf.parse(timeYMD + et).getTime();
						Long timeL = sdf.parse(time).getTime();
						if (timeL >= stL && timeL <= etL) {
							data.add(Double.parseDouble(pb.getValue()));
							x.add(pb.getStartTime());
						}
					}
				}
				break;
			}

			JSONObject r = new JSONObject();
			r.put("data", data);
			r.put("x", x);
			JSONArray severity = new JSONArray();
			for (Map<String, String> m : severityMap) {
				severity.add(m.get("color"));
			}
			r.put("severity", severity);
			List<Map<String, Object>> thresholdValList = new ArrayList<Map<String, Object>>();
			JSONArray thresholds = obj.getJSONArray("threshold");
			for (int j = thresholds.size() - 1; j >= 0; j--) {
				JSONObject th = thresholds.getJSONObject(j);
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("axis", "y");
				m.put("start", th.getDouble("lowLimit"));
				m.put("end", th.getDouble("highLimit"));
				m.put("cls", "regionLevel" + th.getString("severity"));
				thresholdValList.add(m);
			}
			r.put("y", thresholdValList);
			ret.put("data", r);
			ret.put("message", "生成曲线图成功");
		} catch (Exception e) {
			log.eLog(e);
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private String examineThreshold(String str) throws Exception{
		try {
			JSONArray ret = new JSONArray();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdfNG = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyy-MM-dd");
			Long cur = System.currentTimeMillis();
			JSONArray thresholds = JSONArray.fromObject(str);
			List<String[]> ths = new ArrayList<String[]>();
			for (int i = 0; i < thresholds.size(); i++) {
				JSONObject threshold = thresholds.getJSONObject(i);
				String thresholdsConfig =  threshold.getString("threshold");
				if(!"".equals(thresholdsConfig)){
					JSONArray lowHighs = threshold.getJSONArray("threshold");
					List<Double[]> lhs = new ArrayList<Double[]>();
					for (int j = 0; j < lowHighs.size(); j++) {
						JSONObject lowHigh = lowHighs.getJSONObject(j);
						Double[] lh = new Double[2];
						lh[0] = lowHigh.getDouble("lowLimit");
						lh[1] = lowHigh.getDouble("highLimit");
						lhs.add(lh);
					}
					for (int j = 0; j < lhs.size(); j++) {
						Double[] a = lhs.get(j);
						if (a[0] >= a[1]) {
							return null;
						}
						for (int k = j + 1; k < lhs.size(); k++) {
							Double[] b = lhs.get(k);
							if (a[0] == b[0] || a[0] < b[0] && a[1] > b[0]
									|| a[0] > b[0] && b[1] > a[0]) {
								return null;
							}
						}
					}
				}

				String[] th = new String[2];
				th[0] = threshold.containsKey("startTime") ? threshold
						.getString("startTime") : "";
				if (th[0].length() > 0 && !"null".equals(th[0])) {
					Date d = sdfNG.parse(th[0]);
					th[0] = sdf.format(new Date(d.getTime() + 8L * 3600000L))
							.substring(11);
				} else {
					th[0] = "00:00:00";
				}
				threshold.put("startTime", th[0]);
				th[1] = threshold.containsKey("endTime") ? threshold
						.getString("endTime") : "";
				if (th[1].length() > 0 && !"null".equals(th[1])) {
					Date d = sdfNG.parse(th[1]);
					th[1] = sdf.format(new Date(d.getTime() + 8L * 3600000L))
							.substring(11);
				} else {
					th[1] = "23:59:59";
				}
				threshold.put("endTime", th[1]);
				ths.add(th);
				if ("00:00:00".equals(th[0]) && "23:59:59".equals(th[1])) {
					threshold.put("startTime", "");
					threshold.put("endTime", "");
				}
				ret.add(threshold);
			}
			for (int i = 0; i < ths.size(); i++) {
				String[] a = ths.get(i);
				Long as = sdf.parse(sdfYMD.format(new Date(cur)) + " " + a[0])
						.getTime();
				Long ae = sdf.parse(sdfYMD.format(new Date(cur)) + " " + a[1])
						.getTime();
				if (as >= ae) {
					return null;
				}
				for (int j = i + 1; j < ths.size(); j++) {
					String[] b = ths.get(j);
					Long bs = sdf.parse(
							sdfYMD.format(new Date(cur)) + " " + b[0])
							.getTime();
					Long be = sdf.parse(
							sdfYMD.format(new Date(cur)) + " " + b[1])
							.getTime();
					if (bs >= be) {
						return null;
					}
					if (as == bs || as < bs && ae >= bs || as > bs && as <= be) {
						return null;
					}
				}
			}
			return ret.toString();
		} catch (Exception e) {
			log.eLog(e.getMessage());
			return null;
		}
	}
	
}