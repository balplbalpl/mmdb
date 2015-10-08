package com.mmdb.model.database.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库字段配置
 * Created by XIE on 2015/3/10.
 */
public class Field implements Serializable {
    private static final long serialVersionUID = 194785446546437L;
    /**
     * 原DB字段名称
     */
    private String name;
    /**
     * 自定义字段名称
     */
    private String customName;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 自定义字段类型
     */
    private String customType;
    /**
     * 是否隐藏 默认不隐藏
     */
    private Boolean hide = false;

    public Field() {

    }

    /**
     * 默认显示的数据库字段
     *
     * @param name 字段名称
     * @param type 字段类型
     */
    public Field(String name, String type) {
        this.name = name;
        this.customName = name;
        this.type = type;
        this.customType = type;
        this.hide = false;
    }

    /**
     * 全部参数的构造函数
     *
     * @param name       字段名称
     * @param customName 自定义字段名称
     * @param hide       是否隐藏
     */
    public Field(String name, String customName, boolean hide) {
        this.name = name;
        this.customName = customName;
        this.hide = hide;
    }

    /**
     * 全部参数的构造函数
     *
     * @param name       字段名称
     * @param customName 自定义字段名称
     * @param type       字段类型
     * @param customType 自定义字段类型
     * @param hide       是否隐藏
     */
    public Field(String name, String customName, String type, String customType, boolean hide) {
        this.name = name;
        this.customName = customName;
        this.type = type;
        this.customType = customType;
        this.hide = hide;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomType() {
        return customType;
    }

    public void setCustomType(String customType) {
        this.customType = customType;
    }

    public Boolean getHide() {
        return hide;
    }

    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Field field = (Field) o;

        if (customName != null ? !customName.equals(field.customName) : field.customName != null) return false;
        if (name != null ? !name.equals(field.name) : field.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (customName != null ? customName.hashCode() : 0);
        return result;
    }

    /**
     * 将属性转换成Map
     *
     * @return
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", this.getName());
        map.put("customName", this.getCustomName());
        map.put("type", this.getType());
        map.put("customType", this.getCustomType());
        map.put("hide", this.getHide());
        return map;
    }
}
