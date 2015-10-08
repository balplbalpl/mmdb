package com.mmdb.model.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 属性支持类型
 *
 * @author XIE
 */
public class TypeFactory {
    public static Map<String, Type> TYPES = new HashMap<String, Type>();

    public static void init() {
        TYPES.put("String", new StringType());
        TYPES.put("Time", new TimeType());
        TYPES.put("Integer", new IntegerType());
        TYPES.put("Boolean", new BooleanType());
    }

    /**
     * 获取所有属性支持类型[String,Time,Integer,Boolean]
     *
     * @return
     */
    public static Map<String, Type> getTypes() {
        if (TYPES.size() == 0)
            init();
        return TYPES;
    }

    /**
     * 获取所有属性类型[String,Time,Integer,Boolean]
     *
     * @param ty [String,Time,Integer,Boolean]
     * @return
     */
    public static Type getType(String ty) {
        if (TYPES.size() == 0)
            init();
        return TYPES.get(ty);
    }
}
