package com.mmdb.model.db;

import com.mmdb.core.framework.neo4j.annotation.Space;
import com.mmdb.core.framework.neo4j.annotation.Uuid;
import com.mmdb.core.framework.neo4j.entity.NodeEntity;
import com.mmdb.model.bean.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 命名空间为[db]的数据库配置
 *
 * @author XIE
 */
@Space("db")
public class DataBase extends NodeEntity {
    private static final long serialVersionUID = 1L;
    /**
     * 数据库连接名(唯一标识)
     */
    @Uuid
    private String id;
    /**
     * 数据库类型
     */
    private String type;

    /**
     * 数据库路径
     */
    private String url;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 数据库实例名
     */
    private String schema;
    /**
     * 表名
     */
    private String table;
    /**
     * 自定义sql
     */
    private String customSqlQuery;
    /**
     * 表字段的映射
     */
    private List<Field> fields = new ArrayList<Field>();

    /**
     * [构造函数]
     */
    public DataBase() {

    }

    /**
     * [构造函数]
     *
     * @param id       唯一标识
     * @param type     数据库类型
     * @param url      数据库地址
     * @param username 数据库名称
     * @param password 数据库密码
     */
    public DataBase(String id, String type, String url, String username, String password) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * [构造函数]
     *
     * @param id       唯一标识
     * @param type     数据库类型
     * @param schema   数据库实例
     * @param url      数据库地址
     * @param username 数据库名称
     * @param password 数据库密码
     */
    public DataBase(String id, String type, String schema, String url, String username,
                    String password) {
        this.id = id;
        this.type = type;
        this.schema = schema;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 获取db配置id
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 设置db配置id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取db类型
     *
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * 设置db类型
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取db地址
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置db地址
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取db用户名
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置db用户名
     *
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取db密码
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置db密码
     *
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCustomSqlQuery() {
        return customSqlQuery;
    }

    public void setCustomSqlQuery(String customSqlQuery) {
        this.customSqlQuery = customSqlQuery;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    /**
     * 获取db的基本信息
     *
     * @return
     */
    public Map<String, Object> asMap() {
        Map<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("id", this.getId());
        retMap.put("type", this.getType());
        retMap.put("url", this.getUrl());
        retMap.put("username", this.getUsername());
        retMap.put("password", this.getPassword());
        retMap.put("schema", this.getSchema());
        retMap.put("table", this.getTable());
        retMap.put("customSqlQuery", this.getCustomSqlQuery());
        retMap.put("fields", this.fieldsAsList(this.getFields()));
        return retMap;
    }

    /**
     * 将数据库字段数组转换成[数组]
     *
     * @param fs 数据库字段数组
     * @return List<Map<String, Object>>
     */
    private List<Map<String, Object>> fieldsAsList(List<Field> fs) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Field a : fs) {
            list.add(a.asMap());
        }
        return list;
    }

    /**
     * 获取数据库表中的映射字段[名称数组]
     *
     * @return List<String>
     */
    public List<String> getFieldNames() {
        List<Field> fs = this.getFields();
        List<String> list = new ArrayList<String>();
        for (Field field : fs) {
            list.add(field.getName());
        }
        return list;
    }

    /**
     * 自定义字段名称
     *
     * @return
     */
    public List<String> getCustomFieldNames() {
        List<Field> fs = this.getFields();
        List<String> list = new ArrayList<String>();
        for (Field field : fs) {
            list.add(field.getCustomName());
        }
        return list;
    }

    /**
     * 获取表字段对应名称
     *
     * @return
     */
    public Map<String, String> getFieldMapping() {
        List<Field> fs = this.getFields();
        Map<String, String> map = new HashMap<String, String>();
        for (Field field : fs) {
            String fieldName = field.getName(), customName = field.getCustomName();
            map.put(fieldName, customName);
        }
        return map;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DataBase dataBase = (DataBase) o;

        if (id != null ? !id.equals(dataBase.id) : dataBase.id != null) return false;
        if (password != null ? !password.equals(dataBase.password) : dataBase.password != null) return false;
        if (type != null ? !type.equals(dataBase.type) : dataBase.type != null) return false;
        if (url != null ? !url.equals(dataBase.url) : dataBase.url != null) return false;
        if (username != null ? !username.equals(dataBase.username) : dataBase.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
