package com.mmdb.service.info;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.mmdb.model.bean.Attribute;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.info.CiInformation;

/**
 * 配置项 数据 - 服务层
 * 
 * @author XIE
 */
public interface ICiInfoService {
	/**
	 * 添加数据
	 * 
	 * @param information
	 *            配置项数据对象
	 * @return
	 * @throws Exception
	 */

	public CiInformation save(CiInformation information) throws Exception;

	/**
	 * [批量]添加数据
	 * 
	 * @param informations
	 *            配置项数据对象
	 * @throws Exception
	 */

	public void save(List<CiInformation> informations) throws Exception;

	/**
	 * 通过键值对获取数据
	 * 
	 * @param key
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> getByProperty(String key, Object value)
			throws Exception;

	/**
	 * 通过id获取数据
	 * 
	 * 
	 * 
	 * @param cateId
	 *            分类id
	 * @param infoId
	 *            数据id
	 * @return
	 * @throws Exception
	 */
	public CiInformation getById(String cateId, String infoId) throws Exception;

	/**
	 * 通过jsonId获取数据
	 * 
	 * @param jsonId
	 *            ciid+id
	 * @return
	 * @throws Exception
	 */
	public CiInformation getById(String jsonId) throws Exception;

	/**
	 * 高级查询
	 * 
	 * @param category
	 *            分类
	 * @param must
	 *            且条件
	 * @param or
	 *            或条件
	 * @param extend
	 *            是否继承 true:继承
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> qureyByAdvanced(CiCategory category,
			Map<String, String> must, Map<String, String> or, boolean extend, String username, int start, int limit)
			throws Exception;

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
	public List<CiInformation> qureyByTerm(Map<String, String> must,
			Map<String, String> mustNot) throws Exception;

	/**
	 * 模糊查询
	 * 
	 * @param must
	 *            且条件
	 * @param or
	 *            或条件
	 * @return
	 * @throws Exception
	 */
	public List<CiInformation> qureyByFuzzy(Map<String, String> must,
			Map<String, String> or) throws Exception;

	/**
	 * 删除数据
	 * 
	 * @param info
	 *            数据
	 * @throws Exception
	 */

	public void delete(CiInformation info) throws Exception;

	/**
	 * [批量]删除数据
	 * 
	 * @param infos
	 *            数据数组
	 * @throws Exception
	 */

	public void delete(List<CiInformation> infos) throws Exception;

	/**
	 * 该方法用于删除dcv来的数据,
	 * 
	 * @param datas
	 *            [{id:"",scid:""}]
	 * @throws Exception
	 */
	public int delete(CiCategory category, JSONArray datas) throws Exception;

	public void deleteByJsonIds(List<String> jsonIds) throws Exception;

	/**
	 * 清理所有ci数据和ci上的关系
	 * 
	 * @throws Exception
	 */

	public void clearAll() throws Exception;

	/**
	 * 更新数据
	 * 
	 * @param info
	 *            原始数据
	 * @param data
	 *            要更新的数据
	 * @param source
	 *            操作数据源
	 * @return
	 * @throws Exception
	 */

	public CiInformation update(CiInformation info, Map<String, Object> data,
			String source) throws Exception;

	/**
	 * [批量]添加或更新数据
	 * 
	 * @param category
	 *            分类
	 * @param infos
	 *            CI数组
	 * @return
	 * @throws Exception
	 */

	public Map<String, Long> saveOrUpdate(CiCategory category,
			List<CiInformation> infos) throws Exception;

	/**
	 * 通过一个Category删除这个category 下的全部的Ci
	 * 
	 * @param category
	 *            应该是一个现查的category因为没有事务...
	 * @throws Exception
	 */
	public void deleteCiByCategory(CiCategory category) throws Exception;

	/**
	 * 修改一个分类下全部ci的一个属性
	 * 
	 * @param ciCate
	 * @param data
	 *            key为老的属性名称, value为新的属性
	 * @throws Exception
	 */
	public void alterAttr(CiCategory ciCate, Map<String, Attribute> data)
			throws Exception;

	/**
	 * 删除一个分类下的全部ci的一个属性
	 * 
	 * @param ciCate
	 *            分類
	 * @param attrs
	 *            要删的属性
	 * @throws Exception
	 */
	public void deleteAttr(CiCategory ciCate, List<Attribute> attrs)
			throws Exception;

	public void addAttr(CiCategory ciCate, List<Attribute> attrs)
			throws Exception;

	/**
	 * 根据一组解密后的id获取到对应的CiInfo
	 * 
	 * @param jsonid
	 * @return
	 */
	public List<CiInformation> getByIds(List<String> jsonid);

	public List<CiInformation> getByCategory(CiCategory cate) throws Exception;

	/**
	 * 获取全部的Ci
	 * 
	 * @return
	 */
	public List<CiInformation> getAll() throws Exception;

	public void updateInfos(List<CiInformation> infos) throws Exception;

	/**
	 * 通过Where条件查询指定的CI
	 * 
	 * @param whereParam
	 *            查询语句
	 * @return List<CiInformation>
	 * @throws Exception
	 */
	public List<CiInformation> qureyByWhereSQL(String whereParam)
			throws Exception;
	/**
	 * 查询,qureyByAdvanced是模糊匹配(like)的.而这个方法是精确匹配的
	 * @param category
	 * @param must
	 * @param or
	 * @param extend
	 * @param username
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> qureyByAdvancedEQ(CiCategory category,
			Map<String, String> must, Map<String, String> or, boolean extend,
			String username, int start, int limit) throws Exception;
}
