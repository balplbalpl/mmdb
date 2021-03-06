package com.mmdb.rest.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdbc.JdbcConnection;
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
import com.mmdb.core.utils.JsonUtil;
import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.model.bean.User;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.db.IDataSourceService;
import com.mmdb.util.FileManager;
import com.mmdb.util.JdbcOtherTools;

public class HandleDataBaseRest extends BaseRest {
	private IDataSourceService dbSourceService;
	private IDataBaseConfigService dbConfigService;

	@Override
	public void ioc(ApplicationContext context) {
		dbSourceService = (IDataSourceService) SpringContextUtil
				.getApplicationContext().getBean("dataSourceService");

		dbConfigService = (IDataBaseConfigService) SpringContextUtil
				.getApplicationContext().getBean("dbConfigService");
	}

	@Override
	public Representation getHandler() throws Exception {
		String param1 = getValue("param1");
		if (param1 == null || "".equals(param1)) {
			return getAll();
		} else if ("export".equals(param1)) {
			return exportData();
		} else if ("author".equals(param1)) {
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
		if ("test".equals(param1)) {
			return testConnect(params);
		} else if ("getschemanames".equals(param1)) {
			return getSchemaNames(params);
		} else if ("gettablenamebyschema".equals(param1)) {
			return getTableNameBySchema(params);
		} else if ("getfieldnamebytable".equals(param1)) {
			return getFieldNameByTable(params);
		} else if ("getmetadatabytable".equals(param1)) {
			return getMetadataByTable(params);
		} else if ("getfieldnamebydsid".equals(param1)) {
			return getFieldNameByDsId(params);
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
	 * 返回一个json格式全部
	 * 
	 * @return
	 * @throws Exception
	 */
	private Representation getAll() throws Exception {
		JSONObject ret = new JSONObject();
		List<DataBaseConfig> all = dbConfigService.getAll();
		JSONArray data = new JSONArray();
		for (DataBaseConfig config : all) {
			Map<String, String> asMap = config.asMap();
			data.add(asMap);
		}
		ret.put("data", data);
		ret.put("message", "获取数据库配置成功");
		return new JsonRepresentation(ret.toString());
	}

	private Representation getByUser(String username) throws Exception {
		JSONObject ret = new JSONObject();
		List<DataBaseConfig> all = dbConfigService.getByAuthor(username);
		JSONArray data = new JSONArray();
		for (DataBaseConfig config : all) {
			Map<String, String> asMap = config.asMap();
			data.add(asMap);
		}
		ret.put("data", data);
		ret.put("message", "获取数据库配置成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过唯一id获取ciCategory
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	private Representation getById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("getByid");
		DataBaseConfig config = dbConfigService.getById(id);
		if (config != null) {
			Map<String, String> asMap = config.asMap();
			ret.put("data", asMap);
			ret.put("message", "获取数据库配置成功");
		} else {
			throw new MException("数据库配置不存在");
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation exportData() {
		JSONObject ret = new JSONObject();

		log.dLog("getAllForXls");
		File file = FileManager.getInstance().createFile("databaseConfig",
				"json");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			List<DataBaseConfig> dbConfigs = dbConfigService.getAll();
			Map<String, Map<String, String>> retMap = new HashMap<String, Map<String, String>>();
			for (DataBaseConfig nc : dbConfigs) {
				Map<String, String> asMap = nc.asMap();
				retMap.put(nc.getId(), asMap);
			}
			String json = JsonUtil.encodeByJackSon(retMap);

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(json.getBytes("utf-8"));
				fos.flush();
			} catch (Exception e) {
			} finally {
				fos.close();
			}
			ret.put("message", "下载数据库配置成功");
			JSONObject retData = new JSONObject();
			retData.put("url", file.getName());
			ret.put("data", retData);
			log.dLog("exportXML success");

		} catch (Exception e) {
		}
		return new JsonRepresentation(ret.toString());
	}

	@SuppressWarnings("unchecked")
	private Representation importData(Representation entity) throws Exception {
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

		String filename = "";
		FileItem fi = items.get(0);
		filename = fi.getName();
		if (filename == null || filename.equals("")
				|| filename.toLowerCase().trim().indexOf(".json") == -1) {
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

		Map<String, Map<String, String>> xMap = JsonUtil.decodeByJackSon(
				buffer.toString(), Map.class);

		Iterator<Entry<String, Map<String, String>>> iter = xMap.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Entry<String, Map<String, String>> entry = iter.next();
			String key = entry.getKey();
			Map<String, String> val = entry.getValue();
			boolean rac = Boolean.valueOf(val.get("isRac").toString());
			DataBaseConfig dc = dbConfigService.getById(key);
			if (dc == null) {
				dc = new DataBaseConfig(rac, val);
				dc.setId(key);
				dc.setOwner(val.get("owner"));
				dbConfigService.save(dc);
			} else {
				String type = val.get("type");
				String username = val.get("username");
				String password = val.get("password");
				if (rac) {
					String addressUrl = val.get("url");
					dc.setRacAddress(addressUrl);
				} else {
					String hostName = val.get("url");
					int port = Integer.valueOf(val.get("port"));
					String dataBaseName = val.get("database");
					dc.setHostName(hostName);
					if (type.equals("oracle") || type.equals("db2")) {
						if (dataBaseName == null || dataBaseName.equals("")) {
							throw new MException("数据库[" + key + "]实例不能为空");
						}
						dc.setDatabaseName(dataBaseName);
					}
					dc.setPort(port);
				}
				dc.setRac(rac);
				dc.setType(type);
				dc.setUsername(username);
				dc.setPassword(password);
				String owner = val.get("owner");
				if (owner == null || "".equals(owner)) {
					owner = user.getLoginName();
				}
				dc.setOwner(owner);
				dbConfigService.update(dc);
			}
		}
		ret.put("message", "导入数据库配置成功");
		log.dLog("importDataBaseConfig success");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 测试连接是否能用
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonRepresentation testConnect(JSONObject dbMap) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("testing");
			if (dbMap == null || dbMap.size() == 0) {
				throw new Exception("数据库配置参数不能空");
			}
			boolean isRac = dbMap.getBoolean("isRac");
			new DataBaseConfig(isRac, dbMap);
			connection = JdbcOtherTools.getConnection(isRac, dbMap);
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			ret.put("data", dbMap);
			ret.put("message", "数据库连接成功");
		} finally {
			JdbcOtherTools.closeConnection(connection);
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
	private JsonRepresentation save(JSONObject dbMap) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("saveDataBaseConfig");
			if (dbMap == null || dbMap.size() == 0) {
				throw new MException("DataSource参数不能空");
			}
			String name = (String) dbMap.get("name");
			if (name == null || name.equals("")) {
				throw new MException("名称不能空");
			}

			// 增加id处理防止重复
			String type = (String) dbMap.get("type");
			if (type != null) {
				// id = type + "_" + id;
				dbMap.put("name", name);
			}

			DataBaseConfig dc = dbConfigService.getByName(name);
			// if (dc != null) {
			// throw new Exception("DataSource[" + name + "]已存在");
			// }
			boolean isRac = dbMap.getBoolean("isRac");
			// String rac = (String) dbMap.get("isRac");
			// if (rac != null && rac.equals("true")) {
			// isRac = true;
			// }
			dc = new DataBaseConfig(isRac, dbMap);
			// 增加作者
			User user = getUser();
			dc.setOwner(user.getLoginName());
			connection = JdbcOtherTools.getConnection(isRac, dbMap);
			if (connection == null) {
				throw new Exception("数据库连接失败");
			}
			dc = dbConfigService.save(dc);
			ret.put("data", dc.asMap());
			ret.put("message", "保存成功");
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	private JsonRepresentation update(JSONObject dbMap) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("update DataBaseConfig");
			if (dbMap == null || dbMap.size() == 0) {
				throw new Exception("DataSource参数不能空");
			}
			String id = dbMap.getString("id");
			DataBaseConfig dc = dbConfigService.getById(id);
			if (dc == null) {
				throw new MException("DataSource[" + id + "]不存在");
			}
			boolean isRac = dbMap.getBoolean("isRac");
			// String rac = dbMap.getString("isRac");
			// if (rac != null && rac.equals("true")) {
			// isRac = true;
			// }
			new DataBaseConfig(isRac, dbMap);
			connection = JdbcOtherTools.getConnection(isRac, dbMap);
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			dc.setType(dbMap.getString("type"));
			dc.setRac(isRac);
			if (isRac) {
				dc.setRacAddress(dbMap.getString("url"));
			} else {
				dc.setDatabaseName(dbMap.getString("database"));
				dc.setPort(Integer.valueOf(dbMap.getString("port")));
				dc.setHostName(dbMap.getString("url"));
			}
			dc.setUsername(dbMap.getString("username"));
			dc.setPassword(dbMap.getString("password"));
			dc = dbConfigService.update(dc);
			ret.put("data", dc.asMap());
			ret.put("message", "更新成功");
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	private Representation deleteAll() throws MException {
		throw new MException("不支持删除全部");
	}

	private JsonRepresentation deleteById(String id) throws Exception {
		JSONObject ret = new JSONObject();
		log.dLog("deleteDataBaseConfigById");
		if (id == null || id.equals("")) {
			throw new Exception("参数不能空");
		}
		DataBaseConfig dc = dbConfigService.getById(id);
		if (dc == null) {
			throw new MException("DataSource[" + id + "]不存在");
		}
		if (dbSourceService.getDataConfigIds().contains(dc.getId())) {
			throw new MException("该DataSource已被配置成DataSet，请先删除DataSet");
		}
		dbConfigService.delete(dc);
		ret.put("message", "删除成功");
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据库连接获取SCHEMA
	 * 
	 * @return
	 * @throws Exception 
	 */
	private JsonRepresentation getSchemaNames(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("getSchemaNames");
			if (params == null || params.size() == 0) {
				throw new MException("通过数据库连接获取SCHEMA参数不能空");
			}
			String dcId = params.getString("dcId");
			if (dcId == null || dcId.equals("")) {
				throw new MException("数据库连接ID不能空");
			}
			DataBaseConfig dc = dbConfigService.getById(dcId);
			if (dc == null) {
				throw new MException("DataSource[" + dcId + "]不存在");
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), dc.asMap());
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			List<String> schemas = JdbcConnection.getDatabases(connection);
			ret.put("schemas", schemas);
			ret.put("message", "获取SCHEMA成功");
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据库连接和SCHEMA获取表和视图
	 * 
	 * @return
	 * @throws Exception 
	 */
	private JsonRepresentation getTableNameBySchema(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("getTableNameBySchema");
			if (params == null || params.size() == 0) {
				throw new MException("通过数据库连接和SCHEMA获取表和视图参数不能空");
			}
			String dcId = params.getString("dcId");
			if (dcId == null || dcId.equals("")) {
				throw new MException("数据库连接ID不能空");
			}
			DataBaseConfig dc = dbConfigService.getById(dcId);
			if (dc == null) {
				throw new MException("DataSource[" + dcId + "]不存在");
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), dc.asMap());
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			if (params.containsKey("schema")
					&& params.getString("schema").length() > 0) {
				String schemaName = params.getString("schema");
				Map<String, List<String>> tables = JdbcConnection.getTables(
						connection, schemaName);
				if (tables == null) {
					throw new MException("获取数据库表失败");
				} else {
					ret = JSONObject.fromObject(tables);
					ret.put("message", "获取表名成功");
				}
			} else {
				throw new MException("SCHEMA值不能空");
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据库连接、SCHEMA和表名获取字段名
	 * 
	 * @return
	 * @throws Exception 
	 */
	private JsonRepresentation getFieldNameByTable(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("getFieldNameByTable");
			if (params == null || params.size() == 0) {
				throw new MException("通过数据库连接、SCHEMA和表名获取字段名参数不能空");
			}
			String dcId = params.getString("dcId");
			if (dcId == null || dcId.equals("")) {
				throw new MException("数据库连接ID不能空");
			}
			DataBaseConfig dc = dbConfigService.getById(dcId);
			if (dc == null) {
				throw new MException("DataSource[" + dcId + "]不存在");
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), dc.asMap());
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			String schemaName = null;
			if (params.containsKey("schema")) {
				schemaName = params.getString("schema");
			}
			String tableName = null;
			if (params.containsKey("table")) {
				tableName = params.getString("table");
			}
			String customSql = null;
			if (params.containsKey("customSql")) {
				customSql = params.getString("customSql");
			}
			if (schemaName != null) {
				boolean isSelf = params.getBoolean("isSelf");
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
				JSONObject data = JSONObject.fromObject(columns);
				ret.put("data", data);
				ret.put("message", "获取DataSource字段成功");
			} else {
				throw new MException("SCHEMA值不能空");
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据集的ID获取字段名
	 * 
	 * @param json
	 *            {dsId:1239987478978213}
	 * 
	 * @return JsonRepresentation
	 * @throws Exception 
	 */
	private JsonRepresentation getFieldNameByDsId(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("getFieldNameByDcId");
			if (params == null || params.size() == 0) {
				throw new MException("参数不能空");
			}
			String dsId = params.getString("dsId");
			if (dsId == null || dsId.equals("")) {
				throw new MException("数据库连接ID不能空");
			}
			DataSourcePool dp = dbSourceService.getById(dsId);

			// 获取数据库配置连接
			String dcId = dp.getDatabaseConfigId();
			DataBaseConfig dc = dbConfigService.getById(dcId);
			if (dc == null) {
				throw new MException("DataSource[" + dcId + "]不存在");
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
				JSONObject data = JSONObject.fromObject(columns);
				ret.put("data", data);
				ret.put("message", "获取DataSource字段成功");
			} else {
				throw new MException("SCHEMA值不能空");
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}

	/**
	 * 通过数据库连接和SQL获取结果集
	 * 
	 * @return
	 * @throws Exception 
	 */
	private JsonRepresentation getMetadataByTable(JSONObject params) throws Exception {
		JSONObject ret = new JSONObject();
		Connection connection = null;
		try {
			log.dLog("getMetadataByTable");
			if (params == null || params.size() == 0) {
				throw new MException("通过数据库连接和SQL获取结果集");
			}
			String dcId = params.getString("dcId");
			if (dcId == null || dcId.equals("")) {
				throw new MException("数据库连接ID不能空");
			}
			DataBaseConfig dc = dbConfigService.getById(dcId);
			if (dc == null) {
				throw new MException("DataSource[" + dcId + "]不存在");
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), dc.asMap());
			if (connection == null) {
				throw new MException("数据库连接失败");
			}
			String schemaName = null;
			if (params.containsKey("schema")) {
				schemaName = params.getString("schema");
			}
			String tableName = null;
			if (params.containsKey("table")) {
				tableName = params.getString("table");
			}
			String customSql = null;
			if (params.containsKey("customSql")) {
				customSql = params.getString("customSql");
			}
			if (schemaName != null) {
				boolean isSelf = params.getBoolean("isSelf");
				Integer page = params.getInt("page");
				Integer pageSize = params.getInt("pageSize");
				List<Map<String, Object>> data = null;
				if (isSelf) {// 取自定义SQL
					if (customSql != null && customSql.length() > 0) {
						data = JdbcConnection.getDataByTable(connection,
								schemaName, "", customSql, (page - 1)
										* pageSize + 1, page * pageSize);
					} else {
						throw new MException("自定义SQL不能空");
					}
				} else {// 取表
					if (tableName != null && tableName.length() > 0) {
						data = JdbcConnection.getDataByTable(connection,
								schemaName, tableName, "", (page - 1)
										* pageSize + 1, page * pageSize);
					} else {
						throw new MException("表名不能空");
					}
				}
				if (data == null) {
					throw new MException("获取数据库表数据失败");
				}
				int count = JdbcConnection.getCountSize(connection, schemaName,
						tableName, customSql);
				ret.put("count", count);
				ret.put("page", page);
				ret.put("pageSize", pageSize);
				ret.put("datas", data);
				ret.put("message", "获取数据库表数据成功");
			} else {
				throw new MException("SCHEMA值不能空");
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		return new JsonRepresentation(ret.toString());
	}
}
