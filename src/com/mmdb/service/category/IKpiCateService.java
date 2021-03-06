package com.mmdb.service.category;

import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.KpiCategory;
import com.mmdb.model.info.KpiInformation;

/**
 * KPI分类服务层
 * 
 * @author yuhao.guan
 */
public interface IKpiCateService {
	/**
	 * 添加KPI分类
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public KpiCategory save(KpiCategory nCategory) throws Exception;

	/**
	 * 更新KPI分类
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @throws Exception
	 */
	//
	public KpiCategory update(KpiCategory nCategory) throws Exception;


	/**
	 * 删除KPI分类
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @param scene
	 *            删除ci的时候需要scene参数,没有则为null,全部删除
	 * @param bool
	 *            true会删除分类下的数据
	 * @return
	 * @throws Exception
	 */
	public long delete(KpiCategory nCategory, boolean bool) throws Exception;

	

	/**
	 * 根据id获取KPI分类
	 * 
	 * @param id
	 *            分类id(当前分类中唯一)
	 * @return
	 */
	public KpiCategory getById(String id) throws Exception;

	/**
	 * 获取所有KPI分类
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiCategory> getAll() throws Exception;
	
	/**
	 * 根据名称获取KPI分类
	 * 
	 * @param name
	 *            分类name(当前分类中唯一)
	 * @return
	 */
	public KpiCategory getByName(String name) throws Exception;
	
	/**
	 * 获取某个用户下的所有KPI分类
	 * 
	 * @param userName
	 *				用户登录名 
	 * @return List<KpiCategory>
	 * @throws Exception
	 */
	public List<KpiCategory> getKpiCateByUserName(String userName) throws Exception;
	
	/**
	 * 获取到某个用户的所有KPI
	 * 
	 * @param userName
	 * @return List<KpiInformation>
	 * @throws Exception
	 */
	public List<KpiInformation> getKpiByUserName(String userName) throws Exception;
	
	/**
	 * 删除所有的KPI分类
	 * 
	 * @throws Exception
	 */
	public void deleteAll() throws Exception;

	/**
	 * 获取分类下的数据
	 * 
	 * @param cateId
	 *            分类ID
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getKpiByCategory(String cateId) throws Exception;
	
	/**
	 * 按ID获取KPI
	 * 
	 * @param cateId
	 *            分类ID
	 * @return
	 * @throws Exception
	 */
	public KpiInformation getKpiById(String kpiId) throws Exception;
	
	/**
	 * 按名称获取KPI
	 * 
	 * @param cateId
	 *            分类ID
	 * @param name
	 *            kpi名称          
	 * @return
	 * @throws Exception
	 */
	public KpiInformation getKpiByName(String cateId,String name) throws Exception;
	
	/**
	 * 按HEX获取KPI
	 * 
	 * @param cateId
	 *            分类ID
	 * @return
	 * @throws Exception
	 */
	public KpiInformation getKpiByHex(String hexId) throws Exception;
	
	/**
	 * 通过KPI ID列表查询KPI
	 * 
	 * @param kpiIds
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> getKpiByIds(List<String> kpiIds) throws Exception;
	
	/**
	 * 模糊查询KPI
	 * 
	 * @param cateId
	 *            分类ID
	 * @param param
	 *            模糊查询条件
	 * @param userName
	 *            用户名 （null 表示不需要过滤用户）
	 * @param page
	 *            页数（-1 表示不分页）
	 * @param pageSize
	 *           每页条数                                         
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> find(String cateId, String param, 
			String userName,int page,int pageSize) throws Exception;
	
	
	/**
	 * 统计模糊查询KPI的记录数
	 * 
	 * @param cateId
	 *            分类ID
	 * @param param
	 *            模糊查询条件
	 * @param userName
	 *            用户名 （null 表示不需要过滤用户）
	 * @return
	 * @throws Exception
	 */
	public int countFind(String cateId, String param, String userName)throws Exception ;
	
	/**
	 * 通过SQL查询KPI
	 * 
	 * @param whereParam
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> findBySql(String whereParam) throws Exception;
	
	/**
	 * 查询某个分类下的所有KPI(包含分类下子类的KPI)
	 * 
	 * @param cateId 分类Id
	 * @param param  模糊查询参数
	 * @param userName
	 *            用户名 （null 表示不需要过滤用户）
	 * @param page
	 *            页数（-1 表示不分页）
	 * @param pageSize
	 *           每页条数            
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<KpiInformation> findAllByCate(String cateId, 
				String param,String userName,int page, int pageSize) throws Exception;
	
	/**
	 * 统计某个分类下的所有KPI(包含分类下子类的KPI)的条数
	 * 
	 * @param cateId 分类Id
	 * @param param  模糊查询参数
	 * @param userName 用户登录名
	 * 
	 * @return
	 * @throws Exception
	 */
	public int countFindAllByCate(String cateId, String param,
			String userName) throws Exception;
	
	/**
	 * 添加KPI
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public KpiInformation save(KpiInformation info) throws Exception;
	
	/**
	 * 修改KPI
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public KpiInformation update(KpiInformation info) throws Exception;
	
	/**
	 * 删除KPI
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public void delete(KpiInformation info) throws Exception;
	
	/**
	 * 按分类删除KPI
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public void deleteByCategory(KpiCategory cate) throws Exception;
	
	/**
	 * 删除所有KPI
	 * 
	 * @param nCategory
	 *            KPI分类
	 * @return
	 */
	//
	public void deleteAllKpi() throws Exception;
	
	/**
	 * 通过id批量删除KPI
	 * 
	 * @param ids
	 * @throws Exception
	 */
	public void deleteKpiByIds(List<String> kpiIds) throws Exception;
	
	/**
	 * 批量导入kpi category下kpi
	 * @param category
	 * @param informations
	 * @return
	 * @throws Exception
	 */
	public Map<String, Long> saveOrUpdate(KpiCategory category,
			List<KpiInformation> informations) throws Exception;

	public List<KpiInformation> getAllKpi() throws Exception;
}
