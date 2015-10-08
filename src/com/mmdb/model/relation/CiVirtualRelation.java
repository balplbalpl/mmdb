package com.mmdb.model.relation;

import com.mmdb.core.framework.neo4j.annotation.*;
import com.mmdb.core.framework.neo4j.entity.Dynamic;
import com.mmdb.core.framework.neo4j.entity.RelationEntity;
import com.mmdb.model.info.CiInformation;
import com.mmdb.util.HexString;

import java.util.HashMap;
import java.util.Map;

@Space("ciVirtualRel")
public class CiVirtualRelation extends RelationEntity {
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
    @RelationStart(CiInformation.class)
    private CiInformation startInfo;
    /**
     * 所属分类的终点
     */
    private String endCateId;
    /**
     * 数据的终点
     */
    @RelationEnd(CiInformation.class)
    private CiInformation endInfo;
    /**
     * 所属关系分类UID
     */
    @RelationType
    private String relCateId = RelationshipTypes.VIRTUAL;
    /**
     * 关系分类的属性对应的值
     */
    private Dynamic<String, Object> relValue = new Dynamic<String, Object>();

    public CiVirtualRelation() {

    }

    /**
     * 新建两个数据关系[构造函数]
     *
     * @param si CI起点
     * @param ei KPI终点
     */
    public CiVirtualRelation(CiInformation si, CiInformation ei) {
        this.startCateId = si.getCategoryId();
        this.startInfo = si;
        this.endCateId = ei.getCategoryId();
        this.endInfo = ei;
        this.id = HexString.json2Str(si.getCategoryId(), si.getId()) + "_" + relCateId + "_" + HexString.json2Str(ei.getCategoryId(), ei.getId());
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

    public CiInformation getEndInfo() {
        return endInfo;
    }

    public void setEndInfo(CiInformation endInfo) {
        this.endInfo = endInfo;
    }

    public String getRelCateId() {
        return relCateId;
    }

    public void setRelCateId(String relCateId) {
        this.relCateId = relCateId;
    }

    public Dynamic<String, Object> getRelValue() {
        return relValue;
    }

    public void setRelValue(Dynamic<String, Object> relValue) {
        this.relValue = relValue;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("_neo4jid_", HexString.encode(this.getId()));
        map.put("id", this.getId());
        map.put("startCateId", this.getStartCateId());
        map.put("startInfo", this.getStartInfo().asMap());
        map.put("endCateId", this.getEndCateId());
        map.put("endInfo", this.getEndInfo().asMap());
        map.put("relCateId", this.getRelCateId());
        map.put("relValue", this.getRelValue());
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
        CiVirtualRelation other = (CiVirtualRelation) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}