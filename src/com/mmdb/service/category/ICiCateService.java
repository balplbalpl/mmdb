package com.mmdb.service.category;

import java.util.List;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.InCiCateMap;

/**
 * 配置项分类服务层
 * 
 * @author XIE
 */
public interface ICiCateService {
	/**
	 * 添加配置项分类
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @return
	 */
	//
	public CiCategory save(CiCategory nCategory) throws Exception;

	/**
	 * 更新配置项分类
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @throws Exception
	 */
	//
	public CiCategory update(CiCategory nCategory) throws Exception;

	/**
	 * 更新配置项分类和下面的数据
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @param infos
	 *            CI数据
	 * @param ims
	 *            内部分类映射
	 * @return
	 * @throws Exception
	 */
	//
	public CiCategory update(CiCategory nCategory, List<CiInformation> infos,
			List<InCiCateMap> ims) throws Exception;

	/**
	 * 添加分类的属性改成直接调用cypher批量更新 2015-05-21
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @param oldAttr
	 *            原来的属性
	 * @param newAttr
	 *            新的属性
	 * @param ims
	 *            内部分类映射
	 * */
	public CiCategory updateAndeditAttr(CiCategory nCategory, String oldAttr,
			String newAttr, List<InCiCateMap> ims) throws Exception;

	/**
	 * 更新分类属性改成直接调用cypher批量更新 2015-05-21
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @param attr
	 *            要增加的属性
	 * @param defaultval
	 *            默认值（""也可以）
	 * */
	public CiCategory updateAndAddAttr(CiCategory nCategory, String attr,
			String defaultVal) throws Exception;

	/**
	 * 更新分类属性改成直接调用cypher批量更新 2015-05-21
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @param attr
	 *            要删除的属性
	 * */
	public CiCategory updateAndDelAttr(CiCategory nCategory, String attr)
			throws Exception;

	/**
	 * 删除配置项分类
	 * 
	 * @param nCategory
	 *            配置项分类
	 * @param scene
	 *            删除ci的时候需要scene参数,没有则为null,全部删除
	 * @param bool
	 *            true会删除分类下的数据
	 * @return
	 * @throws Exception
	 */
	public long delete(CiCategory nCategory, boolean bool) throws Exception;

	/**
	 * 删除分类 & 数据 & 数据上的关系 (数据初始化使用)
	 * 
	 * @return
	 * @throws Exception
	 */
	public void clearAll() throws Exception;

	/**
	 * 根据id获取配置项分类
	 * 
	 * @param id
	 *            分类id(当前分类中唯一)
	 * @return
	 */
	public CiCategory getById(String id) throws Exception;

	public CiCategory getByName(String name) throws Exception;

	/**
	 * 获取分类下的数据
	 * 
	 * @param cateId
	 *            分类ID
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> getByCategory(String cateId) throws Exception;

	/**
	 * 获取所有配置项分类
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<CiCategory> getAll() throws Exception;

	/**
	 * 获取所有配置项分类使用的图标
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getUseImages() throws Exception;

}
