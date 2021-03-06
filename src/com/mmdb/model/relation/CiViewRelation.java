package com.mmdb.model.relation;

import com.mmdb.core.framework.neo4j.annotation.*;
import com.mmdb.model.info.CiInformation;
import com.mmdb.model.info.ViewInformation;
import com.mmdb.util.HexString;

import java.util.HashMap;
import java.util.Map;

/**
 * 命名空间为[ciViewRelation]CI-VIEW数据之间的关系
 *
 * @author XIE
 */
@Space("ciViewRelation")
public class CiViewRelation  {
    private static final long serialVersionUID = 1L;
    /**
     * 数据关系id(唯一)
     */
    @Uuid
    private String id;
  

	/**
     * 所属分类的起点
     */
    private String startCateId;
    /**
     * 数据的起点
     */
//    @RelationStart(CiInformation.class)
    private CiInformation startInfo;
    /**
     * 所属分类的终点
     */
    private String endCateId;
    /**
     * 数据的终点
     */
//    @RelationEnd(ViewInformation.class)
    private ViewInformation endInfo;
    /**
     * 所属关系分类UID
     */
//    @RelationType
    private String relation;
    
    /**
     * neo4jId
     */
    private Long relationId;

    public CiViewRelation() {

    }

    /**
     * 新建两个数据关系[构造函数]
     *
     * @param ci   CI起点
     * @param view View终点
     */
    public CiViewRelation(CiInformation ci, ViewInformation view) {
        this.startCateId = ci.getCategoryId();
        this.startInfo = ci;
        this.endCateId = view.getCategoryId();
        this.endInfo = view;
        this.relation = RelationshipTypes.CI2VIEW;
        this.id = HexString.encode(HexString.json2Str(ci.getCategoryId(), ci.getId())) + "_" + this.relation + "_" + HexString.encode(HexString.json2Str(view.getCategoryId(),view.getId()));
//        this.id = ci.getId() + "_" + this.relation + "_" + view.getId();
    }

    /**
     * 新建两个数据关系[构造函数]
     *
     * @param ci       CI起点
     * @param view     View终点
     * @param relation 内部关系类型
     */
    public CiViewRelation(CiInformation ci, ViewInformation view, String relation) {
        this(ci, view);
        if (relation == null || relation.equals("")) {
            this.relation = RelationshipTypes.VIEW2CI;
        } else {
            this.relation = RelationshipTypes.CI2VIEW;
        }
        this.id = HexString.json2Str(ci.getCategoryId(), ci.getId()) + "_" + this.relation + "_" + HexString.json2Str(view.getCategoryId(), view.getId());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartCateId() {
        return startCateId;
    }

    public void setStartCateId(String startCateId) {
        this.startCateId = startCateId;
    }

    public CiInformation getStartInfo() {
        return startInfo;
    }

    public void setStartInfo(CiInformation startInfo) {
        this.startInfo = startInfo;
    }

    public String getEndCateId() {
        return endCateId;
    }

    public void setEndCateId(String endCateId) {
        this.endCateId = endCateId;
    }

    public ViewInformation getEndInfo() {
        return endInfo;
    }

    public void setEndInfo(ViewInformation endInfo) {
        this.endInfo = endInfo;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
    
    public Long getRelationId() {
  		return relationId;
  	}

  	public void setRelationId(Long relationId) {
  		this.relationId = relationId;
  	}

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endInfo == null) ? 0 : endInfo.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        result = prime * result + ((startInfo == null) ? 0 : startInfo.hashCode());
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
        CiViewRelation other = (CiViewRelation) obj;
        if (endInfo == null) {
            if (other.endInfo != null)
                return false;
        } else if (!endInfo.equals(other.endInfo))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (relation == null) {
            if (other.relation != null)
                return false;
        } else if (!relation.equals(other.relation))
            return false;
        if (startInfo == null) {
            if (other.startInfo != null)
                return false;
        } else if (!startInfo.equals(other.startInfo))
            return false;
        return true;
    }

    /**
     * 将分类对象转换成[map对象]
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("_neo4jid_", this.getRelationId());
        map.put("_neo4jid_", this.getId());
        map.put("id", this.getId());
        map.put("startCateId", this.getStartCateId());
        map.put("startInfo", this.getStartInfo().asMap());
        map.put("endCateId", this.getEndCateId());
        map.put("endInfo", this.getEndInfo().asMap());
        return map;
    }

}