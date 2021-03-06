package com.mmdb.model.categroy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.mmdb.common.Global;
import com.mmdb.model.bean.Attribute;

/**
 * KPI分类<br>
 * 
 * @author yuhao.guan
 */
public class KpiCategory implements Serializable{
	
	private static final long serialVersionUID = 7962339920946538433L;
	
	/**
	 * kpi属性map
	 */
	public static List<Attribute> attrList = new LinkedList<Attribute>();
	
	// 初始化属性数据
	static {
		
		try {
			Attribute eleAttr = new Attribute("name", true, false);
			attrList.add(eleAttr);
			eleAttr = new Attribute("kpiCategoryName", true, false);
			attrList.add(eleAttr);
			eleAttr = new Attribute("threshold", true, false);
			attrList.add(eleAttr);
			eleAttr = new Attribute("unit", false, false);
			attrList.add(eleAttr);
			eleAttr = new Attribute("owner", false, false);
			attrList.add(eleAttr);
		} catch (Exception e) {
			
		}
		
	}
	/**
	 * 分类id
	 */
	private String id;
	/**
	 * 分类名称
	 */
	private String name;
	
	/**
	 * 分类的图片（可以重复）
	 */
	private String image;
	
	/**
	 * 分类继承的父类
	 */
	private KpiCategory parent;
	
	private String parentId;
	
	/**
	 * 创建者
	 */
	private String owner;
	
	/**
	 * 分类的子类
	 */
	private List<KpiCategory> children = new ArrayList<KpiCategory>();

	/**
	 * 递归获取分类的[所有子类]
	 * 
	 * @return List<KpiCategory>
	 */
	public List<KpiCategory> getAllChildren() {
		List<KpiCategory> children = this.getChildren();
		List<KpiCategory> ret = new ArrayList<KpiCategory>();
		ret.addAll(children);
		for (KpiCategory child : children) {
			ret.addAll(child.getAllChildren());
		}
		return ret;
	}
	
	/**
     * 将分类对象转换成[map对象]
     *
     * @return Map<String,Object>
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", this.getId());
        map.put("name",this.getName());
        map.put("image", this.getImage());
        map.put("parent", this.getParentId());
        map.put("owner",this.getOwner());
		map.put("text", this.getName());
		map.put("icon", "resource/svg/" + Global.svgBaseTheme + "/" +this.getImage());
        return map;
    }
    
    public Map<String, Object> toMapForRest() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", this.getId());
        map.put("name",this.getName());
        map.put("icon", this.getImage());
        map.put("parent", this.getParentId());
        map.put("owner",this.getOwner());
		map.put("text", this.getName());
		map.put("icon", "resource/svg/" + Global.svgBaseTheme + "/" +this.getImage());
        return map;
    }
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
    public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public KpiCategory getParent() {
		return parent;
	}

	public void setParent(KpiCategory parent) {
		this.parent = parent;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
	public List<KpiCategory> getChildren() {
		return children;
	}

	public void setChildren(List<KpiCategory> children) {
		this.children = children;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
}
