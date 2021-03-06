package com.mmdb.rest.mapping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
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
import com.mmdb.core.utils.SimilarityUtil;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.mapping.ISourceCategoryMapService;
import com.mmdb.service.role.IUserService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;
import com.mmdb.util.des.Des;

public class CiMappingRest extends BaseRest {
	private ISourceCategoryMapService sourceCateMapService;
	private IDataSourceService dataSourceService;
	private ICiCateService ciCateService;
	private ITaskService taskService;

	@Override
	public void ioc(ApplicationContext context) {
		sourceCateMapService = context.getBean(ISourceCategoryMapService.class);
		dataSourceService = context.getBean(IDataSourceService.class);
		ciCateService = context.getBean(ICiCateService.class);
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
		} else if ("compare".equals(param1)) {
			return compare(params);
		} else {
			return save(params);
		}
	}

	@Override
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
	 * 获取所有外部分类映射
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<SourceToCategoryMapping> cds = sourceCateMapService.getAll();
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		for (SourceToCategoryMapping cd : cds) {
			cd.setCate(ciCateMap.get(cd.getCateId()));
			cd.setDataSource(dpMap.get(cd.getDataSourceId()));
			list.add(cd.asMap());
		}
		ret.put("data", list);
		ret.put("message", "获取所有内部映射成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByUser(String username) throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getByUser");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<SourceToCategoryMapping> cds = sourceCateMapService
				.getByAuthor(username);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		for (SourceToCategoryMapping cd : cds) {
			cd.setCate(ciCateMap.get(cd.getCateId()));
			cd.setDataSource(dpMap.get(cd.getDataSourceId()));
			list.add(cd.asMap());
		}
		ret.put("data", list);
		ret.put("message", "获取所有内部映射成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取外部分类映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getById");
		SourceToCategoryMapping cd = sourceCateMapService.getById(id);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		if (cd != null) {
			cd.setCate(ciCateMap.get(cd.getCateId()));
			cd.setDataSource(dpMap.get(cd.getDataSourceId()));
			ret.put("data", cd.asMap());
			ret.put("message", "获取外部映射成功");
		} else {
			throw new MException("外部映射不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导出外部分类映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation exportData() {
		JSONObject ret = new JSONObject();

		log.dLog("exportData");
		File file = FileManager.getInstance().createFile(
				"外部分类映射-" + TimeUtil.getTime(TimeUtil.YMD), "json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<SourceToCategoryMapping> cds = sourceCateMapService.getAll();
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			for (SourceToCategoryMapping cd : cds) {
				cd.setCate(ciCateMap.get(cd.getCateId()));
				cd.setDataSource(dpMap.get(cd.getDataSourceId()));
				list.add(cd.asMap());
			}
			String json = JSONArray.fromObject(list).toString();

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(json.toString().getBytes("utf-8"));
				fos.flush();
			} catch (Exception e) {
			} finally {
				fos.close();
			}
			ret.put("message", "下载外部分类映射成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
			log.dLog("export success");

		} catch (Exception e) {
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入外部分类映射
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
					|| filename.toLowerCase().trim().indexOf(".json") == -1) {
				log.eLog("文件格式有误");
				ret.put("message", "文件格式有误");
				getResponse().setStatus(new Status(600));
				return new JsonRepresentation(ret.toString());
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					fi.getInputStream()));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			User user = getUser();
			JSONArray list = JSONArray.fromObject(buffer.toString());
			int add = 0;
			int upd = 0;
			for (int i = 0; i < list.size(); i++) {
				JSONObject obj = list.getJSONObject(i);
				String dataSourceName = obj.getString("dataSourceName");
				String cateName = obj.getString("cateName");
				DataSourcePool dataSource = dataSourceService
						.getByName(dataSourceName);
				CiCategory cate = ciCateService.getByName(cateName);
				if (dataSource != null && cate != null) {
					String name = obj.getString("name");
					SourceToCategoryMapping cd = sourceCateMapService
							.getByName(name);
					if (cd == null) {
						cd = new SourceToCategoryMapping();
						cd.setName(name);
						cd.setCateId(cate.getId());
						cd.setDataSourceId(dataSource.getId());
						JSONObject fieldMap;
						try {
							fieldMap = obj.getJSONObject("fieldMap");
						} catch (Exception e) {
							fieldMap = new JSONObject();
						}
						cd.setFieldMap(fieldMap);
						Object owner = obj.get("owner");
						if (owner == null) {
							owner = user.getLoginName();
						}
						cd.setOwner((String) owner);

						cd = sourceCateMapService.save(cd);
						add++;
					} else {
						cd.setName(name);
						cd.setCateId(cate.getId());
						cd.setDataSourceId(dataSource.getId());
						JSONObject fieldMap;
						try {
							fieldMap = obj.getJSONObject("fieldMap");
						} catch (Exception e) {
							fieldMap = new JSONObject();
						}
						cd.setFieldMap(fieldMap);
						cd = sourceCateMapService.update(cd);
						upd++;
					}
				}
			}

			ret.put("message", "新建外部分类映射" + add + "条,修改外部分类映射" + upd + "条");
			log.dLog("importData success");
		} catch (Exception e) {
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 保存
	 * 
	 * @param cdMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation save(JSONObject cdMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("save SourceToCategoryMapping");
		if (cdMap == null || cdMap.size() == 0) {
			throw new Exception("SourceToCategoryMapping参数不能空");
		}
		String name = (String) cdMap.get("name");
		if (name == null || name.equals("")) {
			throw new MException("名称不能空");
		}

		SourceToCategoryMapping cd = new SourceToCategoryMapping();
		cd.setName(name);
		cd.setCateId(cdMap.containsKey("cateId") ? cdMap.getString("cateId")
				: "");
		cd.setDataSourceId(cdMap.containsKey("dataSourceId") ? cdMap
				.getString("dataSourceId") : "");
		JSONObject fieldMap;
		try {
			fieldMap = cdMap.getJSONObject("fieldMap");
		} catch (Exception e) {
			fieldMap = new JSONObject();
		}
		cd.setFieldMap(fieldMap);
		cd.setOwner(getUsername());
		cd = sourceCateMapService.save(cd);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		cd.setCate(ciCateMap.get(cd.getCateId()));
		cd.setDataSource(dpMap.get(cd.getDataSourceId()));
		Map<String, Object> asMap = cd.asMap();
		ret.put("data", asMap);
		ret.put("message", "保存成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 修改
	 * 
	 * @param cdMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation update(JSONObject cdMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("update SourceToCategoryMapping");
		if (cdMap == null || cdMap.size() == 0) {
			throw new Exception("SourceToCategoryMapping参数不能空");
		}
		String id = cdMap.getString("id");
		SourceToCategoryMapping cd = sourceCateMapService.getById(id);
		if (cd == null) {
			throw new MException("SourceToCategoryMapping[" + id + "]不存在");
		}
		cd.setName(cdMap.containsKey("name") ? cdMap.getString("name") : "");
		cd.setCateId(cdMap.containsKey("cateId") ? cdMap.getString("cateId")
				: "");
		cd.setDataSourceId(cdMap.containsKey("dataSourceId") ? cdMap
				.getString("dataSourceId") : "");
		JSONObject fieldMap;
		try {
			fieldMap = cdMap.getJSONObject("fieldMap");
		} catch (Exception e) {
			fieldMap = new JSONObject();
		}
		cd.setFieldMap(fieldMap);
		cd = sourceCateMapService.update(cd);
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		cd.setCate(ciCateMap.get(cd.getCateId()));
		cd.setDataSource(dpMap.get(cd.getDataSourceId()));
		Map<String, Object> asMap = cd.asMap();
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
		sourceCateMapService.deleteAll();
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
	private JsonRepresentation deleteById(String id) {
		JSONObject ret = new JSONObject();
		try {
			log.dLog("delete DataSourcePool");
			if (id == null || id.equals("")) {
				throw new Exception("参数不能空");
			}
			SourceToCategoryMapping cd = sourceCateMapService.getById(id);
			if (cd == null) {
				throw new Exception("SourceToCategoryMapping[" + id + "]不存在");
			}
			List<Task> tasks = taskService.getAll();
			for (Task task : tasks) {
				List<String> cds = task.getDbCiCateMapIds();
				if (cds != null) {
					for (String cId : cds) {
						if (id.equals(cId)) {
							throw new Exception("SourceToCategoryMapping[" + id
									+ "]正在被任务使用");
						}
					}
				}
			}
			sourceCateMapService.delete(cd);
			ret.put("message", "删除成功");
		} catch (Exception e) {
			log.eLog(e);
			ret.put("message", e.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 只执行一次
	 * 
	 * @param ccMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation run(JSONObject cdMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("立即执行内部映射");
		String id = "";
		if (cdMap.containsKey("id")) {
			id = cdMap.getString("id");
		}
		if (id == null || id.equals("")) {
			throw new Exception("映射ID不能为空");
		}
		SourceToCategoryMapping cd = sourceCateMapService.getById(id);
		if (cd == null) {
			throw new MException("映射不存在");
		}
		Map<String, CiCategory> ciCateMap = getCiCateMap();
		Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
		cd.setCate(ciCateMap.get(cd.getCateId()));
		cd.setDataSource(dpMap.get(cd.getDataSourceId()));
		Map<String, Integer> rm = sourceCateMapService.runNow(cd.getId(),
				ciCateMap);
		ret.put("message",
				"新建数据(" + rm.get("save") + ")条,更新数据(" + rm.get("update")
						+ ")条,删除数据(" + rm.get("delete") + ")条");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 对比
	 * 
	 * @param ccMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation compare(JSONObject cdMap) {
		JSONObject ret = new JSONObject();
		try {
			log.dLog("compare");
			List<String> strs1 = cdMap.getJSONArray("strs1");
			List<String> strs2 = cdMap.getJSONArray("strs2");
			if ((strs1 == null || strs1.size() == 0)
					|| (strs2 == null || strs2.size() == 0)) {
				throw new Exception("参数不能为空");
			}
			List<List<Object>> list = new ArrayList<List<Object>>();
			for (String str1 : strs1) {
				List<Object> wl = new ArrayList<Object>();
				double max = 0d;
				String mStr = "";
				for (String str2 : strs2) {
					double d = SimilarityUtil.sim(str1, str2);
					if (max < d) {
						max = d;
						mStr = str2;
					}
				}
				if (max > 0d) {
					wl.add(max);
					wl.add(mStr);
					wl.add(str1);
					list.add(wl);
				}
			}
			ret.put("data", list);
			ret.put("message", "获取相似度");
		} catch (Exception me) {
			log.eLog(me);
			ret.put("message", me.getMessage());
			getResponse().setStatus(new Status(600));
		}
		return new JsonRepresentation(ret.toString());
	}

	private Map<String, DataSourcePool> getDataSourcePoolMap() throws Exception {
		List<DataSourcePool> dps = dataSourceService.getAll();
		Map<String, DataSourcePool> dpMap = new HashMap<String, DataSourcePool>();
		for (DataSourcePool dp : dps) {
			dpMap.put(dp.getId(), dp);
		}
		return dpMap;
	}

	private Map<String, CiCategory> getCiCateMap() throws Exception {
		List<CiCategory> cates = ciCateService.getAll();
		Map<String, CiCategory> cateMap = new HashMap<String, CiCategory>();
		for (CiCategory cate : cates) {
			cateMap.put(cate.getId(), cate);
		}
		return cateMap;
	}
}
