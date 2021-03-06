package com.mmdb.model.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.mmdb.model.categroy.ViewCategory;
import com.mmdb.ruleEngine.Tool;
import com.mmdb.util.HexString;

/**
 * 命名空间为[viewInfo]的视图分类下的数据<br>
 * 视图内容是XML
 * 
 * @author XIE
 */
public class ViewInformation {
	//视图的软删除状态
	public static final String SOFT_DELETE = "SOFT_DELETE";
	//视图的正常状态
	public static final String NORMAL = "NORMAL";

	/**
	 * 数据id(分类下唯一)
	 */
	private String id;
	/**
	 * 名称
	 */
	private String name;
	/**
	 * 所属分类
	 */
	private ViewCategory category;
	/**
	 * 所属分类id
	 */
	private String categoryId;
	/**
	 * 要保存的XML内容
	 */
	private String xml = "";
	/**
	 * XML版本号
	 */
	private Integer version;
	/**
	 * 视图创建用户
	 */
	private String userName;

	/**
	 * 视图订阅用户
	 */
	private List<String> subscripers;

	/**
	 * true 为 public/false 为 private
	 */
	private Boolean open;

	/**
	 * 新建的时间
	 */
	private long createTime;
	/**
	 * 更新的时间
	 */
	private long updateTime;

	/**
	 * 要保存的SVG内容
	 */
	private String svg;

	/**
	 * 要保存的点位信息
	 */
	private String points;

	/**
	 * 视图缩略图
	 */
	private String imageUrl;

	/**
	 * 历史版本
	 */
	private List<Map<String, Object>> history = new ArrayList<Map<String, Object>>();

	/**
	 * 描述
	 */
	private String description;
	/**
	 * 视图的状态
	 */
	private String status = NORMAL;
	
	/**
	 * 默认[构造函数]
	 */
	public ViewInformation() {

	}

	/**
	 * [构造函数] 初始化指定分类下的视图
	 * 
	 * @param category
	 *            所属分类
	 * @param id
	 *            视图名称
	 * @param xml
	 *            视图XML内容
	 * @param svg
	 *            视图SVG内容
	 * @param imageUrl
	 *            缩略图
	 * @param open
	 *            public/private
	 * @param description
	 *            描述
	 * @throws Exception
	 */
	public ViewInformation(ViewCategory category, String categoryId, String id,
			String name, String xml, String svg, String points,
			String imageUrl, boolean open, String description, long createTime,
			long updateTime) throws Exception {
		if (category == null) {
			throw new Exception("视图分类不存在");
		} else {
			this.setCategory(category);
			this.setCategoryId(category.getId());
		}
		// if (id == null || id.equals("")) {
		// throw new Exception("视图名称为空");
		// } else {
		// this.setId(id);
		// }
		if (name == null || name.equals("")) {
			throw new Exception("视图名称为空");
		} else {
			this.setName(name);
		}
		if (categoryId == null || categoryId.equals("")) {
			throw new Exception("视图分类为空");
		} else {
			this.setCategoryId(categoryId);
		}
		if (xml == null || xml.equals("")) {
			throw new Exception("视图xml无数据");
		} else {
			this.setXml(xml);
		}
		if (svg == null || svg.equals("")) {
			throw new Exception("视图svg无数据");
		} else {
			this.setSvg(svg);
		}
		if (points == null || points.equals("")) {
			throw new Exception("视图points无数据");
		} else {
			this.setPoints(points);
		}
		// if (type == null || type.equals("")) {
		// throw new Exception("视图type无数据");
		// } else {
		// this.setSvg(type);
		// }
		this.setOpen(open);
		if (imageUrl == null || imageUrl.equals("")) {
			throw new Exception("视图缩略图为空");
		} else {
			this.setImageUrl(imageUrl);
		}
		if (description == null) {
			this.setDescription("");
		} else {
			this.setDescription(description);
		}
		// HttpSession session = WebContextFactory.get().getSession();
		// if (session != null) {
		// UserObject user = (UserObject) session.getAttribute("loginUser");
		// if (user != null)
		// this.userName = user.getName();
		// }
		this.setVersion(1);
		this.setCreateTime(createTime);
		this.setUpdateTime(updateTime);
		// this.setColumn(column);
		// this.setRow(row);
	}

	/**
	 * [构造函数] 初始化指定分类下的视图
	 * 
	 * @param category
	 *            所属分类
	 * @param id
	 *            视图名称
	 * @param xml
	 *            视图XML内容
	 * @param svg
	 *            视图SVG内容
	 * @param imageUrl
	 *            缩略图
	 * @param version
	 *            视图版本号
	 * @param open
	 *            public/private
	 * @param description
	 *            描述
	 * @throws Exception
	 */
	public ViewInformation(ViewCategory category, String categoryId, String id,
			String name, String xml, String svg, String points,
			String imageUrl, Integer version, boolean open, String description,
			long createTime, long updateTime) throws Exception {
		this(category, categoryId, id, name, xml, svg, points, imageUrl, open,
				description, createTime, updateTime);
		if (version != null) {
			this.setVersion(version);
		}
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	/**
	 * 获取XML名称
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置XML名称
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public String getType() {
	// return type;
	// }
	//
	// public void setType(String type) {
	// this.type = type;
	// }
	//
	// public long getColumn() {
	// return column;
	// }
	//
	// public void setColumn(long column) {
	// this.column = column;
	// }
	//
	// public long getRow() {
	// return row;
	// }
	//
	// public void setRow(long row) {
	// this.row = row;
	// }

	/**
	 * 获取XML视图分类
	 */
	public ViewCategory getCategory() {
		return category;
	}

	/**
	 * 设置XML视图分类
	 */
	public void setCategory(ViewCategory category) {
		this.category = category;
	}

	/**
	 * 获取XML视图内容
	 */
	public String getXml() {
		return xml;
	}

	/**
	 * 设置XML视图内容
	 * 
	 * @param xml
	 *            XML视图内容
	 */
	public void setXml(String xml) {
		this.xml = xml;
	}

	/**
	 * 获取视图分类的id
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * 设置视图分类的id
	 */
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	/**
	 * 获取版本号
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * 设置版本号
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getSvg() {
		return svg;
	}

	public void setSvg(String svg) {
		this.svg = svg;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
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

	public Boolean getOpen() {
		return open;
	}

	public void setOpen(Boolean open) {
		this.open = open;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getSubscripers() {
		return subscripers;
	}

	public void setSubscripers(List<String> subscripers) {
		this.subscripers = subscripers;
	}

	public List<Map<String, Object>> getHistory() {
		return history;
	}

	public void setHistory(List<Map<String, Object>> history) {
		this.history = history;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public String getStatus() {
		return status;
	}
	/**
	 * 请使用视图 ViewInformation.SOFT_DELETE | ViewInformation.NORMAL
	 * @param status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 把数据内容转成map
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("_id_", this.getId());
		JSONArray _neo4jid_ = new JSONArray();
		_neo4jid_.add(this.getCategory().getId());
		_neo4jid_.add(this.getId());
		ret.put("_neo4jid_", HexString.encode(_neo4jid_.toString()));
		ret.put("_category_", this.getCategory().getName());
		ret.put("_categoryId_", this.getCategoryId());
		ret.put("name", this.getName());
		ret.put("svg", this.getSvg());
		ret.put("xml", this.getXml());
		ret.put("userName", this.getUserName());
		ret.put("subscripers", this.getSubscripers());
		ret.put("open", this.getOpen());
		ret.put("version", this.getVersion());
		ret.put("createTime", this.getCreateTime());
		ret.put("updateTime", this.getUpdateTime());
		String prefix = Tool.findPath("graph", "resource");
		ret.put("imageUrl", prefix + "/thumbnail/" + this.getImageUrl());
		ret.put("description", this.getDescription());
		ret.put("historySize", this.getHistory().size());
		return ret;
	}

	public Map<String, Object> asMapForRest() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("categoryId", this.getCategoryId());
		ret.put("categoryName", this.getCategory().getName());
		ret.put("id", this.getId());
		ret.put("name", this.getName());
		// ret.put("svg", this.getSvg());
		ret.put("svg", this.getPoints());
		ret.put("xml", this.getXml());
		ret.put("userName", this.getUserName());
		ret.put("subscripers", this.getSubscripers());
		ret.put("open", this.getOpen());
		ret.put("version", this.getVersion());
		ret.put("createTime", this.getCreateTime());
		ret.put("updateTime", this.getUpdateTime());
		String prefix = Tool.findPath("graph", "resource");
		ret.put("imageUrl", prefix + "/thumbnail/" + this.getImageUrl());
		ret.put("description", this.getDescription());
		ret.put("historySize", this.getHistory().size());
		ret.put("status", status);
		return ret;
	}

	public String getProjectImageUrl() {
		String prefix = Tool.findPath("graph", "resource");
		return prefix + "/thumbnail/" + this.getImageUrl();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
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
		ViewInformation other = (ViewInformation) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}