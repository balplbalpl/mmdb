package com.mmdb.service.mapping;

import java.util.List;
import java.util.Map;

import com.mmdb.model.mapping.KpiSyncMapping;

public interface IKpiSyncMappingService {

	List<KpiSyncMapping> getAll() throws Exception;

	List<KpiSyncMapping> getByOwner(String username) throws Exception;

	KpiSyncMapping getById(String id) throws Exception;

	KpiSyncMapping getByName(String name) throws Exception;

	KpiSyncMapping save(KpiSyncMapping kpiMapping) throws Exception;

	KpiSyncMapping update(KpiSyncMapping kpiMapping) throws Exception;

	void delById(String id) throws Exception;

	void delAll() throws Exception;

	boolean existByName(String name) throws Exception;

	boolean existById(String id) throws Exception;

	/**
	 * 立即运行映射进行取数据建议kpi
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	Map<String, Integer> runNow(String id) throws Exception;

}
