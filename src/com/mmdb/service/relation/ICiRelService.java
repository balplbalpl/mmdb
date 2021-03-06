package com.mmdb.service.relation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.relation.CiRelation;

/**
 * CI数据关系 - 服务层
 * 
 * @author XIE
 */
public interface ICiRelService {
	/**
	 * 获取所有的关系
	 * 
	 * @return
	 * @throws Exception
	 */
	List<CiRelation> getAll() throws Exception;

	/**
	 * 获取单个数据关系
	 * 
	 * @param ciRelId
	 *            关系ID
	 * @return
	 * @throws Exception
	 */
	CiRelation getById(String ciRelId) throws Exception;

	/**
	 * 获取单个数据关系
	 * 
	 * @param rNeoId
	 *            关系Neo4jID
	 * @return
	 * @throws Exception
	 */
	CiRelation getById(Long rNeoId) throws Exception;

	/**
	 * 保存数据关系
	 * 
	 * @param ciRelation
	 *            关系
	 * @throws Exception
	 */

	CiRelation save(CiRelation ciRelation) throws Exception;

	/**
	 * 批量保存数据关系
	 * 
	 * @param ciRelations
	 *            关系数组
	 * @throws Exception
	 */

	void save(List<CiRelation> ciRelations) throws Exception;

	/**
	 * 删除关系
	 * 
	 * @param ciRelation
	 *            关系
	 * @throws Exception
	 */

	void delete(CiRelation ciRelation) throws Exception;

	/**
	 * 批量删除关系
	 * 
	 * @param ciRelations
	 *            数据关系数组
	 * @throws Exception
	 */

	void delete(List<CiRelation> ciRelations) throws Exception;

	/**
	 * 根据关系分类批量删除关系
	 * 
	 * @param rCategory
	 *            关系分类
	 * @throws Exception
	 */
	void delete(RelCategory rCategory) throws Exception;

	/**
	 * 删除数据关系(数据初始化使用)
	 * 
	 * @throws Exception
	 */

	void deleteAll() throws Exception;

	/**
	 * 更新关系
	 * 
	 * @param ciRelation
	 *            关系
	 * @throws Exception
	 */

	CiRelation update(CiRelation ciRelation) throws Exception;

	/**
	 * 批量更新数据关系
	 * 
	 * @param ciRelations
	 *            关系数组
	 * @throws Exception
	 */

	void update(List<CiRelation> ciRelations) throws Exception;

	/**
	 * 更新数据关系
	 * 
	 * @param rNeoId
	 *            旧的关系id
	 * @param ciRelation
	 *            关系
	 * @throws Exception
	 */

	CiRelation update(Long rNeoId, CiRelation ciRelation) throws Exception;

	/**
	 * 查询CI相关的关系.
	 * 
	 * @param cate
	 *            指定具体的关系
	 * @param must
	 *            必要满足的条件
	 * @param or
	 *            满足必要条件后,增加的条件 (age=6) and (name='x' or name='y') 两个name就是or条件
	 * @param extend
	 *            是否继承
	 * @return
	 * @throws Exception
	 */
	List<CiRelation> qureyByAdvanced(RelCategory cate,
			Map<String, String> must, Map<String, String> or, boolean extend)
			throws Exception;

	/**
	 * 查询一组ci之间的关系,不在这组内的ci不查询
	 * 
	 * @param ciIds
	 *            这组ci
	 * @return 返回的ciRelation是lazy没有去查询具体的开始CI和结束CI
	 */
	List<Map<String, Object>> queryCiInRel(List<String> ciIds) throws Exception;

	/**
	 * 高级查询
	 * 
	 * @param must
	 *            且条件成立
	 * @param mustNot
	 *            且条件不成立
	 * @return
	 * @throws Exception
	 */
	List<CiRelation> qureyByTerm(Map<String, String> must,
			Map<String, String> mustNot) throws Exception;

	/**
	 * 查询某个CI身上的关系
	 * 
	 * @param info
	 *            CI数据
	 * @param rships
	 *            关系分类
	 * @return
	 * @throws Exception
	 */
	List<CiRelation> qureyCiRelation(CiInformation info, List<String> rships)
			throws Exception;

	/**
	 * 查询某个CI身上的关系
	 * 
	 * @param info
	 *            CI数据
	 * @param rships
	 *            关系分类
	 * @return
	 * @throws Exception
	 */
	List<CiRelation> qureyCiRelationWithCiCate(CiInformation info,
			List<String> rships, List<CiCategory> all) throws Exception;

//	/**
//	 * 查询与CI指定关系的CI
//	 * 
//	 * @param info
//	 *            CI数据
//	 * @param rship
//	 *            关系分类
//	 * @return
//	 * @throws Exception
//	 */
//	List<Long> qureyCiByRelation(CiInformation info, String rship)
//			throws Exception;

	/**
	 * 查询某个CI身上的关系
	 * 
	 * @param info
	 *            ci数据
	 * @param rships
	 *            关系分类
	 * @param jsonIds
	 *            cis数据
	 * @return
	 * @throws Exception
	 */
	Set<String> qureyCiRelation(CiInformation info, List<String> rships,
			List<String> jsonIds) throws Exception;

	/**
	 * 查询某个CI身上的关系
	 * 
	 * @param info
	 *            ci数据
	 * @param rships
	 *            关系分类
	 * @param jsonIds
	 *            cis数据
	 * @return
	 * @throws Exception
	 */
	Set<String> qureyCiRelationWithCiCate(CiInformation info,
			List<String> rships, List<String> jsonIds, List<CiCategory> all)
			throws Exception;

	/**
	 * [批量]添加或更新关系数据
	 * 
	 * @param cate
	 *            关系分类
	 * @param rels
	 *            CI关系数组
	 * @return
	 * @throws Exception
	 */
	Map<String, Long> saveOrUpdate(RelCategory cate, Set<CiRelation> rels)
			throws Exception;

	/**
	 * 查询节点相关的所有节点
	 * 
	 * @param info
	 *            数据
	 * @param rts
	 *            关系类型
	 * @param termMap
	 *            过滤条件
	 * @param dirDepth
	 *            方向/层深
	 * @return
	 * @throws Exception
	 */
	Set<CiInformation> newTraversal(CiInformation info, List<String> rts,
			Map<String, Map<String, String>> termMap,
			Map<String, String> dirDepth) throws Exception;

}
