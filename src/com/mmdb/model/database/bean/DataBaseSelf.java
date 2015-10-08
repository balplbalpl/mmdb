package com.mmdb.model.database.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库简单操作动作
 * Created by XIE on 2015/3/28.
 */
public class DataBaseSelf implements Serializable {
    private static final long serialVersionUID = 364754469451784L;
    /**
     * 选择的schema
     */
    private String schema;
    /**
     * 选择的table
     */
    private String table;
    /**
     * 是否是自定义sql
     */
    private boolean self = false;
    /**
     * 自定义sql
     */
    private String customSql;
    /**
     * 表字段的映射
     */
    private List<Field> fields = new ArrayList<Field>();

    /**
     * 用户对数据库默认操作
     *
     * @param self    是否有自定义sql true/false
     * @param selfMap {schema:'',table:'',customSql:''}
     * @param fields  {}
     * @throws Exception
     */
    public DataBaseSelf(boolean self, Map<String, String> selfMap, List<Field> fields) throws Exception {
        if (self) {
            this.self = self;
            String sql = selfMap.get("customSql");
            if (sql == null || sql.equals("")) {
                throw new Exception("自定义SQL语句不能为空");
            }
            this.customSql = sql;
            this.schema = selfMap.get("schema");
        } else {
            String _table = selfMap.get("table");
            if (_table == null || _table.equals("")) {
                throw new Exception("表名不能为空");
            }
            this.schema = selfMap.get("schema");
            this.table = _table;
        }
        if (fields == null) {
            fields = new ArrayList<Field>();
        }
        this.fields = fields;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCustomSql() {
        return customSql;
    }

    public void setCustomSql(String customSql) {
        this.customSql = customSql;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Map<String, Map<String, Object>> coventFields() {
        Map<String, Map<String, Object>> fm = new HashMap<String, Map<String, Object>>();
        for (Field f : this.getFields()) {
            fm.put(f.getName(), f.asMap());
        }
        return fm;
    }

    public Map<String, Object> asMap() {
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("schema", this.getSchema());
        ret.put("table", this.getTable());
        ret.put("customSql", this.getCustomSql());
        ret.put("isSelf", this.isSelf());
        ret.put("fields", this.coventFields());
        return ret;
    }
}
