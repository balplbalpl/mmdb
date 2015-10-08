package com.mmdb.model.bean;

import java.io.Serializable;

/**
 * 分类属性的数据类型抽象类
 *
 * @author XIE
 */
public abstract class Type implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 验证参数类型是否符合要求
     *
     * @param str 参数
     * @throws Exception
     */
    public abstract void testing(String str) throws Exception;

    /**
     * 自定义类型转换到Object
     *
     * @param str 参数
     * @return
     * @throws Exception
     */
    public abstract Object convert(String str) throws Exception;

    /**
     * 获取参数类型名称
     *
     * @return
     */
    public abstract String getName();

    /**
     * 获取系统自定义格式数据的数字
     *
     * @param str
     * @return
     * @throws Exception
     */
    public int customRandomNum(final String str) throws Exception {
        if (!this.isCustom(str)) {
            throw new Exception("数据不符合自定义要求[" + str + "]");
        }
        try {
            return Integer.valueOf(str.substring(2, str.length() - 1).trim());
        } catch (Exception e) {
            throw new Exception("自定义不是数字[ " + str + "]");
        }
    }

    /**
     * 判断字符串是否为系统自定义格式数据
     *
     * @param str
     * @return
     */
    public boolean isCustom(String str) {
        return str.startsWith("${") && str.endsWith("}");
    }

}
