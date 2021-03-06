package com.mmdb.model.database.bean;

import java.util.HashMap;
import java.util.Map;

public class DataSourcePool {// extends NodeEntity
    /**
     * 数据库连接名(唯一标识)
     */
    private String id;
    
    private String name;
    /**
     * 描述
     */
    private String description;
    
    private String schema;

	private String customSql;
    
    private String tableName;
    
    private boolean isSelf;
    
	private String databaseConfigId;
	
	private String owner;
	
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCustomSql() {
		return customSql;
	}

	public void setCustomSql(String customSql) {
		this.customSql = customSql;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

    public String getDatabaseConfigId() {
		return databaseConfigId;
	}

	public void setDatabaseConfigId(String databaseConfigId) {
		this.databaseConfigId = databaseConfigId;
	}
	
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public boolean isSelf() {
		return isSelf;
	}

	public void setSelf(boolean isSelf) {
		this.isSelf = isSelf;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Map<String, Object> asMap() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("id", this.getId());
        ret.put("name", this.getName());
        ret.put("description", this.getDescription());
        ret.put("schema", this.getSchema());
        ret.put("tableName", this.getTableName());
        ret.put("customSql", this.getCustomSql());
        ret.put("isSelf", this.isSelf());
        ret.put("owner", owner);
        return ret;
    }
}
