package com.mmdb.model.bean;

import java.io.Serializable;
import java.util.*;

/**
 * 分类属性 类
 *
 * @author XIE
 */
public class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 属性名称
     */
    private String name;
    /**
     * 属性类别
     */
    private Type type;
    /**
     * 是否隐藏(true:隐藏)
     */
    private Boolean hide;
    /**
     * 显示优先级(true:优先显示)
     */
    private Boolean level;
    /**
     * 是否必填(true:必填)
     */
    private Boolean required;
    /**
     * 属性默认值
     */
    private String defaultValue;
    /**
     * 数据来源(PAGE,XLS,CMDB)
     */
    private List<String> sources = new ArrayList<String>();

    /**
     * [构造函数]
     */
    public Attribute() {

    }
    /**
     * [构造函数]完整
     *
     * @param name         属性名
     * @param type         类型
     * @param hide         是否隐藏(true:隐)
     * @param required     是否必填(true:必)
     * @param level        显示优先级
     * @param defaultValue 非必填默认值
     * @throws Exception
     */
    public Attribute(String name, Type type, boolean hide, boolean required,Boolean level,
                     String defaultValue) throws Exception {
        this.name = name;
        this.type = type;
        this.hide = hide;
        this.level = level;
        this.required = required;
        this.defaultValue = defaultValue;
        if (required && defaultValue == null) {
            throw new Exception("必填属性缺省值不能为null");
        }
        if (required) {
            this.getType().testing(defaultValue);
        }
    }

    /**
     * [构造函数]完整
     *
     * @param name         属性名
     * @param type         类型
     * @param hide         是否隐藏(true:隐)
     * @param required     是否必填(true:必)
     * @param level        显示优先级
     * @param defaultValue 非必填默认值
     * @param sources      数据来源
     * @throws Exception
     */
    public Attribute(String name, Type type, boolean hide, boolean required,Boolean level,
                     String defaultValue, List<String> sources) throws Exception {
        this(name,type,hide,required,level,defaultValue);
        this.sources = sources;
    }
    

    /**
     * [构造函数]属性不能为空(属性的缺省值为"")
     *
     * @param name     属性名称
     * @param required 是否必填(true:必)
     * @param level        显示优先级	
     * @throws Exception
     */
    public Attribute(String name, boolean required,Boolean level) throws Exception {
        this(name, new StringType(), false, required,level, "", null);
        String[] source = {"DCV", "PAGE", "XLS", "CMDB"};
        this.setSources(Arrays.asList(source));
    }
    
	/**
     * 获取属性名称
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * 设置属性名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取属性类型
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * 设置属性类型
     *
     * @param type 类型
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * 获取属性是否隐藏
     *
     * @return
     */
    public Boolean getHide() {
        return hide;
    }

    /**
     * 设置属性是否隐藏
     *
     * @param hide
     */
    public void setHide(Boolean hide) {
        this.hide = hide;
    }

    /**
     * 获取属性是否必填(true:必)
     *
     * @return
     */
    public Boolean getRequired() {
        return required;
    }

    /**
     * 设置属性是否必填(true:必)
     *
     * @param required
     */
    public void setRequired(Boolean required) {
        this.required = required;
    }

    /**
     * 获取属性的缺省值
     *
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * 设置属性的缺省值
     *
     * @param defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getLevel() {
        return level;
    }

    public void setLevel(Boolean level) {
        this.level = level;
    }

    /**
     * 获取属性的数据来源
     *
     * @return
     */
    public List<String> getSources() {
        return sources;
    }

    /**
     * 设置属性的数据来源
     *
     * @param sources
     */
    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    /**
     * 把字符串根据所属自定义类型转换成Object
     *
     * @param str
     * @return
     * @throws Exception
     */
    public Object convert(String str) throws Exception {
        try {
            return this.getType().convert(str);
        } catch (Exception e) {
            if (!this.getRequired()) {
                throw new Exception("属性类型转换[" + this.getName() + "]"
                        + e.getMessage());
            } else {
                try {
                    return this.getType().convert(this.defaultValue);
                } catch (Exception e1) {
                    return null;
                }
            }
        }
    }

    /**
     * 将属性转换成Map
     *
     * @return
     */
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", this.getName());
        map.put("required", this.getRequired());
        map.put("hide", this.getHide());
        map.put("level", this.getLevel());
        map.put("sources", this.getSources());
        map.put("type", this.getType().getName());
        map.put("defaultVal", this.getDefaultValue());
        return map;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Attribute other = (Attribute) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
	@Override
	public String toString() {
		return "Attribute [name=" + name + ", type=" + type + ", hide=" + hide
				+ ", level=" + level + ", required=" + required
				+ ", defaultValue=" + defaultValue + ", sources=" + sources
				+ "]";
	}

}