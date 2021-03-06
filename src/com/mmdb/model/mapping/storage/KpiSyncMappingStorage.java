package com.mmdb.model.mapping.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.categroy.storage.KpiCateStorage;
import com.mmdb.model.database.bean.DataSourcePool;
import com.mmdb.model.database.storage.DataSourceStorage;
import com.mmdb.model.mapping.KpiSyncMapping;
import com.mmdb.mongo.MongoConnect;

/**
 * 用于管理kpi数据映射
 * 
 * @author xiongjian
 * 
 */
@Repository("kpiSyncMappingStorage")
public class KpiSyncMappingStorage {

	private Log log = LogFactory.getLogger("DataSourceToCategoryStorage");
	@Autowired
	private KpiCateStorage kCateStorage;
	@Autowired
	private DataSourceStorage dsStorage;

	public List<KpiSyncMapping> getAll() throws Exception {
		String sql = "select * from KpiSyncMapping";
		return query(sql);
	}

	public KpiSyncMapping getByName(String name) throws Exception {
		List<KpiSyncMapping> list = getByProperty("name", name);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("KPI同步映射[" + name + "]不唯一");
			throw new Exception("KPI同步映射[" + name + "]不唯一");
		} else {
			return null;
		}
	}

	public KpiSyncMapping getById(String id) throws Exception {
		List<KpiSyncMapping> list = getByProperty("_id", id);
		if (list.size() == 1) {
			return list.get(0);
		} else if (list.size() > 1) {
			log.eLog("KPI同步映射[" + id + "]不唯一");
			throw new Exception("KPI同步映射[" + id + "]不唯一");
		} else {
			return null;
		}
	}

	public List<KpiSyncMapping> getByProperty(String key, String val)
			throws Exception {
		String match = "select * from KpiSyncMapping where `" + key + "` = '"
				+ val + "'";
		return query(match);
	}

	public void delById(String id) throws Exception {
		String del = "delete from KpiSyncMapping where _id = '" + id + "'";
		MongoConnect.executeUpdate(del);
	}

	public void delAll() throws Exception {
		String del = "delete from KpiSyncMapping";
		MongoConnect.executeUpdate(del);
	}

	public KpiSyncMapping save(KpiSyncMapping kpiMapping) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("insert into KpiSyncMapping(name,cateId,dataSourceId,fieldMap,owner) values(?,?,?,?,?)");
			pstmt.setString(1,
					kpiMapping.getName() == null ? "" : kpiMapping.getName());
			pstmt.setString(
					2,
					kpiMapping.getCateId() == null ? "" : kpiMapping
							.getCateId());
			pstmt.setString(3, kpiMapping.getDataSourceId() == null ? ""
					: kpiMapping.getDataSourceId());
			pstmt.setString(4, kpiMapping.getFieldMap() == null ? "{}"
					: JSONObject.fromObject(kpiMapping.getFieldMap())
							.toString());
			pstmt.setString(5, kpiMapping.getOwner());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		}
		return getByName(kpiMapping.getName());
	}

	public KpiSyncMapping update(KpiSyncMapping kpiMapping) throws Exception {
		try {
			Connection conn = MongoConnect.getConnection();
			PreparedStatement pstmt = conn
					.prepareStatement("update KpiSyncMapping set name=?,cateId=?,dataSourceId=?,fieldMap=? where _id=?");
			pstmt.setString(1,
					kpiMapping.getName() == null ? "" : kpiMapping.getName());
			pstmt.setString(
					2,
					kpiMapping.getCateId() == null ? "" : kpiMapping
							.getCateId());
			pstmt.setString(3, kpiMapping.getDataSourceId() == null ? ""
					: kpiMapping.getDataSourceId());
			pstmt.setString(4, kpiMapping.getFieldMap() == null ? "{}"
					: JSONObject.fromObject(kpiMapping.getFieldMap())
							.toString());
			pstmt.setString(5, kpiMapping.getId());
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		}
		return kpiMapping;
	}

	public boolean existByName(String name) {
		try {
			if (getByName(name) == null)
				return false;
		} catch (Exception e) {
		}
		return true;
	}

	public boolean existById(String id) {
		try {
			if (getById(id) == null)
				return false;
		} catch (Exception e) {
		}
		return true;
	}

	public List<KpiSyncMapping> query(String sql) throws Exception {
		List<KpiSyncMapping> ret = new ArrayList<KpiSyncMapping>();
		try {
			Connection conn = MongoConnect.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String id = rs.getString("_id");
				String name = rs.getString("name");
				String cateId = rs.getString("cateId");
				String dataSourceId = rs.getString("dataSourceId");
				JSONObject fieldMap = null;
				try {
					fieldMap = JSONObject.fromObject(rs.getString("fieldMap"));
				} catch (Exception e) {
					fieldMap = new JSONObject();
				}
				KpiCategory cate = kCateStorage.getById(cateId);
				DataSourcePool dataSource = dsStorage.getById(dataSourceId);
				@SuppressWarnings("unchecked")
				KpiSyncMapping ksm = new KpiSyncMapping(name, cate, dataSource,
						dataSourceId, fieldMap);
				ksm.setId(id);
				ksm.setOwner(rs.getString("owner"));
				ret.add(ksm);
			}
		} catch (Exception e) {
			throw e;
		}
		return ret;
	}
}
