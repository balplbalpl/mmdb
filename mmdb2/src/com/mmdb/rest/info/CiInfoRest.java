package com.mmdb.rest.info;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.rest.BaseRest;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.info.ICiInfoService;
import com.mmdb.util.FileManager;
import com.mmdb.util.HexString;
import com.mmdb.websocket.WebSocketMapping;
import com.mmdb.websocket.WebSocketMessage;

public class CiInfoRest extends BaseRest {
	private Log log = LogFactory.getLogger("CiInfoBuzImpl");
	private ICiCateService cateService;
	private ICiInfoService infoService;

	@Override
	public void ioc(ApplicationContext context) {
		cateService = context.getBean(ICiCateService.class);
		infoService = context.getBean(ICiInfoService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {// 导出全部,包含内容
			return exportData(null, null, true);
		} else {
			return getByJsonid(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if ("import".equals(param1)) {
			return new JsonRepresentation(importData(entity));
		}

		JSONObject params = parseEntity(entity);

		if (param1 == null || "".equals(param1)) {// 添加一个ci
			if (params.size() == 2 && params.containsKey("categoryId")
					&& params.containsKey("data")) {
				return saveCi(params);
			} else if (params.size() == 1 && params.containsKey("ids")) {
				JSONArray jsonIds = params.getJSONArray("ids");
				return getByJsonids(jsonIds);
			} else {
				return qureyByAdvanced(params);
			}
		} else if ("export".equals(param1)) {// 带条件的导出
			if (params.containsKey("ids") && params.containsKey("hasData")) {
				JSONArray cateIds = params.getJSONArray("ids");
				boolean hasData = params.getBoolean("hasData");
				return exportData(cateIds, null, hasData);
			} else {
				// 下载指定分类下的指定的几个数据
				String cateId = params.getString("cateId");
				JSONArray ciIds = params.getJSONArray("ciIds");

				List<String> cateIds = new ArrayList<String>();
				cateIds.add(cateId);
				return exportData(cateIds, ciIds, true);
			}
		} else if ("getmyci".equals(param1)) {// 我的CI
			return getMyCi(params);
		}
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		if (params.size() == 2 && params.containsKey("jsonId")
				&& params.containsKey("data")) {
			return editCi(params);
		}
		return notFindMethod(entity);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		JSONObject params = parseEntity(entity);
		if (param1 == null || "".equals(param1)) {
			if (params != null) {
				if (params.containsKey("jsonIds")) {
					return deleteByjsonId(params.getJSONArray("jsonIds"));
				} else if (params.containsKey("categoryIds")) {
					return deleteByCateId(params.getJSONArray("categoryIds"));
				}
			}
		} else {// 删除一个属性
			ArrayList<String> jsonids = new ArrayList<String>();
			jsonids.add(param1);
			return deleteByjsonId(jsonids);// param1应该是jsonId
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
		List<CiInformation> all = infoService.getAll();
		JSONArray data = new JSONArray();
		for (CiInformation info : all) {
			data.add(info.asMapForRest());
		}
		ret.put("data", data);
		ret.put("message", "获取全部配置项数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取ciCategory
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getByJsonid(String id) throws Exception {
		JSONObject ret = new JSONObject();
		CiInformation info = infoService.getById(id);
		if (info != null) {
			ret.put("data", info.asMapForRest());
			ret.put("message", "获取配置项数据[" + info.getName() + "]成功");
		} else {
			throw new MException("配置项数据不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByJsonids(List<String> jsonIds) throws Exception {
		JSONObject ret = new JSONObject();
		if (jsonIds == null || jsonIds.size() == 0) {
			throw new Exception("JSON参数不能为空");
		}
		List<CiInformation> infos = infoService.getByIds(jsonIds);
		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		for (CiInformation info : infos) {
			map.put(info.getCiHex(), info.asMapForRest());
		}
		ret.put("data", map);
		ret.put("message", "获取数据成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 模糊查询
	 * 
	 * @param data
	 *            {'categoryId':'','like':{'*':'*
	 *            '},'page':1,pageSize:10,'all':true}
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private JsonRepresentation qureyByAdvanced(JSONObject data)
			throws Exception {
		// TODO 将分页写到数据库中....
		JSONObject ret = new JSONObject();
		String cateId = data.getString("categoryId");
		Boolean extend = data.getBoolean("all");
		JSONObject orExp = data.getJSONObject("like");
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");

		List<CiInformation> list;
		int count;
		if (orExp == null) {
			throw new MException("查询条件不能为空");
		}
		if (cateId == null || cateId.equals("")) {
			Map<String, Object> m = infoService.qureyByAdvanced(null, null,
					orExp, extend == null ? true : extend, null, (page - 1)
							* pageSize, pageSize);
			list = (List<CiInformation>) m.get("data");
			count = (Integer) m.get("count");
		} else {
			CiCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new MException("分类不存在");
			}
			Map<String, Object> m = infoService.qureyByAdvanced(nc, null,
					orExp, extend == null ? true : extend, null, (page - 1)
							* pageSize, pageSize);
			list = (List<CiInformation>) m.get("data");
			count = (Integer) m.get("count");
		}
		// 排序
		// Collections.sort(list, new Comparator<CiInformation>() {
		// @Override
		// public int compare(CiInformation arg0, CiInformation arg1) {
		// return arg0.getName().compareTo(arg1.getName());
		// }
		// });
		// 构建分页数据输出
		List<Object> datas = new ArrayList<Object>();
		// int count = list.size();
		// int start = (page - 1) * pageSize;
		// start = start < 0 ? 0 : start;
		// int end = page * pageSize;
		// start = start > count ? count : start;
		// end = end > count ? count : end;
		// list = list.subList(start, end);
		for (CiInformation info : list) {
			datas.add(info.asMapForRest());
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("count", count);
		retMap.put("page", page);
		retMap.put("pageSize", pageSize);
		retMap.put("datas", datas);

		ret.put("data", retMap);
		ret.put("message", "查询成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 获取我的CI
	 * 
	 * @param data
	 *            {'categoryId':'','like':{'*':'*
	 *            '},'page':1,pageSize:10,'all':true}
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private JsonRepresentation getMyCi(JSONObject data) throws Exception {
		// TODO 将分页写到数据库中....
		JSONObject ret = new JSONObject();
		String cateId = data.getString("categoryId");
		Boolean extend = data.getBoolean("all");
		JSONObject orExp = data.getJSONObject("like");
		int page = data.getInt("page");
		int pageSize = data.getInt("pageSize");

		List<CiInformation> list;
		int count;
		if (orExp == null) {
			throw new MException("查询条件不能为空");
		}
		String username = getUsername();
		if (cateId == null || cateId.equals("")) {
			Map<String, Object> m = infoService.qureyByAdvanced(null, null,
					orExp, extend == null ? true : extend, username, (page - 1)
							* pageSize, pageSize);
			list = (List<CiInformation>) m.get("data");
			count = (Integer) m.get("count");
		} else {
			CiCategory nc = cateService.getById(cateId);
			if (nc == null) {
				throw new MException("分类不存在");
			}
			Map<String, Object> m = infoService.qureyByAdvanced(nc, null,
					orExp, extend == null ? true : extend, username, (page - 1)
							* pageSize, pageSize);
			list = (List<CiInformation>) m.get("data");
			count = (Integer) m.get("count");
		}
		// 排序
		Collections.sort(list, new Comparator<CiInformation>() {
			@Override
			public int compare(CiInformation arg0, CiInformation arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		// 构建分页数据输出
		List<Object> datas = new ArrayList<Object>();
		// int count = list.size();
		// int start = (page - 1) * pageSize;
		// start = start < 0 ? 0 : start;
		// int end = page * pageSize;
		// start = start > count ? count : start;
		// end = end > count ? count : end;
		// list = list.subList(start, end);
		for (CiInformation info : list) {
			datas.add(info.asMapForRest());
		}
		Map<String, Object> retMap = new HashMap<String, Object>();
		retMap.put("count", count);
		retMap.put("page", page);
		retMap.put("pageSize", pageSize);
		retMap.put("datas", datas);

		ret.put("data", retMap);
		ret.put("message", "查询成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入XML
	 * 
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	private String importData(Representation entity) throws Exception {
		JSONObject ret = new JSONObject();

		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			e.printStackTrace();
		}

		String filename = "";
		FileItem fi = items.get(0);
		filename = fi.getName();
		if (filename == null || filename.equals("")
				|| filename.toLowerCase().trim().indexOf("xls") == -1) {
			throw new Exception("文件格式有误");
		}
		String username = getUsername();
		// key:用户名,value:true存在,false不存在
		Map<String, Boolean> users = new HashMap<String, Boolean>();
		users.put(username, true);
		// String info = "";
		log.dLog("正在解析EXCEL，请等待...");
		WebSocketMessage wsm = (WebSocketMessage)WebSocketMapping.getWebSocketActor("message");
		if(wsm!=null){
			wsm.broadcast("正在解析EXCEL，请等待...", 0.1d);
		}
//		MessageResult.broadcast("正在解析EXCEL，请等待...", 0.1d);

		ExcleJxlReadUtil ejr = new ExcleJxlReadUtil(fi.getInputStream());
		Table table = ejr.getTable();
		List<Sublist> sublist = table.getSublist();
		if(wsm!=null){
			wsm.broadcast("准备分类，请等待...", 0.4d);
		}
//		MessageResult.broadcast("准备分类，请等待...", 0.4d);
		List<CiCategory> all = cateService.getAll();
		Map<String, CiCategory> allMap = new HashMap<String, CiCategory>();
		for (CiCategory cate : all) {
			allMap.put(cate.getName(), cate);
		}
		int totle = 0;
		for (Sublist sl : sublist) {
			totle += sl.getData().size();
		}
		double flag = 0.45d / totle;
		double progress = 0.5;
		if(wsm!=null){
			wsm.broadcast("开始解析sheet，请等待...", progress);
		}
//		MessageResult.broadcast("开始解析sheet，请等待...", progress);
		for (Sublist sl : sublist) {
			String name = sl.getName();
			log.iLog("解析sheet[" + name + "]...");
			if(wsm!=null){
				wsm.broadcast("解析sheet[" + name + "]...", progress);
			}
//			MessageResult.broadcast("解析sheet[" + name + "]...", progress);

			if (name.indexOf("-") != -1) {
				String[] cs = name.split("-");
				name = cs[cs.length - 1];
			}
			if (name.endsWith(")")) {
				int s = name.lastIndexOf("(");
				int e = name.lastIndexOf(")");
				if (s > 0 && e > 0 && s < e && (e - s) <= 3) {
					name = name.substring(0, s);
				}
			}
			CiCategory nc = allMap.get(name);
			if (nc == null) {
				String in = "分类[" + sl.getName() + "]不存在,无法新建数据";
				log.dLog(in);
				if(wsm!=null){
					wsm.broadcast(in, progress);
				}
//				MessageResult.broadcast(in, progress);
			} else {
				Map<String, String> t = new HashMap<String, String>();
				List<CiInformation> cis = new ArrayList<CiInformation>();
				List<Map<String, Object>> data = sl.getData();
				if (data.size() == 0) {
					if(wsm!=null){
						wsm.broadcast("分类[" + sl.getName() + "]无数据",
								progress);
					}
//					MessageResult.broadcast("分类[" + sl.getName() + "]无数据",
//							progress);
				}
				for (Map<String, Object> obj : data) {
					try {
						CiInformation ci = new CiInformation(nc, "XLS", obj);
						String hexId = HexString
								.encode(HexString.json2Str(
										ci.getCategory().getName(),
										ci.getData().get(
												ci.getCategory().getMajor()
														.getName())));
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
						ci.setOwner(owner.toString());
						if (!t.containsKey(hexId)) {
							t.put(hexId, hexId);
							cis.add(ci);
						}
					} catch (Exception e) {
						String in = e.getMessage();
						if(wsm!=null){
							wsm.broadcast(in, progress);
						}
//						MessageResult.broadcast(in, progress);
					}
				}
				if (cis.size() > 0) {
					log.iLog("新建/更新数据开始...");
					if(wsm!=null){
						wsm.broadcast("新建/更新数据开始...", progress);
					}
//					MessageResult.broadcast("新建/更新数据开始...", progress);
					Map<String, Long> rm = infoService.saveOrUpdate(nc, cis);
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
//					MessageResult.broadcast(in, progress);
					progress += flag * cis.size();
				} else {
					log.iLog("分类[" + name + "]数据不符合要求");
					if(wsm!=null){
						wsm.broadcast("分类[" + name + "]数据不符合要求", progress);
					}
//					MessageResult
//							.broadcast("分类[" + name + "]数据不符合要求", progress);
				}
			}
		}
		if(wsm!=null){
			wsm.broadcast("上传数据完成", 1);
		}
//		MessageResult.broadcast("上传数据完成", 1);
		ret.put("message", "上传完成");
		return ret.toString();
	}

	/**
	 * 
	 * @param cateIds
	 *            指定要下载的分类 为null或size=0时下载全部的分类
	 * @param hasData
	 *            是否包含数据
	 * @return
	 */
	private Representation exportData(List<String> cateIds, List<String> ciIds,
			boolean hasData) throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("getAllForXls");
		File file = FileManager.getInstance().createFile("配置项数据", "xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Set<String> ciIdSet = new HashSet<String>();
		if (ciIds != null) {
			for (String s : ciIds) {
				ciIdSet.add(s);
			}
		}
		final int pageSize = 40000;
		List<CiCategory> ncs = cateService.getAll();
		int i = 0;
		WritableWorkbook wb = Workbook.createWorkbook(file);
		for (CiCategory nc : ncs) {
			String id = nc.getId();
			String name = nc.getName();
			List<Attribute> items = nc.getAllAttributes();
			if (cateIds == null || cateIds.size() == 0 || cateIds.contains(id)) {
				String pName = nc.getParent() != null ? nc.getParent()
						.getName() + "-" : "";
				WritableSheet ws = wb.createSheet(pName + name, i);
				List<String> title = this.createXlsSheet(ws, items);
				if (hasData) {
					int getPage = 1;
					Map<String, Object> m = infoService.qureyByAdvanced(nc,
							null, null, false, null, (getPage - 1)
									* Tool.getBuff, Tool.getBuff);
					List<CiInformation> datas = (List<CiInformation>) m
							.get("data");
					int count = (Integer) m.get("count");
					int index = 0;
					int sheetIndex = 0;
					List<CiInformation> addDatas = new ArrayList<CiInformation>();
					while (true) {
						for (CiInformation ciInfo : datas) {
							if (ciIds == null || ciIds.size() == 0) {
								addDatas.add(ciInfo);
							} else {
								String jsonId = ciInfo.getCiHex();
								if (ciIdSet.contains(jsonId)) {
									addDatas.add(ciInfo);
									ciIdSet.remove(jsonId);
								}
							}
							if (addDatas.size() == pageSize) {
								if (sheetIndex > 0) {
									ws = wb.createSheet(pName + name + "("
											+ sheetIndex + ")", ++i);
									title = this.createXlsSheet(ws, items);
								}
								this.addCell(ws, title, addDatas);
								sheetIndex++;
								addDatas = new ArrayList<CiInformation>();
							}
							index++;
						}
						if (ciIds != null && ciIds.size() > 0
								&& ciIdSet.size() == 0) {
							break;
						}
						if (index >= count) {
							break;
						}
						getPage++;
						m = infoService.qureyByAdvanced(nc, null, null, false,
								null, (getPage - 1) * Tool.getBuff,
								Tool.getBuff);
						datas = (List<CiInformation>) m.get("data");
						count = (Integer) m.get("count");
					}
					if (addDatas.size() > 0) {
						if (sheetIndex > 0) {
							ws = wb.createSheet(pName + name + "(" + sheetIndex
									+ ")", ++i);
							title = this.createXlsSheet(ws, items);
						}
						this.addCell(ws, title, addDatas);
					}
				}
				i++;
			}
		}
		if (wb.getNumberOfSheets() == 0) {
			wb.createSheet("Sheet1", i);
		}
		wb.write();
		wb.close();
		ret.put("message", "下载配置项数据成功");
		JSONObject retData = new JSONObject();
		retData.put("url", file.getName());
		ret.put("data", retData);
		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unchecked")
	private JsonRepresentation saveCi(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String cateId = data.getString("categoryId");
		data = data.getJSONObject("data");
		CiCategory nc = cateService.getById(cateId);
		if (nc == null) {
			throw new MException("分类[" + cateId + "]不存在");
		}

		CiInformation info = new CiInformation(nc, "PAGE", data);
		info.setOwner(getUsername());
		ret.put("data", infoService.save(info).asMapForRest());
		ret.put("message", "添加成功");
		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unchecked")
	private JsonRepresentation editCi(JSONObject data) throws Exception {
		JSONObject ret = new JSONObject();
		String jsonId = data.getString("jsonId");
		data = data.getJSONObject("data");
		if (jsonId == null || jsonId.equals("")) {
			throw new Exception("JSON参数不能为空");
		}

		if (data != null && data.size() > 0) {
			CiInformation info = infoService.getById(jsonId);
			if (info == null) {
				throw new MException("["
						+ JSONArray.fromObject(jsonId).getString(2) + "]不存在");
			}
			info = infoService.update(info, data, "PAGE");
			ret.put("data", info.asMap());

			ret.put("message", "更新成功");
		} else {
			throw new Exception("参数有空值");
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation deleteByCateId(List<String> cateIds)
			throws Exception {
		JSONObject ret = new JSONObject();
		Map<String, CiCategory> ca = new HashMap<String, CiCategory>();
		List<CiCategory> all = cateService.getAll();
		for (CiCategory ciCategory : all) {
			ca.put(ciCategory.getId(), ciCategory);
		}
		for (String string : cateIds) {
			CiCategory ciCategory = ca.get(string);
			if (ciCategory == null)
				continue;
			infoService.deleteCiByCategory(ciCategory);
		}
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation deleteByjsonId(List<String> jsonIds)
			throws Exception {
		JSONObject ret = new JSONObject();
		infoService.deleteByJsonIds(jsonIds);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
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
		int temp = 1;
		title.add(0, "owner");
		Label ownerLabel = new Label(0, 0, "owner", wff_color);
		WritableCellFeatures twf = new WritableCellFeatures();
		String tinfo = "是否必填：false \n 缺省数值：上传数据的用户 \n";
		twf.setComment(tinfo);
		ownerLabel.setCellFeatures(twf);
		ws.addCell(ownerLabel);

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
			info += "数据来源：" + map.getSources() + "\n";
			wf.setComment(info);
			label.setCellFeatures(wf);
			// label 可以设置宽度
			ws.addCell(label);
		}

		return title;
	}

	/**
	 * 把CI数据写入EXCEL
	 * 
	 * @param ws
	 *            WritableSheet
	 * @param title
	 *            EXCEL标题
	 * @param infos
	 *            CI数组
	 * @throws WriteException
	 */
	private void addCell(WritableSheet ws, List<String> title,
			List<CiInformation> infos) throws WriteException {
		int v = 1;
		for (CiInformation info : infos) {
			Map<String, Object> data = info.getData();
			data.put("owner", info.getOwner() == null ? "" : info.getOwner());
			data.put(info.getCategory().getMajor().getName(), info.getName());
			for (int k = 0; k < title.size(); k++) {
				String value = data.containsKey(title.get(k)) ? String
						.valueOf(data.get(title.get(k))) : "";
				Label label = new Label(k, v, value);
				ws.addCell(label);
			}
			v++;
		}
	}
}