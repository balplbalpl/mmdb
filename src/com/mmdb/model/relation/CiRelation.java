package com.mmdb.model.relation;

import java.util.HashMap;
import java.util.Map;

import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.model.categroy.RelCategory;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.mapping.InCiCateMap;
import com.mmdb.model.mapping.SourceToRelationMapping;

/**
 * 命名空间为[ciInfoRel]CI数据之间的关系
 * 
 * @author XIE
 */
public class CiRelation {
	/**
	 * 数据关系id(唯一)
	 */
	private String id;
	/**
	 * 所属分类的起点
	 */
	private String startCateId;
	/**
	 * 数据的起点
	 */
	private CiInformation startInfo;
	/**
	 * 所属分类的终点
	 */
	private String endCateId;
	/**
	 * 数据的终点
	 */
	private CiInformation endInfo;
	/**
	 * 内部映射的ID
	 */
	private String inMapId;
	/**
	 * 外部映射的ID
	 */
	private String outMapId;
	/**
	 * 所属关系分类UID
	 */
	private String relCateId;
	/**
	 * 数据来源
	 */
	private String source;
	/**
	 * 标签
	 */
	private String tag;
	/**
	 * 关系分类的属性对应的值
	 */
	private Dynamic<String, Object> relValue = new Dynamic<String, Object>();
	/**
	 * neo4jId
	 */
	private Long relationId;

	private String owner;
	
	/**
	 * [构造函数]
	 */
	public CiRelation() {

	}

	/**
	 * 新建两个数据关系[构造函数]
	 * 
	 * @param si
	 *            CI起点
	 * @param ei
	 *            CI终点
	 * @param relCate
	 *            关系分类
	 * @param relValue
	 *            关系分类属性对应的值
	 */
	public CiRelation(CiInformation si, CiInformation ei, RelCategory relCate,
			Dynamic<String, Object> relValue) {
		this.startCateId = si.getCategoryId();
		this.startInfo = si;
		this.endCateId = ei.getCategoryId();
		this.endInfo = ei;
		this.relCateId = relCate.getId();
		this.relValue = relValue;

		this.id = si.getCiHex() + "_" + this.relCateId + "_" + ei.getCiHex();
	}

	/**
	 * 新建内部映射关系[构造函数]
	 * 
	 * @param si
	 *            CI起点
	 * @param ei
	 *            CI终点
	 * @param inMap
	 *            内部映射
	 */
	public CiRelation(CiInformation si, CiInformation ei, InCiCateMap inMap) {
		this.startCateId = si.getCategoryId();
		this.startInfo = si;
		this.endCateId = ei.getCategoryId();
		this.endInfo = ei;
		this.inMapId = inMap.getName();
		this.relCateId = inMap.getRelCate().getId();
		this.relValue = new Dynamic<String, Object>().from(inMap.getRelValue());
		this.id = si.getCiHex() + "_" + this.relCateId + "_" + ei.getCiHex();
	}

	/**
	 * 新建内部映射关系[构造函数]
	 * 
	 * @param si
	 *            CI起点
	 * @param ei
	 *            CI终点
	 * @param outMap
	 *            外部映射
	 */
	public CiRelation(CiInformation si, CiInformation ei,
			SourceToRelationMapping outMap) {
		this.startCateId = si.getCategoryId();
		this.startInfo = si;
		this.endCateId = ei.getCategoryId();
		this.endInfo = ei;
		this.outMapId = outMap.getId();
		this.relCateId = outMap.getRelCate().getId();
		this.relValue = new Dynamic<String, Object>()
				.from(outMap.getRelValue());
		this.id = si.getCiHex() + "_" + this.relCateId + "_" + ei.getCiHex();
	}

	/**
	 * 获取数据关系Id
	 */
	public String getId() {
		return id;
	}

	/**
	 * 设置数据关系Id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 获取分类起点
	 */
	public String getStartCateId() {
		return startCateId;
	}

	/**
	 * 设置分类起点
	 */
	public void setStartCateId(String startCate) {
		this.startCateId = startCate;
	}

	/**
	 * 获取分类终点
	 */
	public String getEndCateId() {
		return endCateId;
	}

	/**
	 * 设置分类终点
	 */
	public void setEndCateId(String endCate) {
		this.endCateId = endCate;
	}

	/**
	 * 获取数据的起点
	 */
	public CiInformation getStartInfo() {
		return startInfo;
	}

	/**
	 * 设置数据起点
	 * 
	 * @param startInfo
	 *            CI数据
	 */
	public void setStartInfo(CiInformation startInfo) {
		this.startInfo = startInfo;
	}

	/**
	 * 获取数据的终点
	 */
	public CiInformation getEndInfo() {
		return endInfo;
	}

	/**
	 * 设置数据的终点
	 * 
	 * @param endInfo
	 *            CI数据
	 */
	public void setEndInfo(CiInformation endInfo) {
		this.endInfo = endInfo;
	}

	/**
	 * 获取内部映射的ID
	 */
	public String getInMapId() {
		return inMapId;
	}

	/**
	 * 设置内部映射的ID
	 */
	public void setInMapId(String inMapId) {
		this.inMapId = inMapId;
	}

	/**
	 * 获取外部映射
	 */
	public String getOutMapId() {
		return outMapId;
	}

	/**
	 * 设置内部映射
	 */
	public void setOutMapId(String outMapId) {
		this.outMapId = outMapId;
	}

	/**
	 * 获取数据间的关系类型ID
	 */
	public String getRelCateId() {
		return relCateId;
	}

	/**
	 * 设置数据之间的关系ID
	 */
	public void setRelCateId(String relCateId) {
		this.relCateId = relCateId;
	}

	/**
	 * 获取关系分类的属性对应的值
	 */
	public Dynamic<String, Object> getRelValue() {
		return relValue;
	}

	/**
	 * 设置关系分类的属性对应的值
	 */
	public void setRelValue(Dynamic<String, Object> relValue) {
		this.relValue = relValue;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelationId() {
		return relationId;
	}

	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}
	

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * 将分类对象转换成[map对象]
	 */
	public Map<String, Object> asMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", this.getId());
		map.put("startCateId", this.getStartCateId());
		map.put("startInfo", this.getStartInfo().asMap());
		map.put("endCateId", this.getEndCateId());
		map.put("endInfo", this.getEndInfo().asMap());
		map.put("relCateId", this.getRelCateId());
		map.put("relValue", this.getRelValue());
		map.put("owner", this.getOwner()==null ? "" : this.getOwner());
		return map;
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
		CiRelation other = (CiRelation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
