package com.mmdb.model.bean;


/**
 * 分类属性的数据类型[布尔类型]
 *
 * @author XIE
 */
public class BooleanType extends Type {
    private static final long serialVersionUID = 1L;
    private String name = "Boolean";

    public BooleanType() {

    }

    /**
     * 测试字符串是否是Boolean类型
     *
     * @param str Boolean字符串
     * @return
     * @throws Exception
     */
    @Override
    public void testing(String str) throws Exception {
        if (str == null) {
            throw new Exception("参数为null");
        } else {
            if (!str.equalsIgnoreCase("true") || !str.equalsIgnoreCase("false")) {
                throw new Exception("参数[" + str + "]不是Boolean类型");
            }
            Boolean.valueOf(str);
        }
    }

    /**
     * Boolean转换到Object
     *
     * @param str Boolean字符串
     * @return
     * @throws Exception
     */
    @Override
    public Object convert(String str) throws Exception {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
