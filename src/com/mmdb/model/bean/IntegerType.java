package com.mmdb.model.bean;

import java.util.Random;

/**
 * 分类属性的数据类型[整型]
 *
 * @author XIE
 */
public class IntegerType extends Type {
    private static final long serialVersionUID = 1L;
    private String name = "Integer";

    @Override
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IntegerType() {

    }

    /**
     * 测试数据是否为整型
     *
     * @param str
     * @throws Exception
     */
    @Override
    public void testing(String str) throws Exception {
        if (str == null) {
            throw new Exception("参数为null");
        } else {
            try {
                Integer.valueOf(str);
            } catch (Exception e) {
                throw new Exception("参数[" + str + "]不是整型");
            }
        }
    }

    /**
     * 整型转换到Object
     *
     * @param str 整型字符串
     * @return
     * @throws Exception
     */
    @Override
    public Object convert(String str) throws Exception {
        try {
            this.testing(str);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        if (this.isCustom(str)) {
            return new Random().nextInt();
        } else {
            return str;
        }
    }

}
