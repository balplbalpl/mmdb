package com.mmdb.service.category;

import java.util.List;

import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToRelationMapping;
import com.mmdb.model.relation.CiRelation;

/**
 * 关系分类 - 服务层
 * 
 * @author XIE
 */
public interface IRelCateService {
	/**
	 * 添加关系分类
	 * 
	 * @param rCate
	 *            关系分类
	 * @return
	 */
	public RelCategory save(RelCategory rCate) throws Exception;

	/**
	 * 更新关系分类
	 * 
	 * @param rCate
	 * @throws Exception
	 */
	public RelCategory update(RelCategory rCate) throws Exception;

	/**
	 * 删除关系分类,会删除该分类下的所有数据
	 * 
	 * @param rCate
	 *            关系分类
	 * @throws Exception
	 */
	//
	public void delete(RelCategory rCate) throws Exception;

	/**
	 * 删除所有关系分类(数据初始化使用)
	 * 
	 * @return
	 * @throws Exception
	 */
	//
	public void deleteAll() throws Exception;

	/**
	 * 根据id获取关系分类
	 * 
	 * @param id
	 *            分类id(当前关系中唯一)
	 * @return
	 */
	public RelCategory getById(String id) throws Exception;

	public RelCategory getByName(String name) throws Exception;

	/**
	 * 获取所有关系分类
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<RelCategory> getAll() throws Exception;

	/**
	 * 获取所有关系分类名称
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getCateNames() throws Exception;

	/**
	 * 更新关系分类字段 并更新数据上的字段
	 * 
	 * @param rCate
	 *            关系分类
	 * @param rels
	 *            分类下的数据
	 * @param ims
	 *            使用分类的CI内部映射
	 * @param oms
	 *            使用分类的CI外部映射
	 * @return
	 * @throws Exception
	 */
	//
	public RelCategory update(RelCategory rCate, List<CiRelation> rels,
			List<InCiCateMap> ims, List<SourceToRelationMapping> oms)
			throws Exception;

	/**
	 * 更新关系分类字段 并更新数据上的字段
	 * 
	 * @param rCate
	 *            关系分类
	 * @param rels
	 *            分类下的数据
	 * @param ims
	 *            使用分类的CI内部映射
	 * @param oms
	 *            使用分类的CI外部映射
	 * @return
	 * @throws Exception
	 */
	public RelCategory updateAndEditAttr(RelCategory rCate, String oldAttr,
			String newAttr, List<InCiCateMap> ims,
			List<SourceToRelationMapping> oms) throws Exception;

	public RelCategory updateAndAddAttr(RelCategory rCate, String attr,
			String defaulVal) throws Exception;

	public RelCategory updateAndDelAttr(RelCategory rCate, String attr,
			List<InCiCateMap> ims, List<SourceToRelationMapping> oms)
			throws Exception;

	public void saveHasId(RelCategory category);
}
