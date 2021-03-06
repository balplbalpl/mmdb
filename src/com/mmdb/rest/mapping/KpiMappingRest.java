package com.mmdb.rest.mapping;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
import com.mmdb.core.utils.SimilarityUtil;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.KpiSyncMapping;
import com.mmdb.model.task.Task;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.category.IKpiCateService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.mapping.IKpiSyncMappingService;
import com.mmdb.service.task.ITaskService;
import com.mmdb.util.FileManager;

/**
 * 用于批量导入kpi数据,kpi全部是一值映射,及没有时间重复次数默认为0
 * 
 * 
 * @author xiongjian
 * 
 */
public class KpiMappingRest extends BaseRest {
	private IKpiSyncMappingService kpiMappingService;
	private IDataSourceService dataSourceService;
	private IKpiCateService kpiCateService;
	private ITaskService taskService;

	@Override
	public void  ioc(ApplicationContext context) {
		dataSourceService = (IDataSourceService) SpringContextUtil
				.getApplicationContext().getBean("dataSourceService");

		kpiMappingService = (IKpiSyncMappingService) SpringContextUtil
				.getApplicationContext().getBean("kpiSyncMappingService");

		kpiCateService = (IKpiCateService) SpringContextUtil
				.getApplicationContext().getBean("kpiCateService");

		taskService = (ITaskService) SpringContextUtil.getApplicationContext()
				.getBean("taskService");
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		}else if ("owner".equals(param1)) {
			String param2 = getValue("param2");
			return getByUser(param2);
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
		try {
			log.iLog("getAll");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<KpiSyncMapping> all = kpiMappingService.getAll();
			for (KpiSyncMapping mapping : all) {
				Map<String, Object> asMap = mapping.asMap();
				asMap.put("text", asMap.get("name"));
				list.add(asMap);
			}
			ret.put("data", list);
			ret.put("message", "获取所有KPI同步映射成功");
		} catch (Exception e) {
			throw e;
		}
		return new JsonRepresentation(ret.toString());
	}
	
	/**
	 * 获取到某个用户的映射
	 * 
	 * @param username
	 * @return
	 * @throws Exception
	 */
	private Representation getByUser(String username) throws Exception {
		JSONObject ret = new JSONObject();
		try {
			log.iLog("getByUser");
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<KpiSyncMapping> all = kpiMappingService.getByOwner(username);
			for (KpiSyncMapping mapping : all) {
				Map<String, Object> asMap = mapping.asMap();
				asMap.put("text", asMap.get("name"));
				list.add(asMap);
			}
			ret.put("data", list);
			ret.put("message", "获取所有KPI同步映射成功");
		} catch (Exception e) {
			throw e;
		}
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
		try {
			log.dLog("getById");
			KpiSyncMapping mapping = kpiMappingService.getById(id);
			if (mapping != null) {
				Map<String, Object> asMap = mapping.asMap();
				asMap.put("text", asMap.get("name"));
				ret.put("data", asMap);
				ret.put("message", "获取KPI同步映射成功");
			} else {
				//ret.put("message", "KPI同步射不存在");
				//getResponse().setStatus(new Status(600));
				throw new MException("KPI同步射不存在");
			}
		} catch (Exception e) {
			throw e;
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
	private Representation exportData() throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("exportData");
		File file = FileManager.getInstance().createFile(
				"KPI同步映射-" + TimeUtil.getTime(TimeUtil.YMD), "json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
		try {
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			List<KpiSyncMapping> cds = kpiMappingService.getAll();

			for (KpiSyncMapping cd : cds) {
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
			throw e;
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
	private Representation importData(Representation entity) throws Exception {
		log.dLog("importData");
		JSONObject ret = new JSONObject();
		DiskFileItemFactory factory = new DiskFileItemFactory();
		RestletFileUpload upload = new RestletFileUpload(factory);

		List<FileItem> items = null;
		try {
			items = upload.parseRepresentation(entity);
		} catch (FileUploadException e) {
			log.eLog(e);
			throw e;
		}

		String filename = "";
		FileItem fi = items.get(0);
		try {
			filename = fi.getName();
			if (filename == null || filename.equals("")
					|| filename.toLowerCase().trim().indexOf(".json") == -1) {
				log.eLog("文件格式有误");
				//ret.put("message", "文件格式有误");
				//getResponse().setStatus(new Status(600));
				throw new MException("文件格式错误");
				//return new JsonRepresentation(ret.toString());
			}

//			BufferedReader in = new BufferedReader(new InputStreamReader(
//					fi.getInputStream()));
//			StringBuffer buffer = new StringBuffer();
//			String line = "";
//			while ((line = in.readLine()) != null) {
//				buffer.append(line);
//			}
			
			
			InputStream inputStream = fi.getInputStream();
			byte[] t = new byte[inputStream.available()];
			int read = inputStream.read(t);
			
			String userName = this.getUsername();
			JSONArray list = JSONArray.fromObject(new String(t,"utf-8"));
			int add = 0;
			int upd = 0;
			for (int i = 0; i < list.size(); i++) {
				JSONObject obj = list.getJSONObject(i);
				String dataSourceName = obj.getString("dataSourceName");
				String cateName = obj.getString("cateName");
				DataSourcePool dataSource = dataSourceService
						.getByName(dataSourceName);
				KpiCategory cate = kpiCateService.getByName(cateName);
				if (dataSource != null && cate != null) {
					String name = obj.getString("name");
					KpiSyncMapping cd = kpiMappingService.getByName(name);
					if (cd == null) {
						cd = new KpiSyncMapping();
						cd.setName(name);
						cd.setCateId(cate.getId());
						cd.setCate(cate);
						cd.setDataSource(dataSource);
						cd.setDataSourceId(dataSource.getId());
						JSONObject fieldMap;
						try {
							fieldMap = obj.getJSONObject("fieldMap");
						} catch (Exception e) {
							fieldMap = new JSONObject();
						}
						cd.setFieldMap(fieldMap);
						Object owner = obj.get("owner");
						if(owner==null){
							owner = userName;
						}
						cd.setOwner((String)owner);
						cd = kpiMappingService.save(cd);
						add++;
					} else {
						cd.setDataSource(dataSource);
						cd.setName(name);
						cd.setCate(cate);
						cd.setCateId(cate.getId());
						cd.setDataSourceId(dataSource.getId());
						JSONObject fieldMap;
						try {
							fieldMap = obj.getJSONObject("fieldMap");
						} catch (Exception e) {
							fieldMap = new JSONObject();
						}
						cd.setFieldMap(fieldMap);
						cd = kpiMappingService.update(cd);
						upd++;
					}
				}
			}

			ret.put("message", "新建外部分类映射" + add + "条,修改外部分类映射" + upd + "条");
			log.dLog("importData success");
		} catch (Exception e) {
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
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
			log.dLog("save SourceToCategoryMapping");
			if (cdMap == null || cdMap.size() == 0) {
				throw new MException("KPI同步映射参数不能空");
			}
			String name = (String) cdMap.get("name");
			if (name == null || name.equals("")) {
				throw new MException("名称不能空");
			}
			if (!cdMap.containsKey("cateId") || "".equals(cdMap.get("cateId"))) {
				throw new MException("KPI分类不存在");
			}
			if (!cdMap.containsKey("dataSourceId")
					|| "".equals(cdMap.get("dataSourceId"))) {
				throw new MException("数据源不存在");
			}

			JSONObject fieldMap = null;
			try {
				fieldMap = cdMap.getJSONObject("fieldMap");
			} catch (Exception e) {
				fieldMap = new JSONObject();
			}
			String cateId = null;
			try {
				cateId = cdMap.getString("cateId");
			} catch (Exception e) {
			}
			String dataSourceId = null;
			try {
				dataSourceId = cdMap.getString("dataSourceId");
			} catch (Exception e) {
			}

			KpiCategory cate = kpiCateService.getById(cateId);
			if (cate == null) {
				throw new MException("KPI分类不存在");
			}
			DataSourcePool dsp = dataSourceService.getById(dataSourceId);
			if (dsp == null) {
				throw new MException("数据源不存在");
			}

			KpiSyncMapping mapping = new KpiSyncMapping(name, cate, dsp,
					dataSourceId, fieldMap);
			//所有者
			String userName = this.getUsername();
			mapping.setOwner(userName);
			
			kpiMappingService.save(mapping);

			ret.put("message", "保存成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
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
			log.dLog("update KPI同步映射");
			if (cdMap == null || cdMap.size() == 0) {
				throw new MException("KPI同步映射参数不能空");
			}

			String id = cdMap.getString("id");
			JSONObject fieldMap = null;
			String dataSourceId = null;
			String cateId = null;
			String name = (String) cdMap.get("name");

			if (name == null || name.equals("")) {
				throw new MException("名称不能空");
			}

			if (!cdMap.containsKey("cateId") || "".equals(cdMap.get("cateId"))) {
				throw new MException("KPI分类不存在");
			}

			if (!cdMap.containsKey("dataSourceId")
					|| "".equals(cdMap.get("dataSourceId"))) {
				throw new MException("数据源不存在");
			}

			try {
				fieldMap = cdMap.getJSONObject("fieldMap");
			} catch (Exception e) {
				fieldMap = new JSONObject();
			}

			try {
				cateId = cdMap.getString("cateId");
			} catch (Exception e) {
			}

			try {
				dataSourceId = cdMap.getString("dataSourceId");
			} catch (Exception e) {
			}

			KpiSyncMapping mapping = kpiMappingService.getById(id);
			KpiCategory cate = kpiCateService.getById(cateId);
			DataSourcePool dsp = dataSourceService.getById(dataSourceId);

			if (mapping == null) {
				throw new MException("KPI同步映射[" + id + "]不存在");
			}

			if (cate == null) {
				throw new MException("KPI分类不存在");
			}
			if (dsp == null) {
				throw new MException("数据源不存在");
			}
			mapping.setCate(cate);
			mapping.setCateId(cateId);
			mapping.setDataSource(dsp);
			mapping.setDataSourceId(dsp.getId());
			mapping.setFieldMap(fieldMap);

			kpiMappingService.update(mapping);

			ret.put("message", "修改成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
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
			kpiMappingService.delAll();
			ret.put("message", "删除全部成功");
		} catch (Exception e) {
			//e.printStackTrace();
			//log.eLog(e.getMessage());
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
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
			log.dLog("delete DataSourcePool");
			if (id == null || id.equals("")) {
				throw new MException("参数不能空");
			}
			KpiSyncMapping kpi = kpiMappingService.getById(id);
			if (kpi == null) {
				throw new MException("KPI同步映射不存在");
			}
			List<Task> tasks = taskService.getAll();
			for (Task task : tasks) {
				List<String> cds = task.getDbCiCateMapIds();
				if (cds != null) {
					for (String cId : cds) {
						if (id.equals(cId)) {
							throw new MException("KPI同步映射[" + kpi.getName()
									+ "]正在被任务使用");
						}
					}
				}
			}
			kpiMappingService.delById(id);
			ret.put("message", "删除成功");
		} catch (Exception e) {
			//log.eLog(e);
			//ret.put("message", e.getMessage());
			//getResponse().setStatus(new Status(600));
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
			log.dLog("立即执行KPI同步映射");
			String id = cdMap.getString("id");
			if (id == null || id.equals("")) {
				throw new Exception("映射ID不能为空");
			}
			Map<String, Integer> rm = kpiMappingService.runNow(id);
			ret.put("message",
					"新建数据(" + rm.get("save") + ")条,更新数据(" + rm.get("update")
							+ ")条,删除数据(" + rm.get("delete") + ")条");
		} catch (Exception me) {
			//log.eLog(me);
			//ret.put("message", me.getMessage());
			//getResponse().setStatus(new Status(600));
			throw me;
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 对比
	 * 
	 * @param ccMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation compare(JSONObject cdMap) throws Exception {
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
			//log.eLog(me);
			//ret.put("message", me.getMessage());
			//getResponse().setStatus(new Status(600));
			throw me;
		}
		return new JsonRepresentation(ret.toString());
	}

	// private Map<String, DataSourcePool> getDataSourcePoolMap() throws
	// Exception {
	// List<DataSourcePool> dps = dataSourceService.getAll();
	// Map<String, DataSourcePool> dpMap = new HashMap<String,
	// DataSourcePool>();
	// for (DataSourcePool dp : dps) {
	// dpMap.put(dp.getId(), dp);
	// }
	// return dpMap;
	// }
	//
	// private Map<String, CiCategory> getCiCateMap() throws Exception {
	// List<CiCategory> cates = ciCateService.getAll();
	// Map<String, CiCategory> cateMap = new HashMap<String, CiCategory>();
	// for (CiCategory cate : cates) {
	// cateMap.put(cate.getId(), cate);
	// }
	// return cateMap;
	// }
}
