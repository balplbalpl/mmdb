package com.mmdb.model.icon;

import java.util.HashMap;
import java.util.Map;

import com.mmdb.core.framework.neo4j.annotation.Space;

/**
 * 图标库
 * 
 * @author XIE
 * 
 */
@Space("iconLibrary")
public class IconLibrary {
	private static final long serialVersionUID = 1L;
	/**
	 * 图标
	 */
	private String icon;
	/**
	 * 3D模型
	 */
	private String model;
	
	/**
	 * neo4jid
	 * 
	 * */
	private Long neo4jid;
	
	

	public Long getNeo4jid() {
		return neo4jid;
	}

	public void setNeo4jid(Long neo4jid) {
		this.neo4jid = neo4jid;
	}

	public IconLibrary() {
		super();
	}

	public IconLibrary(String icon, String model) {
		super();
		this.icon = icon;
		this.model = model;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	/**
	 * 把数据内容转成map
	 * 
	 * @return
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("_neo4jid_", this.getNeo4jid());
		ret.put("icon", this.getIcon());
		ret.put("model", this.getModel());
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((icon == null) ? 0 : icon.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		IconLibrary other = (IconLibrary) obj;
		if (icon == null) {
			if (other.icon != null)
				return false;
		} else if (!icon.equals(other.icon))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}
}
