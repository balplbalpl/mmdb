package com.mmdb.service.relation;

import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.info.KpiInformation;

public interface ICiKpiRelService {
	public List<Map<String, String>> getAllCiKpiRel(List<String> cateIds,
			Boolean hasChildren) throws Exception;

	public Map<String,Integer> addCiKpiRel(Map<String, Object> data,
			Map<String, CiCategory> ciCateMap);

	public Integer getCiKpiRelCount() throws Exception;

	public Map<String, CiCategory> getCiCateMap();

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI
	 * 
	 * @param ciId
	 * @return List<Map<String, String>>
	 * @throws Exception
	 */
	public List<Map<String, String>> getRelByCiId(String ciId) throws Exception;

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI的分类信息
	 * 
	 * @param ciId
	 * @return List<KpiCategory>
	 * @throws Exception
	 */
	public List<KpiCategory> getKpiCateByCiId(String ciId) throws Exception;

	/**
	 * 通过CI Id获取到关联此CI的所有的KPI
	 * 
	 * @param ciId
	 * @param kpiCateId
	 * @return List<KpiInformation>
	 * @throws Exception
	 */
	public List<KpiInformation> getKpiByKpiCate(String ciId, String kpiCateId)
			throws Exception;

	/**
	 * 通过ci获取相关联的kpi
	 * 
	 * @param ciHexId
	 *            ci的十六进制id
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getKpiByCi(String ciHexId) throws Exception;

	/**
	 * 通过一组ci获取相关联的kpi
	 * 
	 * @param ciHexId
	 *            ci的十六进制id
	 * @return key为ci的hexId,value是对应的kpi
	 * @throws Exception
	 */
	public Map<String, List<KpiInformation>> getKpiByCi(List<String> ciHexIds)
			throws Exception;

	/**
	 * 手动注册CI KPi关系
	 * 
	 * @param ciId
	 *            ci的十六进制id
	 * @param kpiIds
	 *            kpi的十六进制id列表
	 */
	public void saveCiKpiRel(String ciId, List<String> kpiIds) throws Exception;

	/**
	 * 手动注册CI KPI关系
	 * 
	 * @param ciId
	 *            ci的16进制ID
	 * @param kpiIds
	 *            kpi的16进制ID列表
	 */
	public void saveCiKpiRel(List<String> ciIds, List<String> kpiIds)
			throws Exception;

	/**
	 * 删除CI KPI关系
	 * 
	 * @param ciId
	 *            ci的十六进制id
	 * @param kpiIds
	 *            kpi的十六进制id列表
	 */
	public void delCiKpiRel(String ciId, List<String> kpiIds) throws Exception;

	/**
	 * 删除一个ci上的全部与kpi的关系
	 * 
	 * @param ciId
	 *            ci的十六进制id
	 * @throws Exception
	 */
	public void delCiKpiRelByCiId(String ciId) throws Exception;

	public void delCiKpiRelByKpi(String kpiId) throws Exception;
	
	/**
	 * 通过Ci Category的名称删除ci kpi关系
	 * 
	 * @param ciCategoryName
	 * @throws Exception
	 */
	public void delRelByCiCate(String ciCategoryName) throws Exception;
	
	/**
	 * 通过Kpi Category的名称删除ci kpi关系
	 * @param kpiCategoryName
	 * @throws Exception
	 */
	public void delRelByKpiCate(String kpiCategoryName) throws Exception;
	
	/**
	 * 删除所有的关系
	 * 
	 * @throws Exception
	 */
	public void deletelAll() throws Exception;
	
	/**
	 * 通过kpi 列表批量删除关系
	 * 
	 * @param kpiHexs
	 * @throws Exception
	 */
	public void delRelByKpis(List<String> kpiHexs) throws Exception;
	
	/**
	 * 统计 通过Ci分类名称查询出的关系记录总数
	 * 
	 * @param ciCateName
	 * @return
	 * @throws Exception
	 */
	public int getCountByCiCate(String ciCateName) throws Exception;
	
	/**
	 * 通过关系ID批量删除关系
	 * 
	 * @param relIds
	 * @throws Exception
	 */
	public void delCiKpiRelByIds(List<String> relIds) throws Exception;
	
	/**
	 * 通过ci的hexid获取对应的ci
	 * @param ciHexs
	 * @return
	 * @throws Exception
	 */
	public int getCountByCiHexs(List<String> ciHexs) throws Exception;
	
	/**
	 * 通过Ci的分类获取的关系列表
	 * 
	 * @param ciCategoryName 分类名称
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getRelbyCiCate(String ciCateName,
			int page, int pageSize) throws Exception;
	
	/**
	 * 获取带分页的数据
	 * @param ciHexs
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getCiKpiRelbyCiHexs(List<String> ciHexs,int page,int pageSize)
			throws Exception;
	
	/**
	 * 通过ciHex查询所有的关系
	 * 
	 * @param ciHexs
	 * @param page
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getAllRelbyCiHexs(List<String> ciHexs,
			int page, int pageSize) throws Exception;
}
