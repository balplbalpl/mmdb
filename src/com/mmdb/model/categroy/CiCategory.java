package com.mmdb.model.categroy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mmdb.common.Global;
import com.mmdb.model.bean.Attribute;

/**
 * 命名空间为[ciCategory]的配置项分类<br>
 * <p 1.全部分类的名称都不能相同>
 * <p 2.id是mongo的id,唯一的>
 * 
 * @author XIE
 */
public class CiCategory {
	private static final long serialVersionUID = 1L;
	/**
	 * 分类id（当前分类中唯一,目前是自动生成的）
	 */
	private String id;
	/**
	 * 分类名称（可以重复）
	 */
	private String name;
	/**
	 * 分类的图片（可以重复）
	 */
	private String image;
	/**
	 * 分类继承的父类
	 */
	private CiCategory parent;
	/**
	 * 分类的子类
	 */
	private List<CiCategory> children = new ArrayList<CiCategory>();

	/**
	 * 分类的属性
	 */
	private List<Attribute> attributes = new ArrayList<Attribute>();

	/**
	 * 分类的主键
	 */
	private Attribute ownMajor;

	/**
	 * 客户端显示id字段
	 */
	private Attribute clientId;

	private String parentId;

	private String owner;
	
	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * [构造函数]
	 */
	public CiCategory() {

	}

	/**
	 * 创建分类的[根类]
	 * 
	 * @param uid
	 *            分类UID
	 * @param name
	 *            分类名称
	 */
	public CiCategory(String uid, String name) {
		this.id = uid;
		this.name = name;
	}

	/**
	 * 创建分类[子类]
	 * 
	 * @param uid
	 *            分类UID
	 * @param name
	 *            分类名称
	 * @param parent
	 *            父类
	 */
	public CiCategory(String uid, String name, CiCategory parent) {
		this.id = uid;
		this.name = name;
		this.parent = parent;
		if(parent!=null){
			parentId = parent.getId();
		}
	}

	/**
	 * 获取分类的[主键]-属性
	 * 
	 * @return Attribute
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
	 * 获取[父类]
	 * 
	 * @return CiCategory
	 */
	public CiCategory getParent() {
		return parent;
	}

	/**
	 * 获取分类的[属性]
	 * 
	 * @return List<Attribute>
	 */
	public List<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * 获取分类的所有[属性](包括继承)
	 * 
	 * @return List<Attribute>
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
	 * 获取分类的所有[属性](不包括继承)
	 * 
	 * @return List<Attribute>
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
	 * 设置分类的[属性]
	 * 
	 * @param attributes
	 *            属性数组
	 */
	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * 设置分类的[父类]
	 * 
	 * @param parent
	 *            分类-父类
	 */
	public void setParent(CiCategory parent) {
		this.parent = parent;
	}

	/**
	 * 获取分类的[子类]
	 * 
	 * @return List<CiCategory>
	 */
	public List<CiCategory> getChildren() {
		return children;
	}

	/**
	 * 获取分类的[所有子类]
	 * 
	 * @return List<CiCategory>
	 */
	public List<CiCategory> getAllChildren() {
		List<CiCategory> children = this.getChildren();
		List<CiCategory> ret = new ArrayList<CiCategory>();
		ret.addAll(children);
		for (CiCategory child : children) {
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}

	/**
	 * 设置分类的[子类]
	 * 
	 * @param children
	 */
	public void setChildren(List<CiCategory> children) {
		this.children = children;
	}

	/**
	 * 获取分类[名称]
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置分类[名称]
	 * 
	 * @param name
	 *            分类名称
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 获取分类的[UID]
	 * 
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置分类的[UID]
	 * 
	 * @param uid
	 *            分类UID
	 */
	public void setId(String uid) {
		this.id = uid;
	}

	/**
	 * 获取分类[对应图标]
	 * 
	 * @return String
	 */
	public String getImage() {
		return image;
	}

	/**
	 * 设置分类[对应图标]
	 * 
	 * @param image
	 */
	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * 获取分类的[主键属性](如果父类不存在，获取父类的主键)
	 * 
	 * @return Attribute
	 */
	public Attribute getMajor() {
		Attribute t = ownMajor;
		CiCategory parent;
		while (t == null) {
			parent = this.getParent();
			t = parent.getMajor();
		}
		return t;
	}

	/**
	 * 获取客户端显示id
	 * 
	 * @return Attribute
	 */
	public Attribute getClientId() {
		return clientId;
	}

	/**
	 * 设置客户端显示id
	 * 
	 * @param clientId
	 */
	public void setClientId(Attribute clientId) {
		this.clientId = clientId;
	}

	/**
	 * 通过属性名称获取[属性](包括继承的属性)
	 * 
	 * @param name
	 *            属性名
	 * @return Attribute
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
	 * @return Attribute
	 */
	public Attribute getSelfAttributeByName(String name) {
		List<Attribute> attrs = this.getAttributes();
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
	 * @return List<Attribute>
	 */
	public List<Attribute> getExtendAttrs() {
		List<Attribute> extendAttrs = new ArrayList<Attribute>();
		CiCategory parent = this.getParent();
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
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * 将分类对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMapForRest() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("text", this.getName());
		map.put("icon", "resource/svg/" + Global.svgBaseTheme + "/" +this.getImage());
		Attribute clientId = this.getClientId();
		map.put("clientId", clientId == null ? null : clientId.getName());
		map.put("parent", parent == null ? null : parent.getId());
		map.put("attributes", attrsAsList(this.getAttributes()));
		map.put("allattributes", attrsAsList(getAllAttributes()));
		map.put("major", getMajor().getName());
		map.put("owner", owner);
		return map;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		CiCategory parent = this.getParent();
		List<CiCategory> children = this.getChildren();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("image", this.getImage());
		Attribute clientId = this.getClientId();
		map.put("clientId", clientId == null ? null : clientId.getName());
		Attribute major = this.getMajor();
		Attribute pmajor = parent != null ? parent.getMajor() : null;
		map.put("parentMajor", pmajor == null ? null : pmajor.asMap());
		map.put("major", major == null ? null : major.asMap());
		map.put("ownMajor", this.getOwnMajor() == null ? null : this
				.getOwnMajor().asMap());
		map.put("parent", parent != null ? parent.getId() : null);
		map.put("owner", this.owner);
		map.put("parentName", parent != null ? parent.getName() : null);
		map.put("children", children != null ? csAsList(children) : null);
		map.put("selfAttributes", attrsAsList(this.getAttributes()));
		map.put("attributes", attrsAsList(this.getAllAttributes()));
		map.put("extendAttributes", attrsAsList(this.getExtendAttrs()));
		return map;
	}

	/**
	 * 将分类数组转换成[UID数组]
	 * 
	 * @param cs
	 *            分类数组
	 * @return List<String>
	 */
	private List<String> csAsList(List<CiCategory> cs) {
		List<String> list = new ArrayList<String>();
		for (CiCategory c : cs) {
			list.add(c.getId());
		}
		return list;
	}

	/**
	 * 将属性数组转换成[属性数组]
	 * 
	 * @param attrs
	 *            分类属性数组
	 * @return List<Map<String, Object>>
	 */
	private List<Map<String, Object>> attrsAsList(List<Attribute> attrs) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (Attribute a : attrs) {
			Map<String, Object> asMap = a.asMap();
			if (ownMajor != null) {
				if (a.getName().equals(ownMajor.getName())) {
					asMap.put("major", true);
				}
			}
			list.add(asMap);
		}
		return list;
	}

	/**
	 * 将属性数组转换成[属性名称数组]
	 * 
	 * @return List<String>
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
	public String toString() {
		return "CiCategory [id=" + id + ", name=" + name + ", image=" + image
				+ ", parent=" + parent + ", children=" + children
				+ ", attributes=" + attributes + ", ownMajor=" + ownMajor
				+ ", clientId=" + clientId + ", parentId=" + parentId + "]";
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
		CiCategory other = (CiCategory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
