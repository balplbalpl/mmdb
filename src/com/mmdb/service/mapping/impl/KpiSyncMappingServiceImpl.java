package com.mmdb.service.mapping.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdbc.JdbcConnection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.database.bean.DataBaseConfig;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.info.KpiInformation;
import com.mmdb.model.info.storage.KpiInfoStorage;
import com.mmdb.model.mapping.KpiSyncMapping;
import com.mmdb.model.mapping.storage.KpiSyncMappingStorage;
import com.mmdb.service.db.IDataBaseConfigService;
import com.mmdb.service.mapping.IKpiSyncMappingService;
import com.mmdb.util.HexString;
import com.mmdb.util.JdbcOtherTools;

@Service("kpiSyncMappingService")
public class KpiSyncMappingServiceImpl implements IKpiSyncMappingService {
	@Autowired
	private KpiSyncMappingStorage ksmStorage;
	@Autowired
	private KpiInfoStorage kInfoStorage;
	@Autowired
	private IDataBaseConfigService dbConfigService;

	@Override
	public List<KpiSyncMapping> getAll() throws Exception {
		return ksmStorage.getAll();
	}
	@Override
	public List<KpiSyncMapping> getByOwner(String username) throws Exception {
		return ksmStorage.getByProperty("owner", username);
	}
	
	
	@Override
	public KpiSyncMapping getById(String id) throws Exception {
		return ksmStorage.getById(id);
	}

	@Override
	public KpiSyncMapping getByName(String name) throws Exception {
		return ksmStorage.getByName(name);
	}

	@Override
	public KpiSyncMapping save(KpiSyncMapping kpiMapping) throws Exception {
		return ksmStorage.save(kpiMapping);
	}

	@Override
	public KpiSyncMapping update(KpiSyncMapping kpiMapping) throws Exception {
		return ksmStorage.update(kpiMapping);
	}

	@Override
	public void delById(String id) throws Exception {
		ksmStorage.delById(id);

	}

	@Override
	public void delAll() throws Exception {
		ksmStorage.delAll();
	}

	@Override
	public boolean existByName(String name) throws Exception {
		return ksmStorage.existByName(name);
	}

	@Override
	public boolean existById(String id) throws Exception {
		return ksmStorage.existById(id);
	}

	@Override
	public Map<String, Integer> runNow(String id) throws Exception {
		KpiSyncMapping ksm = ksmStorage.getById(id);
		if (ksm == null)
			throw new Exception("KPI同步映射不存在");
		Map<String, String> fieldMap = ksm.getFieldMap();
		KpiCategory cate = ksm.getCate();
		int crCount = 0;
		int upCount = 0;
		String owner = ksm.getOwner();
		Connection connection = null;
		try {
			DataBaseConfig dc = dbConfigService.getById(ksm.getDataSource()
					.getDatabaseConfigId());
			DataSourcePool ds = ksm.getDataSource();
			Map<String, String> am = dc.asMap();
			if (dc.getRac()) {
				am.put("url", dc.getRacAddress());
			}
			connection = JdbcOtherTools.getConnection(dc.getRac(), am);
			if (connection == null) {
				throw new Exception("获取数据库连接失败");
			}
			int count = JdbcConnection.getCountSize(connection, ds.getSchema(),
					ds.getTableName(), ds.getCustomSql());
			int pageSize = 1000;
			int page = count % pageSize == 0 ? count / pageSize
					: (count / pageSize) + 1;
			for (int i = 0; i < page; i++) {
				List<Map<String, Object>> data;

				int startCount = i * pageSize + 1, endCount = (i + 1)
						* pageSize;
				if (ds.isSelf()) {
					data = JdbcConnection.getDataByTable(connection, "", "",
							ds.getCustomSql(), startCount, endCount);
				} else {
					data = JdbcConnection.getDataByTable(connection,
							ds.getSchema(), ds.getTableName(), "", startCount,
							endCount);
				}
				if (data == null) {
					throw new Exception("获取数据库数据失败");
				}
				List<KpiInformation> kInfos = new ArrayList<KpiInformation>(
						data.size());
				for (Map<String, Object> m : data) {
					KpiInformation kInfo = handlerData(cate, m, fieldMap);
					if (kInfo != null){
						kInfo.setOwner(owner);
						kInfos.add(kInfo);
					}
				}
				List<List<KpiInformation>> rs = kInfoStorage.saveOrUpdate(cate,
						kInfos);
				if (rs != null) {
					crCount += rs.get(0).size();
					upCount += rs.get(1).size();
				}
			}
		} finally {
			JdbcOtherTools.closeConnection(connection);
		}
		Map<String, Integer> ret = new HashMap<String, Integer>();
		ret.put("save", crCount);
		ret.put("update", upCount);
		ret.put("delete", 0);
		return ret;
	}

	/**
	 * 将数据转换成阈值
	 * 
	 * @param data
	 * @param fieldMap
	 * @return
	 */
	private KpiInformation handlerData(KpiCategory cate,
			Map<String, Object> data, Map<String, String> fieldMap) {
		KpiInformation ret = new KpiInformation();
		String mName = fieldMap.get("name");
		String mUnit = fieldMap.get("unit");

		// 阈值部分
		String mRepeat = fieldMap.get("repeat");

		String ll1 = (String) data.get(fieldMap.get("severity1lowLimit"));
		String hl1 = (String) data.get(fieldMap.get("severity1highLimit"));

		String ll2 = (String) data.get(fieldMap.get("severity2lowLimit"));
		String hl2 = (String) data.get(fieldMap.get("severity2highLimit"));

		String ll3 = (String) data.get(fieldMap.get("severity3lowLimit"));
		String hl3 = (String) data.get(fieldMap.get("severity3highLimit"));

		String ll4 = (String) data.get(fieldMap.get("severity4lowLimit"));
		String hl4 = (String) data.get(fieldMap.get("severity4highLimit"));

		String ll5 = (String) data.get(fieldMap.get("severity5lowLimit"));
		String hl5 = (String) data.get(fieldMap.get("severity5highLimit"));

		int repeat = 0;
		try {
			repeat = Integer.parseInt((String) data.get(mRepeat));
		} catch (Exception e) {
		}

		JSONObject one = new JSONObject();
		JSONArray ths = new JSONArray();
		one.put("repeat", ""+repeat);
		one.put("startTime", "");
		one.put("endTime", "");
		

		// 组织级别
		if (ll5 != null && hl5 != null) {// 最高级别的
			try {
				int l5 = Integer.parseInt(ll5);
				int h5 = Integer.parseInt(hl5);
				JSONObject severity5 = new JSONObject();
				severity5.put("severity", ""+5);
				severity5.put("lowLimit", ""+l5);
				severity5.put("highLimit", ""+h5);
				ths.add(severity5);
			} catch (Exception e) {// 空值或者不为数值无效
			}
		}

		if (ll4 != null && hl4 != null) {// 最高级别的
			try {
				int l4 = Integer.parseInt(ll4);
				int h4 = Integer.parseInt(hl4);
				JSONObject severity4 = new JSONObject();
				severity4.put("severity", ""+4);
				severity4.put("lowLimit", ""+l4);
				severity4.put("highLimit", ""+h4);
				ths.add(severity4);
			} catch (Exception e) {// 空值或者不为数值无效
			}
		}
		if (ll3 != null && hl3 != null) {// 最高级别的
			try {
				int l3 = Integer.parseInt(ll3);
				int h3 = Integer.parseInt(hl3);
				JSONObject severity = new JSONObject();
				severity.put("severity", ""+3);
				severity.put("lowLimit", ""+l3);
				severity.put("highLimit", ""+h3);
				ths.add(severity);
			} catch (Exception e) {// 空值或者不为数值无效
			}
		}
		if (ll2 != null && hl2 != null) {// 最高级别的
			try {
				int l2 = Integer.parseInt(ll2);
				int h2 = Integer.parseInt(hl2);
				JSONObject severity = new JSONObject();
				severity.put("severity", ""+2);
				severity.put("lowLimit", ""+l2);
				severity.put("highLimit", ""+h2);
				ths.add(severity);
			} catch (Exception e) {// 空值或者不为数值无效
			}
		}
		if (ll1 != null && hl1 != null) {// 最高级别的
			try {
				int l1 = Integer.parseInt(ll1);
				int h1 = Integer.parseInt(hl1);
				JSONObject severity = new JSONObject();
				severity.put("severity", ""+1);
				severity.put("lowLimit", ""+l1);
				severity.put("highLimit", ""+h1);
				ths.add(severity);
			} catch (Exception e) {// 空值或者不为数值无效
			}
		}
		// 必须有一个级别的阈值.
		if (ths.size() == 0) {
			return null;
		}
		one.put("threshold", ths);
		String name = (String) data.get(mName);
		String unit = (String) data.get(mUnit);
		String source = "";
		String threshold = "[" + one.toString() + "]";
		String kpiHex = HexString.encode(HexString.json2Str(cate.getName(),
				name));

		ret.setName(name);
		ret.setKpiCategory(cate);
		ret.setKpiCategoryId(cate.getId());
		ret.setKpiHex(kpiHex);
		ret.setSource(source);
		ret.setKpiCategoryName(cate.getName());
		ret.setUnit(unit);
		ret.setThreshold(threshold);

		return ret;
	}
}
