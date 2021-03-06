package com.mmdb.rest.mapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import jxl.write.biff.RowsExceededException;
import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.ExcleJxlReadUtil;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.core.utils.ExcleJxlReadUtil.Sublist;
import com.mmdb.core.utils.ExcleJxlReadUtil.Table;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.mapping.IInCiCateMapService;
import com.mmdb.service.role.IUserService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;
import com.mmdb.util.des.Des;

public class CiCiMappingRest extends BaseRest {
	private IInCiCateMapService inCiCateMapService;
	private ICiCateService ciCateService;
	private IRelCateService relCateService;
	private ITaskService taskService;

	@Override
	public void ioc(ApplicationContext context) {
		inCiCateMapService = context.getBean(IInCiCateMapService.class);
		ciCateService = context.getBean(ICiCateService.class);
		relCateService = context.getBean(IRelCateService.class);
		taskService = context.getBean(ITaskService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		} else if ("author".equals(param1)) {
			String param2 = (String) getRequestAttributes().get("param2");
			if (param2 != null) {
				try {
					param2 = URLDecoder.decode(param2, "utf-8");
				} catch (UnsupportedEncodingException e) {
				}
			}
			return getByUser(param2);
		} else {
			return getById(param1);
		}
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");

		if ("import".equals(param1)) {
			return importData(entity);
		}

		JSONObject params = parseEntity(entity);
		if ("run".equals(param1)) {
			return run(params);
		} else {
			return save(params);
		}
	}

	public Representation putHandler(Representation entity) throws Exception {
		JSONObject params = parseEntity(entity);
		return update(params);
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return deleteAll();
		} else {
			return deleteById(param1);
		}
	}

	/**
	 * 获取所有内部分类映射
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<InCiCateMap> ccs = inCiCateMapService.getAll();
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		for (InCiCateMap cc : ccs) {
			cc.setRelCate(relCateMap.get(cc.getRelCateId()));
			cc.setStartCate(ciCateMap.get(cc.getStartCateId()));
			cc.setEndCate(ciCateMap.get(cc.getEndCateId()));
			list.add(cc.asMap());
		}
		ret.put("data", list);
		ret.put("message", "获取所有内部映射成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByUser(String username) throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getByUser");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<InCiCateMap> ccs = inCiCateMapService.getByAuthor(username);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		for (InCiCateMap cc : ccs) {
			cc.setRelCate(relCateMap.get(cc.getRelCateId()));
			cc.setStartCate(ciCateMap.get(cc.getStartCateId()));
			cc.setEndCate(ciCateMap.get(cc.getEndCateId()));
			list.add(cc.asMap());
		}
		ret.put("data", list);
		ret.put("message", "获取所有内部映射成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取内部分类映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getById");
		InCiCateMap cc = inCiCateMapService.getById(id);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		if (cc != null) {
			cc.setRelCate(relCateMap.get(cc.getRelCateId()));
			cc.setStartCate(ciCateMap.get(cc.getStartCateId()));
			cc.setEndCate(ciCateMap.get(cc.getEndCateId()));
			ret.put("data", cc.asMap());
			ret.put("message", "获取内部映射成功");
		} else {
			throw new MException("内部映射不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导出内部分类映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation exportData() {
		JSONObject ret = new JSONObject();
		log.dLog("exportData");
		File file = FileManager.getInstance().createFile(
				"内部分类映射-" + TimeUtil.getTime(TimeUtil.YMD), "xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream out = null;
		try {
			List<InCiCateMap> relMaps = inCiCateMapService.getAll();
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			for (InCiCateMap relMap : relMaps) {
				relMap.setRelCate(relCateMap.get(relMap.getRelCateId()));
				relMap.setStartCate(ciCateMap.get(relMap.getStartCateId()));
				relMap.setEndCate(ciCateMap.get(relMap.getEndCateId()));
			}
			out = new FileOutputStream(file);
			WritableWorkbook wb = Workbook.createWorkbook(out);
			WritableSheet ws = wb.createSheet("内部分类映射", 0);
			List<String> tabList = addXlsTable(ws);
			Map<String, Object> rMap = new HashMap<String, Object>();
			for (int i = 0; i < relMaps.size(); i++) {
				rMap = relMaps.get(i).asMap();
				for (int j = 0; j < tabList.size(); j++) {
					Label label = new Label(j, i + 1, rMap.get(tabList.get(j))
							.toString());
					ws.addCell(label);
				}
			}
			wb.write();
			wb.close();

			ret.put("message", "下载内部分类映射成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
			log.dLog("exportXLS success");

		} catch (Exception e) {
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			out = null;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入内部分类映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation importData(Representation entity) {
		log.dLog("importData");
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
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf(".xls") == -1) {
				log.eLog("文件格式有误");
				ret.put("message", "文件格式有误");
				getResponse().setStatus(new Status(600));
				return new JsonRepresentation(ret.toString());
			}

			ExcleJxlReadUtil ejr = new ExcleJxlReadUtil(fi.getInputStream());
			Table table = ejr.getTable();
			List<Sublist> sublist = table.getSublist();
			for (Sublist sl : sublist) {
				String id = sl.getName().trim();
				if (id.equals("内部分类映射")) {
					log.iLog("解析sheet[" + id + "]...");
					List<Map<String, Object>> data = sl.getData();
					List<InCiCateMap> datas = new ArrayList<InCiCateMap>();
					String mName, sCateId, sField, eCateId, eField;
					String rId;
					// InCiCateMap icm = null;
					User user = getUser();
					for (Map<String, Object> obj : data) {
						mName = obj.get("映射名称").toString();
						// icm = icmService.getByName(mName);
						rId = obj.get("关系分类ID").toString();
						sCateId = obj.get("起点分类ID").toString();
						sField = obj.get("起点分类字段").toString();
						eCateId = obj.get("终点分类ID").toString();
						eField = obj.get("终点分类字段").toString();
						CiCategory sCate = ciCateService.getById(sCateId);
						CiCategory eCate = ciCateService.getById(eCateId);
						RelCategory rc = relCateService.getById(rId);

						Object owner = obj.get("owner");
						if (owner == null) {
							owner = user.getLoginName();
						}

						// TODO 关系属性值没有处理
						InCiCateMap mp = new InCiCateMap(mName, rc, null,
								sCate, eCate, sField, eField);
						// if (icm != null) {
						// mp.setNeo4jid(icm.getNeo4jid());
						// }
						mp.setOwner((String) owner);
						datas.add(mp);
					}
					if (datas.size() > 0) {
						log.iLog("保存/更新数据开始...");
						for (InCiCateMap im : datas) {
							if (inCiCateMapService.exist(im.getId())) {
								inCiCateMapService.update(im);
							} else {
								inCiCateMapService.save(im);
							}
						}
						String in = "[" + id + "]保存(" + datas.size() + ")条新数据";
						log.iLog(in);
					}
				}
			}

			ret.put("message", "导入内部分类映射成功");
			log.dLog("importData success");
		} catch (Exception e) {
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 保存
	 * 
	 * @param dbMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation save(JSONObject ccMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("save InCiCateMap");
		if (ccMap == null || ccMap.size() == 0) {
			throw new Exception("InCiCateMap参数不能空");
		}
		String name = (String) ccMap.get("name");
		if (name == null || name.equals("")) {
			throw new MException("名称不能空");
		}

		InCiCateMap cc = new InCiCateMap();
		cc.setName(name);
		cc.setRelCateId(ccMap.containsKey("relCateId") ? ccMap
				.getString("relCateId") : "");
		JSONObject relValue = new JSONObject();
		if (ccMap.containsKey("relValue")) {
			try {
				relValue = ccMap.getJSONObject("relValue");
			} catch (Exception e) {
				relValue = new JSONObject();
			}
		}
		cc.setRelValue(relValue);
		cc.setStartCateId(ccMap.containsKey("startCateId") ? ccMap
				.getString("startCateId") : "");
		cc.setStartCateField(ccMap.containsKey("startCateField") ? ccMap
				.getString("startCateField") : "");
		cc.setEndCateId(ccMap.containsKey("endCateId") ? ccMap
				.getString("endCateId") : "");
		cc.setEndCateField(ccMap.containsKey("endCateField") ? ccMap
				.getString("endCateField") : "");
		User user = getUser();
		cc.setOwner(user.getLoginName());

		cc = inCiCateMapService.save(cc);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		cc.setRelCate(relCateMap.get(cc.getRelCateId()));
		cc.setStartCate(ciCateMap.get(cc.getStartCateId()));
		cc.setEndCate(ciCateMap.get(cc.getEndCateId()));
		Map<String, Object> asMap = cc.asMap();
		ret.put("data", asMap);
		ret.put("message", "保存成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 修改
	 * 
	 * @param ccMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation update(JSONObject ccMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("update InCiCateMap");
		if (ccMap == null || ccMap.size() == 0) {
			throw new Exception("InCiCateMap参数不能空");
		}
		String id = ccMap.getString("id");
		InCiCateMap cc = inCiCateMapService.getById(id);
		if (cc == null) {
			throw new MException("InCiCateMap不存在");
		}
		cc.setName(ccMap.containsKey("name") ? ccMap.getString("name") : "");
		cc.setRelCateId(ccMap.containsKey("relCateId") ? ccMap
				.getString("relCateId") : "");
		JSONObject relValue = new JSONObject();
		if (ccMap.containsKey("relValue")) {
			try {
				relValue = ccMap.getJSONObject("relValue");
			} catch (Exception e) {
				relValue = new JSONObject();
			}
		}
		cc.setRelValue(relValue);
		cc.setStartCateId(ccMap.containsKey("startCateId") ? ccMap
				.getString("startCateId") : "");
		cc.setStartCateField(ccMap.containsKey("startCateField") ? ccMap
				.getString("startCateField") : "");
		cc.setEndCateId(ccMap.containsKey("endCateId") ? ccMap
				.getString("endCateId") : "");
		cc.setEndCateField(ccMap.containsKey("endCateField") ? ccMap
				.getString("endCateField") : "");
		cc = inCiCateMapService.update(cc);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		cc.setRelCate(relCateMap.get(cc.getRelCateId()));
		cc.setStartCate(ciCateMap.get(cc.getStartCateId()));
		cc.setEndCate(ciCateMap.get(cc.getEndCateId()));
		Map<String, Object> asMap = cc.asMap();
		ret.put("data", asMap);
		ret.put("message", "修改成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除全部
	 * 
	 * @param dbMap
	 * @return
	 * @throws Exception
	 */
	private Representation deleteAll() throws Exception {
		JSONObject ret = new JSONObject();
		inCiCateMapService.deleteAll();
		ret.put("message", "删除全部成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 删除
	 * 
	 * @param dbMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation deleteById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("delete DataSourcePool");
		if (id == null || id.equals("")) {
			throw new Exception("参数不能空");
		}
		InCiCateMap cc = inCiCateMapService.getById(id);
		if (cc == null) {
			throw new MException("InCiCateMap不存在");
		}
		List<Task> tasks = taskService.getAll();
		for (Task task : tasks) {
			List<String> ccs = task.getInCiCateMapIds();
			if (ccs != null) {
				for (String cId : ccs) {
					if (id.equals(cId)) {
						throw new MException("InCiCateMap正在被任务使用");
					}
				}
			}
		}
		inCiCateMapService.delete(cc);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 只执行一次
	 * 
	 * @param ccMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation run(JSONObject ccMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("立即执行内部映射");
		String id = "";
		if (ccMap.containsKey("id")) {
			id = ccMap.getString("id");
		}
		if (id == null || id.equals("")) {
			throw new MException("映射ID不能为空");
		}
		InCiCateMap irm = inCiCateMapService.getById(id);
		if (irm == null) {
			throw new MException("映射不存在");
		}
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, RelCategory> relCateMap = getRelCateMap();
		irm.setRelCate(relCateMap.get(irm.getRelCateId()));
		irm.setStartCate(ciCateMap.get(irm.getStartCateId()));
		irm.setEndCate(ciCateMap.get(irm.getEndCateId()));
		Map<String, Long> retMap = inCiCateMapService.runNow(irm.getId(),
				ciCateMap, relCateMap);
		log.dLog("新建关系(" + retMap.get("save") + ")条,更新关系("
				+ retMap.get("update") + ")");
		
		ret.put("message",
				"新建关系(" + retMap.get("save") + ")条,更新关系("
						+ retMap.get("update") + ")");

		return new JsonRepresentation(ret.toString());
	}

	private Map<String, CiCategory> getCiCateMap() throws Exception {
		List<CiCategory> cates = ciCateService.getAll();
		Map<String, CiCategory> cateMap = new HashMap<String, CiCategory>();
		for (CiCategory cate : cates) {
			cateMap.put(cate.getId(), cate);
		}
		return cateMap;
	}

	private Map<String, RelCategory> getRelCateMap() throws Exception {
		List<RelCategory> cates = relCateService.getAll();
		Map<String, RelCategory> cateMap = new HashMap<String, RelCategory>();
		for (RelCategory cate : cates) {
			cateMap.put(cate.getId(), cate);
		}
		return cateMap;
	}

	private List<String> addXlsTable(WritableSheet ws)
			throws RowsExceededException, WriteException {
		String[] labels = { "映射名称", "起点分类ID", "起点分类名称", "起点分类字段", "终点分类ID",
				"终点分类名称", "终点分类字段", "关系ID", "关系名称" };
		WritableFont wf_color = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		WritableCellFormat wff_color = new WritableCellFormat(wf_color);
		wf_color.setColour(Colour.BLACK);
		WritableCellFormat wff_color2 = new WritableCellFormat(wf_color);
		WritableCellFeatures wf = new WritableCellFeatures();
		Label label = new Label(0, 0, "映射名称", wff_color);
		String info = "唯一标识：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(1, 0, "起点分类ID", wff_color);
		info = "分类的唯一标识：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(2, 0, "起点分类名称", wff_color2);
		info = "分类名称：选填";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(3, 0, "起点分类字段", wff_color);
		info = "分类字段：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(4, 0, "终点分类ID", wff_color);
		info = "分类的唯一标识：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(5, 0, "终点分类名称", wff_color2);
		info = "分类名称：：选填";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(6, 0, "终点分类字段", wff_color);
		info = "分类字段：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(7, 0, "关系分类ID", wff_color);
		info = "分类的唯一标识：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(8, 0, "关系分类名称", wff_color2);
		info = "分类名称：必填项";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		label = new Label(9, 0, "关系字段数据来源", wff_color2);
		info = "分类字段：选填";
		wf.setComment(info);
		label.setCellFeatures(wf);
		ws.addCell(label);
		// TODO
		ArrayList<String> ret = new ArrayList<String>();
		for (String str : labels) {
			ret.add(str);
		}
		return ret;
	}

}
