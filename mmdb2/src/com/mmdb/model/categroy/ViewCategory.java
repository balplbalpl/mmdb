package com.mmdb.model.categroy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命名空间为[viewCategory]视图分类<br>
 * 分类相当于文件夹
 * 
 * @author XIE
 */
public class ViewCategory {

	/**
	 * 分类id(唯一)
	 */
	private String id;

	/**
	 * 分类名称（同一分类下不可重复）
	 */
	private String name;

	/**
	 * 用户唯一标识
	 */
	private String userId;

	/**
	 * true时为共有的,false为私有视图
	 */
	private Boolean open;

	/**
	 * 分类的父类
	 */
	private ViewCategory parent;

	/**
	 * 分类的子类
	 */
	private List<ViewCategory> children = new ArrayList<ViewCategory>();

	private String parentName;
	
	/**
	 * 新建的时间
	 */
	private long createTime;
	/**
	 * 更新的时间
	 */
	private long updateTime;
	
	/**
	 * [默认构造函数]
	 */
	public ViewCategory() {

	}

	/**
	 * [构造函数]创建根类
	 * 
	 * @param id
	 *            分类唯一标识
	 * @param name
	 *            分类名称
	 * @param userId
	 *            用户id
	 * @param open
	 *            public/private
	 */
	public ViewCategory(String id, String name, String userId, boolean open) {
		this.id = id;
		this.name = name;
		this.userId = userId;
		this.open = open;
		this.createTime = new Date().getTime();
	}

	/**
	 * [构造函数]创建子类
	 * 
	 * @param id
	 *            分类唯一标识
	 * @param name
	 *            分类名称
	 * @param userId
	 *            用户id
	 * @param open
	 *            public/private
	 * @param parent
	 *            父类
	 */
	public ViewCategory(String id, String name, String userId, boolean open,
			ViewCategory parent) {
		this(id, name, userId, open);
		this.parent = parent;
	}

	/**
	 * 获取分类唯一标识
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置分类唯一标识
	 * 
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取分类名称（类似文件夹）
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 设置分类名称（类似文件夹）
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getUserId() {
		return userId;
	}

	/**
	 * 设置用户id
	 * 
	 * @param userId
	 *            用户id
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean getOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	/**
	 * 获取分类的父类
	 * 
	 * @return
	 */
	public ViewCategory getParent() {
		return parent;
	}

	/**
	 * 设置分类的父类
	 * 
	 * @param parent
	 */
	public void setParent(ViewCategory parent) {
		this.parent = parent;
	}

	/**
	 * 获取分类下的子类
	 * 
	 * @return
	 */
	public List<ViewCategory> getChildren() {
		return children;
	}

	/**
	 * 设置分类的子类
	 * 
	 * @param children
	 */
	public void setChildren(List<ViewCategory> children) {
		this.children = children;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	
	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 获取分类的[所有子类]
	 * 
	 * @return
	 */
	public List<ViewCategory> getAllChildren() {
		List<ViewCategory> children = this.getChildren();
		List<ViewCategory> ret = new ArrayList<ViewCategory>();
		ret.addAll(children);
		for (ViewCategory child : children) {
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}

	/**
	 * 将分类对象转换成[map对象]
	 * 
	 * @return Map<String,Object>
	 */
	public Map<String, Object> asMapForRest() {
		Map<String, Object> map = new HashMap<String, Object>();
		ViewCategory parent = this.getParent();
		map.put("id", this.getId());
		map.put("name", this.getName());
		map.put("open", this.getOpen());
		map.put("createTime", this.createTime);
		map.put("updateTime", this.updateTime);
		map.put("parent", parent != null ? parent.getId() : null);
		map.put("parentName", parent != null ? parent.getName() : null);
		return map;
	}

	/**
	 * 将分类数组转换成[UID数组]
	 * 
	 * @param cs
	 *            分类数组
	 * @return
	 */
	private List<String> csAsList(List<ViewCategory> cs) {
		List<String> list = new ArrayList<String>();
		for (ViewCategory c : cs) {
			list.add(c.getId());
		}
		return list;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((open == null) ? 0 : open.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		ViewCategory other = (ViewCategory) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (open == null) {
			if (other.open != null)
				return false;
		} else if (!open.equals(other.open))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}