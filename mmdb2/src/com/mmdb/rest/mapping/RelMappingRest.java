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
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.exception.MException;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.model.bean.User;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.ICiCateService;
import com.mmdb.service.category.IRelCateService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;

public class RelMappingRest extends BaseRest {
	private ISourceRelationMapService sourceRelMapService;
	private IDataSourceService dataSourceService;
	private ICiCateService ciCateService;
	private IRelCateService relCateService;
	private ITaskService taskService;

	@Override
	public void ioc(ApplicationContext context) {
		sourceRelMapService = context.getBean(ISourceRelationMapService.class);
		dataSourceService = context.getBean(IDataSourceService.class);
		ciCateService = context.getBean(ICiCateService.class);
		relCateService = context.getBean(IRelCateService.class); 
		taskService = context.getBean(ITaskService.class); 
	}

	@Override
	public Representation getHandler() throws Exception{
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		} else if ("owner".equals(param1)) {
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
	public Representation postHandler(Representation entity) throws Exception{
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
	 * 获取所有外部关系映射
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.iLog("getAll");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<SourceToRelationMapping> crs = sourceRelMapService.getAll();
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			for (SourceToRelationMapping cr : crs) {
				cr.setDataSource(dpMap.get(cr.getDataSourceId()));
				cr.setRelCate(relCateMap.get(cr.getRelCateId()));
				cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
				cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
				list.add(cr.asMap());
			}
			ret.put("data", list);
			ret.put("message", "获取所有外部关系映射成功");
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByUser(String username) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.iLog("getAll");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<SourceToRelationMapping> crs = sourceRelMapService
					.getByAuthor(username);
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			for (SourceToRelationMapping cr : crs) {
				cr.setDataSource(dpMap.get(cr.getDataSourceId()));
				cr.setRelCate(relCateMap.get(cr.getRelCateId()));
				cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
				cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
				list.add(cr.asMap());
			}
			ret.put("data", list);
			ret.put("message", "获取所有外部关系映射成功");
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取外部关系映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception{
		JSONObject ret = new JSONObject();
		try {
			log.dLog("getById");
			SourceToRelationMapping cr = sourceRelMapService.getById(id);
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			if (cr != null) {
				cr.setDataSource(dpMap.get(cr.getDataSourceId()));
				cr.setRelCate(relCateMap.get(cr.getRelCateId()));
				cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
				cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
				ret.put("data", cr.asMap());
				ret.put("message", "获取外部关系映射成功");
			} else {
				throw new MException("外部关系映射不存在");
			}
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导出外部关系映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation exportData() throws Exception{
		JSONObject ret = new JSONObject();

		log.dLog("exportData");
		File file = FileManager.getInstance().createFile(
				"外部关系映射-" + TimeUtil.getTime(TimeUtil.YMD), "json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<SourceToRelationMapping> cs = sourceRelMapService.getAll();
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			for (SourceToRelationMapping cr : cs) {
				cr.setDataSource(dpMap.get(cr.getDataSourceId()));
				cr.setRelCate(relCateMap.get(cr.getRelCateId()));
				cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
				cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
				list.add(cr.asMap());
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
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入外部关系映射
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation importData(Representation entity)  throws Exception{
		log.dLog("importData");
		JSONObject ret = new JSONObject();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			//log.eLog(e);
			throw e;
		}

		String filename = "";
		FileItem fi = items.get(0);
		try {
			filename = fi.getName();
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf(".json") == -1) {
				/*log.eLog("文件格式有误");
				ret.put("message", "文件格式有误");
				getResponse().setStatus(new Status(600));
				return new JsonRepresentation(ret.toString());*/
				throw new MException("文件格式有误");
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
				String relCateName = obj.getString("relCateName");
				String sourceCateName = obj.getString("sourceCateName");
				String targetCateName = obj.getString("targetCateName");
				DataSourcePool dataSource = dataSourceService
						.getByName(dataSourceName);
				RelCategory relCate = relCateService.getByName(relCateName);
				CiCategory sourceCate = ciCateService.getByName(sourceCateName);
				CiCategory targetCate = ciCateService.getByName(targetCateName);
				if (dataSource != null && relCate != null && sourceCate != null
						&& targetCate != null) {
					String name = obj.getString("name");
					SourceToRelationMapping cr = sourceRelMapService
							.getByName(name);
					if (cr == null) {
						cr = new SourceToRelationMapping();
						cr.setName(name);
						cr.setDataSourceId(dataSource.getId());
						cr.setRelCateId(relCate.getId());
						JSONObject relValue;
						try {
							relValue = obj.getJSONObject("relValue");
						} catch (Exception e) {
							relValue = new JSONObject();
						}
						cr.setRelValue(relValue);
						cr.setSourceCateId(sourceCate.getId());
						cr.setSourceField(obj.containsKey("sourceField") ? obj
								.getString("sourceField") : "");
						cr.setTargetCateId(targetCate.getId());
						cr.setTargetField(obj.containsKey("targetField") ? obj
								.getString("targetField") : "");
						Object owner = obj.get("owner");
						if (owner == null) {
							owner = user.getLoginName();
						}
						cr.setOwner((String) owner);
						cr = sourceRelMapService.save(cr);
						add++;
					} else {
						cr.setName(name);
						cr.setDataSourceId(dataSource.getId());
						cr.setRelCateId(relCate.getId());
						JSONObject relValue;
						try {
							relValue = obj.getJSONObject("relValue");
						} catch (Exception e) {
							relValue = new JSONObject();
						}
						cr.setRelValue(relValue);
						cr.setSourceCateId(sourceCate.getId());
						cr.setSourceField(obj.containsKey("sourceField") ? obj
								.getString("sourceField") : "");
						cr.setTargetCateId(targetCate.getId());
						cr.setTargetField(obj.containsKey("targetField") ? obj
								.getString("targetField") : "");
						cr = sourceRelMapService.update(cr);
						upd++;
					}
				}
			}

			ret.put("message", "新建外部关系映射" + add + "条,修改外部关系映射" + upd + "条");
			log.dLog("importData success");
		} catch (Exception e) {
			throw e;
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
		try {
			log.dLog("save SourceToRelationMapping");
			if (cdMap == null || cdMap.size() == 0) {
				throw new MException("SourceToRelationMapping参数不能空");
			}
			String name = (String) cdMap.get("name");
			if (name == null || name.equals("")) {
				throw new MException("名称不能空");
			}

			SourceToRelationMapping cr = new SourceToRelationMapping();
			cr.setName(name);
			cr.setDataSourceId(cdMap.containsKey("dataSourceId") ? cdMap
					.getString("dataSourceId") : "");
			cr.setRelCateId(cdMap.containsKey("relCateId") ? cdMap
					.getString("relCateId") : "");
			JSONObject relValue;
			try {
				relValue = cdMap.getJSONObject("relValue");
			} catch (Exception e) {
				relValue = new JSONObject();
			}
			cr.setRelValue(relValue);
			cr.setSourceCateId(cdMap.containsKey("sourceCateId") ? cdMap
					.getString("sourceCateId") : "");
			cr.setSourceField(cdMap.containsKey("sourceField") ? cdMap
					.getString("sourceField") : "");
			cr.setTargetCateId(cdMap.containsKey("targetCateId") ? cdMap
					.getString("targetCateId") : "");
			cr.setTargetField(cdMap.containsKey("targetField") ? cdMap
					.getString("targetField") : "");
			
			String userName = getUsername();
			cr.setOwner(userName);

			cr = sourceRelMapService.save(cr);
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			cr.setDataSource(dpMap.get(cr.getDataSourceId()));
			cr.setRelCate(relCateMap.get(cr.getRelCateId()));
			cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
			cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
			Map<String, Object> asMap = cr.asMap();
			ret.put("data", asMap);
			ret.put("message", "保存成功");
		} catch (Exception e) {
			throw e;
		}
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
		try {
			log.dLog("update SourceToRelationMapping");
			if (cdMap == null || cdMap.size() == 0) {
				throw new MException("SourceToRelationMapping参数不能空");
			}
			String id = cdMap.getString("id");
			SourceToRelationMapping cr = sourceRelMapService.getById(id);
			if (cr == null) {
				throw new MException("SourceToRelationMapping[" + id + "]不存在");
			}
			cr.setName(cdMap.containsKey("name") ? cdMap.getString("name") : "");
			cr.setDataSourceId(cdMap.containsKey("dataSourceId") ? cdMap
					.getString("dataSourceId") : "");
			cr.setRelCateId(cdMap.containsKey("relCateId") ? cdMap
					.getString("relCateId") : "");
			JSONObject relValue;
			try {
				relValue = cdMap.getJSONObject("relValue");
			} catch (Exception e) {
				relValue = new JSONObject();
			}
			cr.setRelValue(relValue);
			cr.setSourceCateId(cdMap.containsKey("sourceCateId") ? cdMap
					.getString("sourceCateId") : "");
			cr.setSourceField(cdMap.containsKey("sourceField") ? cdMap
					.getString("sourceField") : "");
			cr.setTargetCateId(cdMap.containsKey("targetCateId") ? cdMap
					.getString("targetCateId") : "");
			cr.setTargetField(cdMap.containsKey("targetField") ? cdMap
					.getString("targetField") : "");
			cr = sourceRelMapService.update(cr);
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			cr.setDataSource(dpMap.get(cr.getDataSourceId()));
			cr.setRelCate(relCateMap.get(cr.getRelCateId()));
			cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
			cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
			Map<String, Object> asMap = cr.asMap();
			ret.put("data", asMap);
			ret.put("message", "修改成功");
		} catch (Exception e) {
			throw e;
		}
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
		try {
			sourceRelMapService.deleteAll();
			ret.put("message", "删除全部成功");
		} catch (Exception e) {
			throw e;
		}

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
		try {
			log.dLog("delete sourceRelMapService");
			if (id == null || id.equals("")) {
				throw new MException("参数不能空");
			}
			SourceToRelationMapping cr = sourceRelMapService.getById(id);
			if (cr == null) {
				throw new MException("SourceToRelationMapping[" + id + "]不存在");
			}
			List<Task> tasks = taskService.getAll();
			for (Task task : tasks) {
				List<String> crs = task.getOutCiCateMapIds();
				if (crs != null) {
					for (String cId : crs) {
						if (id.equals(cId)) {
							throw new MException("SourceToRelationMapping[" + id
									+ "]正在被任务使用");
						}
					}
				}
			}
			sourceRelMapService.delete(cr);
			ret.put("message", "删除成功");
		} catch (Exception e) {
			throw e;
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
		try {
			log.dLog("立即执行外部关系映射");
			String id = "";
			if (cdMap.containsKey("id")) {
				id = cdMap.getString("id");
			}
			if (id == null || id.equals("")) {
				throw new Exception("映射ID不能为空");
			}
			SourceToRelationMapping cr = sourceRelMapService.getById(id);
			if (cr == null) {
				throw new Exception("映射不存在");
			}
			Map<String, CiCategory> ciCateMap = getCiCateMap();
			Map<String, RelCategory> relCateMap = getRelCateMap();
			Map<String, DataSourcePool> dpMap = getDataSourcePoolMap();
			cr.setDataSource(dpMap.get(cr.getDataSourceId()));
			cr.setRelCate(relCateMap.get(cr.getRelCateId()));
			cr.setSourceCate(ciCateMap.get(cr.getSourceCateId()));
			cr.setTargetCate(ciCateMap.get(cr.getTargetCateId()));
			Map<String, Integer> rm = sourceRelMapService.runNow(cr.getId(),
					ciCateMap, relCateMap);
			ret.put("message",
					"新建数据(" + rm.get("save") + ")条,更新数据(" + rm.get("update")
							+ ")条,删除数据(" + rm.get("delete") + ")条");
		} catch (Exception me) {
			//log.eLog(me);
			throw me;
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

	private Map<String, RelCategory> getRelCateMap() throws Exception {
		List<RelCategory> dps = relCateService.getAll();
		Map<String, RelCategory> dpMap = new HashMap<String, RelCategory>();
		for (RelCategory dp : dps) {
			dpMap.put(dp.getId(), dp);
		}
		return dpMap;
	}

}
