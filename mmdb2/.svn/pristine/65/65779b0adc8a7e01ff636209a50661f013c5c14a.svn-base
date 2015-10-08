package com.mmdb.model.categroy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mmdb.core.framework.neo4j.annotation.Space;
import com.mmdb.model.bean.Attribute;

/**
 * 命名空间为[relCate]关系分类<br>
 * 
 * @author XIE
 */
@Space("relCate")
public class RelCategory {
	private static final long serialVersionUID = 1L;
	/**
	 * 关系分类id（当前分类中唯一,目前是自动生成的）
	 */
	private String id;
	/**
	 * 关系关系名称（可以重复）
	 */
	private String name;
	/**
	 * 分类的图片
	 */
	private String image;

	/**
	 * 关系继承的父类
	 */
	private RelCategory parent;
	/**
	 * 类型分类的子类
	 */
	private List<RelCategory> children = new ArrayList<RelCategory>();

	/**
	 * 类型分类的属性
	 */
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * 分类的主键
	 */
	private Attribute ownMajor;

	/**
	 * 关系类型 - 区别直接关系还是间接关系,默认是直接关系,indirect是间接关系
	 */
	private String type = "direct";

	private String parentId;
	
	private String owner;
	
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * 默认[构造函数]
	 */
	public RelCategory() {

	}

	/**
	 * 新建关系[根类][构造函数]
	 */
	public RelCategory(String uid, String name) {
		this.id = uid;
		this.name = name;
	}

	/**
	 * 创建关系[子类][构造函数]
	 * 
	 * @param uid
	 *            分类UID
	 * @param name
	 *            分类名称
	 * @param parent
	 *            父类
	 */
	public RelCategory(String uid, String name, RelCategory parent) {
		this.id = uid;
		this.name = name;
		this.parent = parent;
	}

	/**
	 * 获取分类的[UID]
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置分类的[UID]
	 * 
	 * @param id
	 *            分类UID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取关系分类[名称]
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 设置关系分类[名称]
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取分类分类[对应图标]
	 * 
	 * @return
	 */
	public String getImage() {
		return image;
	}

	/**
	 * 设置关系分类[对应图标]
	 * 
	 * @param image
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * 获取关系分类[父类]
	 * 
	 * @return
	 */
	public RelCategory getParent() {
		return parent;
	}

	/**
	 * 设置关系分类[父类]
	 * 
	 * @param parent
	 */
	public void setParent(RelCategory parent) {
		this.parent = parent;
	}

	/**
	 * 获取关系分类的[子类]
	 * 
	 * @return
	 */
	public List<RelCategory> getChildren() {
		return children;
	}

	/**
	 * 设置关系分类的[子类]
	 * 
	 * @param children
	 */
	public void setChildren(List<RelCategory> children) {
		this.children = children;
	}

	/**
	 * 获取分类的[主键]-属性
	 * 
	 * @return
	 */
	public Attribute getOwnMajor() {
		return ownMajor;
	}

	/**
	 * 设置分类的[主键]-属性
	 * 
	 * @param ownMajor
	 *            主键-属性
	 */
	public void setOwnMajor(Attribute ownMajor) {
		this.ownMajor = ownMajor;
	}

	/**
	 * 获取关系分类的[属性]
	 * 
	 * @return
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * 设置关系分类的[属性]
	 * 
	 * @param attributes
	 *            属性数组
	 */
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * 获取关系分类的[所有子类]
	 * 
	 * @return
	 */
	public List<RelCategory> getAllChildren() {
		List<RelCategory> children = this.getChildren();
		List<RelCategory> ret = new ArrayList<RelCategory>();
		ret.addAll(children);
		for (RelCategory child : children) {
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}

	/**
	 * 获取关系分类的所有[属性](包括继承)
	 * 
	 * @return
	 */
	public List<Attribute> getAllAttributes() {
		List<Attribute> attrs = this.getExtendAttrs();
		List<Attribute> ret = this.getAttributes();
		for (Attribute attr : ret) {
			if (!attrs.contains(attr)) {
				attrs.add(attr);
			}
		}
		return attrs;
	}

	/**
	 * 获取关系分类的所有[属性](不包括继承)
	 * 
	 * @return
	 */
	public List<Attribute> getSelfAttributes() {
		List<Attribute> attrs = this.getExtendAttrs();
		List<Attribute> ret = this.getAttributes();
		for (Attribute attr : attrs) {
			if (ret.contains(attr)) {
				ret.remove(attr);
			}
		}
		return ret;
	}

	/**
	 * 通过属性名称获取[属性](包括继承的属性)
	 * 
	 * @param name
	 *            属性名
	 * @return
	 */
	public Attribute getAttributeByName(String name) {
		List<Attribute> attrs = this.getAllAttributes();
		Attribute ret = null;
		for (Attribute a : attrs) {
			if (name.equals(a.getName())) {
				ret = a;
				break;
			}
		}
		return ret;
	}

	/**
	 * 通过属性名称获取[属性](不包括继承的属性)
	 * 
	 * @param name
	 *            属性名
	 * @return
	 */
	public Attribute getSelfAttributeByName(String name) {
		List<Attribute> attrs = this.getSelfAttributes();
		Attribute ret = null;
		for (Attribute a : attrs) {
			if (name.equals(a.getName())) {
				ret = a;
				break;
			}
		}
		return ret;
	}

	/**
	 * 获取从父类继承来的[属性]
	 * 
	 * @return
	 */
	public List<Attribute> getExtendAttrs() {
		List<Attribute> extendAttrs = new ArrayList<Attribute>();
		RelCategory parent = this.getParent();
		while (parent != null) {
			List<Attribute> aList = parent.getAttributes();
			for (Attribute a : aList) {
				if (!extendAttrs.contains(a)) {
					extendAttrs.add(a);
				}
			}
			parent = parent.getParent();
		}
		return extendAttrs;
	}

	/**
	 * 获取分类的[主键属性](如果父类不存在，获取父类的主键)
	 * 
	 * @return
	 */
	public Attribute getMajor() {
		Attribute a = this.getOwnMajor();
		if (a == null && this.getParent() != null) {
			a = this.getParent().getMajor();
		}
		return a;
	}

	/**
	 * 将分类对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		RelCategory parent = null;
		List<RelCategory> children = null;
		parent = this.getParent();
		children = this.getChildren();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("image", this.getImage());
		map.put("type", this.getType());
		Attribute major = this.getMajor();
		Attribute pmajor = parent != null ? parent.getMajor() : null;
		map.put("parentMajor", pmajor == null ? null : pmajor.asMap());
		map.put("major", major == null ? null : major.asMap());
		map.put("ownMajor", this.getOwnMajor() == null ? null : this
				.getOwnMajor().asMap());
		map.put("parent", parent != null ? parent.getId() : null);
		map.put("parentName", parent != null ? parent.getName() : null);
		map.put("children", children != null ? csAsList(children) : null);
		map.put("selfAttributes", attrsAsList(this.getSelfAttributes()));
		map.put("attributes", attrsAsList(this.getAllAttributes()));
		map.put("extendAttributes", attrsAsList(this.getExtendAttrs()));
		map.put("owner", owner==null?"":owner);
		return map;
	}

	public Map<String, Object> asMapForRest() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("text", this.getName());
		map.put("parent", parent != null ? parent.getId() : "#");
		map.put("attributes", attrsAsList(this.getAttributes()));
		map.put("owner", owner==null?"":owner);
		return map;
	}

	/**
	 * 将关系分类数组转换成[UID数组]
	 * 
	 * @param cs
	 *            分类数组
	 * @return
	 */
	private List<String> csAsList(List<RelCategory> cs) {
		List<String> list = new ArrayList<String>();
		for (RelCategory c : cs) {
			list.add(c.getId());
		}
		return list;
	}

	/**
	 * 将属性数组转换成[属性名数组]
	 * 
	 * @param attrs
	 *            分类属性数组
	 * @return
	 */
	private List<Map<String, Object>> attrsAsList(List<Attribute> attrs) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Attribute a : attrs) {
			list.add(a.asMap());
		}
		return list;
	}

	/**
	 * 将属性数组转换成[属性名称数组]
	 * 
	 * @return
	 */
	public List<String> getAttributeNames() {
		List<Attribute> attrs = this.getAllAttributes();
		List<String> list = new ArrayList<String>();
		for (Attribute a : attrs) {
			list.add(a.getName());
		}
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		RelCategory other = (RelCategory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
