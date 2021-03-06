package com.mmdb.rest.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdbc.JdbcConnection;
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
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.User;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.mapping.SourceToCategoryMapping;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.service.mapping.ISourceCategoryMapService;
import com.mmdb.service.mapping.ISourceRelationMapService;
import com.mmdb.service.role.IUserService;
import com.mmdb.util.FileManager;
import com.mmdb.util.JdbcOtherTools;
import com.mmdb.util.des.Des;

public class DatasetRest extends BaseRest {
	private IDataSourceService dbSourceService;
	private IDataBaseConfigService dbConfigService;
	private ISourceCategoryMapService sourceCateMapService;
	private ISourceRelationMapService sourceRelMapService;

	@Override
	public void ioc(ApplicationContext context) {
		dbSourceService = context.getBean(IDataSourceService.class);
		dbConfigService = context.getBean(IDataBaseConfigService.class);
		sourceCateMapService = context.getBean(ISourceCategoryMapService.class);
		sourceRelMapService = context.getBean(ISourceRelationMapService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		} else if ("owner".equals(param1)) {
			String param2 = getValue("param2");
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
		if ("getfieldnamebydataset".equals(param1)) {
			return getFieldNameByDataset(params);
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
	 * 获取所有数据集
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<DataSourcePool> dps = dbSourceService.getAll();
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		for (DataSourcePool dp : dps) {
			Map<String, Object> dbMap = dp.asMap();
			dbMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
			list.add(dbMap);
		}
		ret.put("data", list);
		ret.put("message", "获取所有数据集成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取数据集
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getById");
		DataSourcePool dp = dbSourceService.getById(id);
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		if (dp != null) {
			Map<String, Object> asMap = dp.asMap();
			asMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
			ret.put("data", asMap);
			ret.put("message", "获取数据集成功");
		} else {
			throw new MException("数据集不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByUser(String username) throws Exception {
		JSONObject ret = new JSONObject();
		log.iLog("getAll");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<DataSourcePool> dps = dbSourceService.getByAuthor(username);
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		for (DataSourcePool dp : dps) {
			Map<String, Object> dbMap = dp.asMap();
			dbMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
			list.add(dbMap);
		}
		ret.put("data", list);
		ret.put("message", "获取所有数据集成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导出数据集
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	private Representation exportData() throws Exception {
		JSONObject ret = new JSONObject();

		log.dLog("exportData");
		File file = FileManager.getInstance()
				.createFile("databasePool", "json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		List<DataSourcePool> dps = dbSourceService.getAll();
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		for (DataSourcePool dp : dps) {
			Map<String, Object> dbMap = dp.asMap();
			dbMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
			list.add(dbMap);
		}
		JSONArray json = JSONArray.fromObject(list);

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(json.toString().getBytes("utf-8"));
			fos.flush();
		} catch (Exception e) {
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ret.put("message", "下载数据集成功");
		JSONObject retData = new JSONObject();
		retData.put("url", file.getName());
		ret.put("data", retData);
		log.dLog("exportXML success");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 导入数据集
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

			JSONArray dpList = JSONArray.fromObject(buffer.toString());
			for (int i = 0; i < dpList.size(); i++) {
				JSONObject dpObj = dpList.getJSONObject(i);
				String dcId = dpObj.getJSONObject("dbMap").getString("id");
				String owner = null;
				if (dpObj.containsKey("owner")) {
					owner = (String) dpObj.get("owner");
				} else {
					owner = user.getLoginName();
				}
				DataSourcePool dp = dbSourceService.getById(dcId);
				if (dp == null) {
					String name = dpObj.getString("name");
					if (name == null || "".equals(name)) {
						continue;
					}
					DataSourcePool byName = dbSourceService.getByName(name);
					if (byName != null) {
						continue;
					}
					dp = new DataSourcePool();
					dp.setName(dpObj.containsKey("name") ? dpObj
							.getString("name") : "");
					dp.setDescription(dpObj.containsKey("description") ? dpObj
							.getString("description") : "");
					dp.setSchema(dpObj.containsKey("schema") ? dpObj
							.getString("schema") : "");
					dp.setTableName(dpObj.containsKey("tableName") ? dpObj
							.getString("tableName") : "");
					dp.setCustomSql(dpObj.containsKey("customSql") ? dpObj
							.getString("customSql") : "");
					String databaseConfigId = "";
					if (dpObj.containsKey("dbMap")) {
						if (dpObj.getJSONObject("dbMap").containsKey("id")) {
							databaseConfigId = dpObj.getJSONObject("dbMap")
									.getString("id");
						}
					}
					dp.setDatabaseConfigId(databaseConfigId);
					dp.setSelf(dpObj.containsKey("isSelf") ? dpObj
							.getBoolean("isSelf") : false);
					dp.setOwner(owner);
					dbSourceService.save(dp);
				} else {
					dp.setName(dpObj.containsKey("name") ? dpObj
							.getString("name") : "");
					dp.setDescription(dpObj.containsKey("description") ? dpObj
							.getString("description") : "");
					dp.setSchema(dpObj.containsKey("schema") ? dpObj
							.getString("schema") : "");
					dp.setTableName(dpObj.containsKey("tableName") ? dpObj
							.getString("tableName") : "");
					dp.setCustomSql(dpObj.containsKey("customSql") ? dpObj
							.getString("customSql") : "");
					String databaseConfigId = "";
					if (dpObj.containsKey("dbMap")) {
						if (dpObj.getJSONObject("dbMap").containsKey("id")) {
							databaseConfigId = dpObj.getJSONObject("dbMap")
									.getString("id");
						}
					}
					dp.setDatabaseConfigId(databaseConfigId);
					dp.setSelf(dpObj.containsKey("isSelf") ? dpObj
							.getBoolean("isSelf") : false);

					dp.setOwner(owner);

					dbSourceService.update(dp);
				}
			}
			ret.put("message", "导入数据集成功");
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
	private JsonRepresentation save(JSONObject dpMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("saveDataSourcePool");
		if (dpMap == null || dpMap.size() == 0) {
			throw new MException("DataSource参数不能空");
		}
		String name = (String) dpMap.get("name");
		if (name == null || name.equals("")) {
			throw new MException("名称不能空");
		}
		DataSourcePool byName = dbSourceService.getByName(name);
		if (byName != null) {
			throw new MException("DataSource[" + name + "]已存在");
		}
		User user = getUser();
		DataSourcePool dp = new DataSourcePool();
		dp.setName(dpMap.containsKey("name") ? dpMap.getString("name") : "");
		dp.setDescription(dpMap.containsKey("description") ? dpMap
				.getString("description") : "");
		dp.setSchema(dpMap.containsKey("schema") ? dpMap.getString("schema")
				: "");
		dp.setTableName(dpMap.containsKey("tableName") ? dpMap
				.getString("tableName") : "");
		dp.setCustomSql(dpMap.containsKey("customSql") ? dpMap
				.getString("customSql") : "");
		dp.setDatabaseConfigId(dpMap.containsKey("databaseConfigId") ? dpMap
				.getString("databaseConfigId") : "");
		dp.setSelf(dpMap.containsKey("isSelf") ? dpMap.getBoolean("isSelf")
				: false);
		dp.setOwner(user.getLoginName());
		dp = dbSourceService.save(dp);
		Map<String, Object> asMap = dp.asMap();
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		asMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
		ret.put("data", asMap);
		ret.put("message", "保存成功");

		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 修改
	 * 
	 * @param dbMap
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation update(JSONObject dpMap) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("update DataSourcePool");
		if (dpMap == null || dpMap.size() == 0) {
			throw new Exception("DataSource参数不能空");
		}
		String id = dpMap.getString("id");
		DataSourcePool dp = dbSourceService.getById(id);
		if (dp == null) {
			throw new MException("DataSource不存在");
		}
		dp.setName(dpMap.containsKey("name") ? dpMap.getString("name") : "");
		dp.setDescription(dpMap.containsKey("description") ? dpMap
				.getString("description") : "");
		dp.setSchema(dpMap.containsKey("schema") ? dpMap.getString("schema")
				: "");
		dp.setTableName(dpMap.containsKey("tableName") ? dpMap
				.getString("tableName") : "");
		dp.setCustomSql(dpMap.containsKey("customSql") ? dpMap
				.getString("customSql") : "");
		dp.setDatabaseConfigId(dpMap.containsKey("databaseConfigId") ? dpMap
				.getString("databaseConfigId") : "");
		dp.setSelf(dpMap.containsKey("isSelf") ? dpMap.getBoolean("isSelf")
				: false);
		dp = dbSourceService.update(dp);
		Map<String, Object> asMap = dp.asMap();
		Map<String, Map<String, String>> dcMap = getDataBaseConfigMap();
		asMap.put("dbMap", dcMap.get(dp.getDatabaseConfigId()));
		ret.put("data", asMap);
		ret.put("message", "更新成功");
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
		dbSourceService.deleteAll();
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
		DataSourcePool dp = dbSourceService.getById(id);
		if (dp == null) {
			throw new MException("DataSource不存在");
		}
		List<SourceToCategoryMapping> ciCateMappings = sourceCateMapService
				.getAll();
		for (SourceToCategoryMapping ciCateMapping : ciCateMappings) {
			if (id.equals(ciCateMapping.getDataSource().getId())) {
				throw new MException("DataSource[" + dp.getName()
						+ "]正在被外部分类映射使用");
			}
		}
		List<SourceToRelationMapping> ciRelMappings = sourceRelMapService
				.getAll();
		for (SourceToRelationMapping ciRelMapping : ciRelMappings) {
			if (id.equals(ciRelMapping.getDataSourceId())) {
				throw new MException("DataSource[" + dp.getName()
						+ "]正在被外部关系映射使用");
			}
		}
		dbSourceService.delete(dp);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据集获取字段名
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation getFieldNameByDataset(JSONObject params)
			throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		log.dLog("getFieldNameByDataset");
		if (params == null || params.size() == 0) {
			throw new Exception("通过数据集获取字段名参数不能空");
		}
		String dataSourceId = params.getString("dataSourceId");
		if (dataSourceId == null || dataSourceId.equals("")) {
			throw new Exception("数据集ID不能空");
		}
		DataSourcePool dp = dbSourceService.getById(dataSourceId);
		if (dp == null) {
			throw new MException("DataSource不存在");
		}
		try {
			DataBaseConfig dc = dbConfigService.getById(dp
					.getDatabaseConfigId());
			if (dc == null) {
				throw new MException("DataConfig[" + dp.getDatabaseConfigId()
						+ "]不存在");
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), dc.asMap());
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			String schemaName = dp.getSchema();
			String tableName = dp.getTableName();
			String customSql = dp.getCustomSql();
			if (schemaName != null) {
				boolean isSelf = dp.isSelf();
				Map<String, Map<String, String>> columns = null;
				if (isSelf) {// 取自定义SQL
					if (customSql != null && customSql.length() > 0) {
						columns = JdbcConnection.getColumns(connection,
								schemaName, "", customSql);
					} else {
						throw new MException("自定义SQL不能空");
					}
				} else {// 取表
					if (tableName != null && tableName.length() > 0) {
						columns = JdbcConnection.getColumns(connection,
								schemaName, tableName, "");
					} else {
						throw new MException("表名不能空");
					}
				}
				if (columns == null) {
					throw new MException("获取DataSource字段失败");
				}
				ret.put("data", JSONObject.fromObject(columns));
				ret.put("message", "获取DataSource字段成功");
			} else {
				throw new Exception("SCHEMA值不能空");
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	private Map<String, Map<String, String>> getDataBaseConfigMap()
			throws Exception {
		List<DataBaseConfig> dcs = dbConfigService.getAll();
		Map<String, Map<String, String>> dcMap = new HashMap<String, Map<String, String>>();
		for (DataBaseConfig dc : dcs) {
			dcMap.put(dc.getId(), dc.asMap());
		}
		return dcMap;
	}
}
