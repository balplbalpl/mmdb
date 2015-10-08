package com.mmdb.model.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.mmdb.core.utils.TimeUtil;

/**
 * 分类属性的数据类型
 *
 * @author
 */
public class TimeType extends Type {
    private static final long serialVersionUID = 1L;
    private String format = "yyyy-MM-dd HH:mm:ss";
    private String name = "Time";

    @Override
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setFormat(TimeFormat format) {
        this.format = format.str;
    }

    public enum TimeFormat {
        YMD_CN("yyyy-MM-dd"), YMD_EN("yyyy年MM月dd日"), YMDHMS_CN(
                "yyyy年MM月dd日 HH点mm分ss秒"), YMDHMS_EN("yyyy-MM-dd HH:mm:ss");
        private String str;

        private TimeFormat(String str) {
            this.str = str;
        }
    }

    public TimeType(TimeFormat format) throws Exception {
        if (format == null) {
            throw new Exception("参数为空值");
        }
        this.format = format.str;
    }

    public TimeType(String format) throws Exception {
        if (format == null) {
            throw new Exception("参数为空值");
        }
        this.format = format;
    }

    public TimeType() {

    }

    @Override
    public void testing(String time) throws Exception {
        if (time == null) {
            throw new Exception("参数为空值");
        } else if (!this.isCustom(time)) {
            try {
                new SimpleDateFormat(this.getFormat()).parse(time);
            } catch (ParseException e) {
                throw new Exception("参数格式有误[" + time + "]");
            }
        }
    }

    @Override
    public Object convert(String str) throws Exception {
        this.testing(str);
        if (this.isCustom(str)) {
            return TimeUtil.getTime(this.getFormat());
        } else {
            return str;
        }
    }
}
