package com.mmdb.model.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import com.mmdb.common.Global;
import com.mmdb.core.utils.TimeUtil;
import com.mmdb.model.bean.Attribute;
import com.mmdb.model.categroy.CiCategory;
import com.mmdb.util.HexString;

/**
 * 命名空间为[ciInfo]的配置项分类下的数据<br>
 * <p id为mongo的id,但是给前台使用的是Hexid就是分类id+ >
 * 
 * @author XIE
 */
public class CiInformation {

	/**
	 * 数据id(唯一)
	 */
	private String id;
	/**
	 * ci的十六进制id由 ci分类名字+ci主键值 转化
	 */
	private String ciHex;

	/**
	 * 所属分类
	 */
	private CiCategory category;
	/**
	 * 数据上的内容
	 */
	private Map<String, Object> data = new HashMap<String, Object>();
	/**
	 * 数据来源
	 */
	private String source;
	/**
	 * 更新时间
	 */
	private String updateTime;
	/**
	 * 创建时间
	 */
	private String createTime;
	/**
	 * 属性记录
	 */
	private Map<String, String> record = new HashMap<String, String>();
	/**
	 * 所属分类id
	 */
	private String categoryId;

	/**
	 * 标签
	 */
	private String tag;

	/**
	 * 所有者
	 */
	private String owner;

	/**
	 * [构造函数]
	 */
	public CiInformation() {

	}

	/**
	 * 新建数据[构造函数]
	 * 
	 * @param category
	 *            分类
	 * @param source
	 *            数据源
	 * @param data
	 *            数据内容
	 * @throws Exception
	 */
	public CiInformation(CiCategory category, String source,
			Map<String, Object> data) throws Exception {
		if (source == null) {
			throw new Exception("数据来源不存在");
		} else {
			this.source = source;
		}
		if (category == null) {
			throw new Exception("分类不存在");
		} else {
			this.category = category;
			this.categoryId = category.getId();
		}
		if (data == null || data.size() < 1) {
			throw new Exception("data无数据");
		} else {
			this.createTime = TimeUtil.getTime(TimeUtil.YMDHMS);
			this.updateTime = this.getCreateTime();
		}
		// 处理id,或许可能是继承id
		Attribute major = category.getMajor();
		String majorName = major.getName();
		if (data.containsKey(majorName)) {// 判断数据中对象的主键
			Object id = data.get(majorName);// 把主键的值做为该分类数据的唯一标识
			if (id == null || id.equals("")) {
				throw new Exception("主键不能为空");
			}
			try {
				id = major.convert(id.toString());
				this.id = id.toString();
				this.data.put(majorName, id);
				// this.record.put(majorName, source);
			} catch (Exception e) {
				throw new Exception("数据[" + data + "]" + e.getMessage());
			}
		} else {
			throw new Exception("数据[" + data + "],缺少分类[" + category.getName()
					+ "]主键[" + majorName + "]");
		}
		// 处理属性，或许有继承属性
		List<Attribute> attributes = category.getAllAttributes();
		for (Attribute attribute : attributes) {
			String value = "";
			String field = attribute.getName();
			if (data.containsKey(field)) {
				Object val = data.get(field);
				if (val != null) {
					value = val.toString();
				}
			} else {
				value = attribute.getDefaultValue();
			}
			// if (attribute.getRequired() && value.equals("")) {
			// throw new Exception("分类[" + category.getName() + "]中[" +
			// data.get(majorName) + "]属性[" + field + "]是必填项");
			// }
			this.data.put(field, attribute.convert(value));
			// this.record.put(field, source);
		}
	}

	/**
	 * 获取分类下一条数据的id(不同分类下id可以重复)
	 */
	public String getId() {
		return id;
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
		CiInformation other = (CiInformation) obj;
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

	/**
	 * 设置分类下一条数据的id(不同分类下id可以重复)
	 * 
	 * @param id
	 *            数据的id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取数据的分类
	 */
	public CiCategory getCategory() {
		return category;
	}

	/**
	 * 设置数据的分类
	 */
	public void setCategory(CiCategory category) {
		this.category = category;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	/**
	 * 获取数据来源
	 */
	public String getSource() {
		return source;
	}

	/**
	 * 设置数据来源
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * 获取更新时间
	 */
	public String getUpdateTime() {
		return updateTime;
	}

	/**
	 * 设置更新时间
	 */
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * 获取创建数据时间
	 */
	public String getCreateTime() {
		return createTime;
	}

	/**
	 * 设置创建数据时间
	 */
	public void setCreateTime(String creationTime) {
		this.createTime = creationTime;
	}

	/**
	 * 获取属性记录
	 */
	public Map<String, String> getRecord() {
		return record;
	}

	/**
	 * 设置属性记录
	 */
	public void setRecord(Map<String, String> record) {
		this.record = record;
	}

	/**
	 * 获取分类id
	 */
	public String getCategoryId() {
		return categoryId;
	}

	/**
	 * 设置分类id
	 */
	public void setCategoryId(String categoryid) {
		this.categoryId = categoryid;
	}
	/**
	 * 返回主键的值
	 * @return
	 */
	public String getName() {
		return (String) this.data.get(this.category.getMajor().getName());
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getCategoryName() {
		if (category != null) {
			return category.getName();
		}
		if (ciHex == null || "".equals(ciHex))
			return null;
		return JSONArray.fromObject(HexString.decode(ciHex)).getString(0);
	}

	public String getCiHex() {
		if (ciHex == null || "".equals(ciHex)) {
			JSONArray hex = new JSONArray();
			hex.add(this.getCategory().getName());
			hex.add(this.getName());
			ciHex = HexString.encode(hex.toString());
		}
		return ciHex;
	}

	public void setCiHex(String ciHex) {
		this.ciHex = ciHex;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * 把数据内容转成map
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> ret = this.getData();
		List<String> levels = new ArrayList<String>();
		List<Attribute> attributes = this.getCategory().getAllAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getLevel() != null) {
				levels.add(attribute.getName());
			}
		}
		LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> iter = ret.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (levels.contains(key)) {
				dataMap.put(key, value);
				iter.remove();
			}
		}
		dataMap.putAll(ret);
		dataMap.put("_id_", this.getId());
		JSONArray _neo4jid_ = new JSONArray();
		_neo4jid_.add(this.getCategory().getName());
		_neo4jid_.add(this.getName());
		dataMap.put("_jsonId_", HexString.encode(_neo4jid_.toString()));
		dataMap.put("_category_", this.getCategory().getName());
		dataMap.put("_categoryId_", this.getCategoryId());
		Attribute attribute = this.getCategory().getClientId();
		if (attribute != null) {
			String name = attribute.getName();
			dataMap.put("_name_", dataMap.get(name));
		} else {
			dataMap.put("_name_", this.getId());
		}
		dataMap.put("_major_",
				dataMap.get(this.getCategory().getMajor().getName()));
		return dataMap;
	}

	public Map<String, Object> asMapForRest() {
		Map<String, Object> ret = new HashMap<String, Object>();
		Map<String, Object> datas = this.getData();

		List<String> levels = new ArrayList<String>();
		List<Attribute> attributes = this.getCategory().getAllAttributes();
		for (Attribute attribute : attributes) {
			if (attribute.getLevel() != null) {
				levels.add(attribute.getName());
			}
		}
		LinkedHashMap<String, Object> dataMap = new LinkedHashMap<String, Object>();
		Iterator<Map.Entry<String, Object>> iter = datas.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Object> entry = iter.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			if (levels.contains(key)) {
				dataMap.put(key, value);
				iter.remove();
			}
		}
		dataMap.putAll(datas);
		this.setData(dataMap);
		ret.put("data", dataMap);
		ret.put("id", ciHex);
		ret.put("mongoId", id);
		ret.put("categoryId", this.getCategoryId());
		ret.put("categoryName", getCategoryName());
		ret.put("icon", "/resource/svg/" + Global.svgBaseTheme + "/"
				+ this.getCategory().getImage());

		Attribute clientId = this.getCategory().getClientId();
		String major = this.getCategory().getMajor().getName();
		ret.put("name", dataMap.get(major));
		Object client = null;
		if(clientId != null){
			client = dataMap.get(clientId.getName());
		}
		ret.put("client",
				client == null || "".equals(client) ? dataMap.get(major)
						: client);
		return ret;
	}
}
