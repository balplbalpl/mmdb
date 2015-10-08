package com.mmdb.model.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mmdb.model.categroy.CiCategory;
import com.mmdb.model.categroy.RelCategory;

/**
 * 命名空间为[InCiCateRelMap]内部配置项分类的映射<br>
 * 
 * @author XIE
 */
public class InCiCateMap {
	/**
	 * 关系映射名称(不可重复)
	 */

	private String id;
	
	private String name;
	/**
	 * 关系分类
	 */
	// @RelationTo(elementClass = RelCategory.class, type =
	// "inCiCateMap-RelCategroy", direction = Direction.INCOMING)
	private RelCategory relCate;

	private String relCateId;

	/**
	 * 关系分类的属性对应的值
	 */
	private Map<String, Object> relValue = new HashMap<String, Object>();
	/**
	 * 配置项分类 - 起点
	 */
	// @RelationTo(elementClass = CiCategory.class, type =
	// "S-inCiCateMap-CiCategory", direction = Direction.INCOMING)
	private CiCategory startCate;
	/**
	 * 配置项分类ID - 起点
	 */
	private String startCateId;
	/**
	 * 配置项分类字段 - 起点
	 */
	private String startCateField;
	/**
	 * 配置项分类 - 终点
	 */
	// @RelationTo(elementClass = CiCategory.class, type =
	// "E-inCiCateMap-CiCategory", direction = Direction.INCOMING)
	private CiCategory endCate;
	/**
	 * 配置项分类ID - 终点
	 */
	private String endCateId;
	/**
	 * 配置项分类字段 - 终点
	 */
	private String endCateField;
	
	//创建人
	private String owner;

	public String getRelCateId() {
		return relCateId;
	}

	public void setRelCateId(String relCateId) {
		this.relCateId = relCateId;
	}

	/**
	 * [构造函数]
	 */
	public InCiCateMap() {
	}

	/**
	 * 添加映射[构造函数]
	 * 
	 * @param name
	 *            映射名称
	 * @param rCate
	 *            映射的关系类型
	 * @param sCate
	 *            配置项分类起点
	 * @param eCate
	 *            配置项分类终点
	 * @param sField
	 *            配置项起点字段
	 * @param eField
	 *            配置项终点字段
	 * @throws Exception
	 */
	public InCiCateMap(String name, RelCategory rCate,
			Map<String, Object> relValue, CiCategory sCate, CiCategory eCate,
			String sField, String eField) throws Exception {
		this.name = name;
		this.relCate = rCate;
		this.relValue = this.paddingRelValue(relValue);
		this.startCate = sCate;
		this.startCateId = sCate.getId();
		this.endCate = eCate;
		this.endCateId = eCate.getId();
		this.startCateField = sField;
		this.endCateField = eField;
	}

	/**
	 * 获取关系映射名称(不可重复)
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 设置映射的名称
	 * 
	 * @param name
	 *            关系映射名称(不可重复)
	 * @throws Exception
	 */
	public void setName(String name) throws Exception {
		if (name == null || "".equals(name)) {
			throw new Exception("映射名称不能为空");
		}
		this.name = name;
	}

	/**
	 * 获取映射的关系类型
	 * 
	 * @return
	 */
	public RelCategory getRelCate() {
		return this.relCate;
	}

	/**
	 * 设置映射的关系类型
	 * 
	 * @param relCate
	 *            关系分类
	 * @throws Exception
	 */
	public void setRelCate(RelCategory relCate) throws Exception {
		if (relCate == null || relCate.getId() == null) {
			throw new Exception("关系类型为空");
		}
		this.relCate = relCate;
	}

	/**
	 * 获取关系分类的属性对应的值
	 * 
	 * @return
	 */
	public Map<String, Object> getRelValue() {
		return relValue;
	}

	/**
	 * 设置关系分类的属性对应的值
	 * 
	 * @param relValue
	 */
	public void setRelValue(Map<String, Object> relValue) {
		this.relValue = relValue;
	}

	/**
	 * 获取配置项分类 - 起点
	 * 
	 * @return
	 */
	public CiCategory getStartCate() {
		return this.startCate;
	}

	/**
	 * 设置配置项分类 - 起点
	 * 
	 * @param sCate
	 *            配置项分类
	 * @throws Exception
	 */
	public void setStartCate(CiCategory sCate) throws Exception {
		if (sCate == null || sCate.getId() == null) {
			throw new Exception("缺少关系分类起点");
		}
		this.startCate = sCate;
		this.startCateId = sCate.getId();
	}

	/**
	 * 获取配置项分类ID - 起点
	 * 
	 * @return
	 */
	public String getStartCateId() {
		return startCateId;
	}

	/**
	 * 设置配置项分类ID - 起点
	 * 
	 * @param startCateId
	 */
	public void setStartCateId(String startCateId) {
		this.startCateId = startCateId;
	}

	/**
	 * 获取配置项分类 -起点- [字段]
	 * 
	 * @return
	 */
	public String getStartCateField() {
		return this.startCateField;
	}

	/**
	 * 设置配置项分类 -起点- [字段]
	 * 
	 * @param field
	 *            配置项分类字段
	 * @throws Exception
	 */
	public void setStartCateField(String field) throws Exception {
		if (field == null || "".equals(field)) {
			throw new Exception("缺少关系分类起点字段");
		}
		this.startCateField = field;
	}

	/**
	 * 获取配置项 -终点- [分类 ]
	 * 
	 * @return
	 */
	public CiCategory getEndCate() {
		return this.endCate;
	}

	/**
	 * 设置配置项 - 终点 - [分类]
	 * 
	 * @param eCate
	 *            配置项分类
	 * @throws Exception
	 */
	public void setEndCate(CiCategory eCate) throws Exception {
		if (eCate == null || eCate.getId() == null) {
			throw new Exception("缺少关系分类终点");
		}
		this.endCate = eCate;
		this.endCateId = eCate.getId();
	}

	/**
	 * 获取配置项分类ID - 终点
	 * 
	 * @return
	 */
	public String getEndCateId() {
		return endCateId;
	}

	/**
	 * 设置配置项分类ID - 终点
	 * 
	 * @param endCateId
	 */
	public void setEndCateId(String endCateId) {
		this.endCateId = endCateId;
	}

	/**
	 * 获取配置项分类 -终点- [字段]
	 * 
	 * @return
	 */
	public String getEndCateField() {
		return this.endCateField;
	}

	/**
	 * 设置配置项分类 -终点- [字段]
	 * 
	 * @param field
	 * @throws Exception
	 */
	public void setEndCateField(String field) throws Exception {
		if (field == null || "".equals(field)) {
			throw new Exception("缺少关系终点分类字段");
		}
		this.endCateField = field;
	}

	/**
	 * 将关系映射对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("映射ID", this.getId());
		ret.put("映射名称", this.getName());

		ret.put("关系ID", this.getRelCate().getId());
		ret.put("关系名称", this.getRelCate().getName());
		ret.put("关系属性值", this.getRelValue());

		ret.put("起点分类名称", this.getStartCate().getName());
		ret.put("起点分类ID", this.getStartCate().getId());
		ret.put("起点分类字段", this.getStartCateField());

		ret.put("终点分类名称", this.getEndCate().getName());
		ret.put("终点分类ID", this.getEndCate().getId());
		ret.put("终点分类字段", this.getEndCateField());
		
		ret.put("所有者", this.owner);

		return ret;
	}

	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * 处理关系属性空值
	 * 
	 * @param relValue
	 * @return
	 */
	public Map<String, Object> paddingRelValue(Map<String, Object> relValue) {
		if (relValue == null) {
			relValue = new HashMap<String, Object>();
		}
		List<String> attrs = this.relCate.getAttributeNames();
		Map<String, Object> relVal = new HashMap<String, Object>();
		for (String a : attrs) {
			Object obj = null;
			if (relValue.containsKey(a)) {
				obj = relValue.get(a);
				if (obj == null || obj.equals("")) {
					obj = this.relCate.getAttributeByName(a).getDefaultValue();
				}
			} else {
				obj = this.relCate.getAttributeByName(a).getDefaultValue();
			}
			relVal.put(a, obj);
		}
		return relVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InCiCateMap other = (InCiCateMap) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
