package com.mmdb.model.bean;

import com.mmdb.core.utils.StringUtil;
import com.mmdb.core.utils.StringUtil.Dictionary;

/**
 * 分类属性的数据类型
 *
 * @author XIE
 */
public class StringType extends Type {

    private static final long serialVersionUID = 1L;
    private Dictionary dictionary = Dictionary.upper_letter;
    /**
     * 是否允许字符串为""
     */
    private boolean empty = true;
    private String name = "String";

    public StringType() {

    }

    public StringType(Dictionary dic) throws Exception {
        if (dic == null) {
            throw new Exception("参数不能为空");
        }

        this.dictionary = dic;
    }

    @Override
	public String getName() {
        return name;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringType empty() {
        this.empty = true;
        return this;
    }

    public StringType unEmpty() {
        this.empty = false;
        return this;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void testing(String str) throws Exception {
        if (str == null) {
            throw new Exception("参数不能为null");
        } else if (!this.empty && str.equals("")) {
            throw new Exception("参数不能为空");
        } else if (this.isCustom(str)) {
            this.customRandomNum(str);
        }
    }

    @Override
    public Object convert(String str) throws Exception {
        this.testing(str);
        if (this.isCustom(str)) {
            int i = this.customRandomNum(str);
            return StringUtil.generate(i, this.dictionary);
        } else {
            return str;
        }
    }
}