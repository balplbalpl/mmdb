package com.mmdb.model.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 定时器计时类
 * 
 * @author XIE
 * 
 */
public class Timing implements Serializable {

	private static final long serialVersionUID = 1L;

	private static Pattern regMinute = Pattern
			.compile("^(0?[0-9]|[1-5][0-9])$");// 分钟
	private static Pattern regHour = Pattern
			.compile("^(0?[0-9]|1[0-9]|2[0-3])$");// 小时
	private static Pattern regDay = Pattern
			.compile("^(0?[0-9]|1[0-9]|2[0-9]|3[0-1])$");// 日
	private static Pattern regMonth = Pattern.compile("^(0?[1-9]|1[0-2])$");// 月份匹配
	private static Pattern regWeek = Pattern.compile("^[0-7]$");// 日
	private static Pattern regYear = Pattern.compile("^[2-9][0-9][1-9][0-9]$");// 年

	private String minute;// 分钟
	private String hour;// 小时
	private String day;// 日
	private String month;// 月
	private String week;// 周
	private String year;// 年

	private List<Integer> minuteData;
	private List<Integer> hourData;
	private List<Integer> dayData;
	private List<Integer> monthData;
	private List<Integer> weekData;
	private List<Integer> yearData;

	/**
	 * @param format
	 *            "分 时 天 月 周 年" 空格分割<br/>
	 *            *：所有 <br/>
	 *            ,：多选<br/>
	 *            /：起点/间隔(比如分钟 5/10是5,15,25,35,45,55)<br/>
	 *            例："0/2 17 22 9 * *" 代表 每年的9月22日17点 从0分开始 两分钟执行一次
	 * @throws Exception
	 */
	public Timing(String format) throws Exception {
		String[] datas = format.split(" ");
		if (datas.length != 6) {
			throw new Exception("定时器格式有误,必需是空格区分的，分 时 天 月 周 年");
		}
		this.setMinute(datas[0]);
		this.setHour(datas[1]);
		this.setDay(datas[2]);
		this.setMonth(datas[3]);
		this.setWeek(datas[4]);
		this.setYear(datas[5]);
	}

	/**
	 * @param format
	 *            "分 时 天 月 周 年" 空格分割<br/>
	 *            *：所有 <br/>
	 *            ,：多选<br/>
	 *            /：起点/间隔(比如分钟 5/10是5,15,25,35,45,55)<br/>
	 *            例："0/2 17 22 9 * *" 代表 每年的9月22日17点 从0分开始 两分钟执行一次
	 * @throws Exception
	 */
	public Timing(String type, String format) throws Exception {
		String[] datas = format.split(" ");
		if (datas.length != 6) {
			throw new Exception("定时器格式有误,必需是空格区分的，分 时 天 月 周 年");
		}
		this.setMinute(datas[0]);
		this.setHour(datas[1]);
		this.setDay(datas[2]);
		this.setMonth(datas[3]);
		this.setWeek(datas[4]);
		this.setYear(datas[5]);
	}

	/**
	 * @param sleep
	 *            多少分钟执行一次
	 * @throws Exception
	 */
	public Timing(int sleep) throws Exception {
		this("0/" + (sleep) + " * * * * *");
	}

	/**
	 * 精准时间执行
	 * 
	 * @param y
	 * @param m
	 * @param d
	 * @param h
	 * @param m2
	 * @throws Exception
	 */
	public Timing(int y, int m, int d, int h, int m2) throws Exception {
		this(m2 + " " + h + " " + d + " " + m + " " + y);
	}

	public String getMinute() {
		return minute;
	}

	/**
	 * 正则验证
	 * 
	 * @param reg
	 * @param str
	 * @return
	 * @throws Exception
	 */
	private Integer convert(Pattern reg, String str) throws Exception {
		if (reg.matcher(str).find()) {
			return Integer.valueOf(str);
		} else {
			throw new Exception(str + " 格式有误");
		}
	}

	/**
	 * 转换并验证数据
	 * 
	 * @param reg
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private List<Integer> value(Pattern reg, String key) throws Exception {
		List<Integer> ret = new ArrayList<Integer>();
		if (key == null) {
			throw new Exception(key + " 格式有误");
		}
		key = key.trim();
		if (!key.equals("*")) {
			if (key.indexOf("-") != -1) {// 范围
				String[] mes = key.split("-");
				if (mes.length != 2) {
					throw new Exception(key + " 格式有误");
				}
				Integer me1 = Integer.valueOf(mes[0]);
				Integer me2 = Integer.valueOf(mes[1]);
				if (me1 > me2) {
					throw new Exception(key + " 格式有误");
				}
				for (; me1 <= me2; me1++) {
					ret.add(convert(reg, me1.toString()));
				}
			} else if (key.indexOf(",") != -1) {// 数组
				String[] mes = key.split(",");
				for (String me : mes) {
					ret.add(convert(reg, me));
				}
			} else if (key.indexOf("/") != -1) {// 起点/间隔(5/20=25,45)
				String[] mes = key.split("/");
				if (mes.length != 2) {
					throw new Exception(key + " 格式有误");
				}
				Integer me1 = Integer.valueOf(mes[0]);
				Integer me2 = Integer.valueOf(mes[1]);
				if (me2 < 1 || me1 < 0) {
					throw new Exception(key + " 格式有误");
				}
				Integer start = me1;
				convert(reg, String.valueOf((me1 + me2)));
				try {
					while (true) {
						ret.add(convert(reg, start.toString()));
						start += me2;
					}
				} catch (Exception e) {

				}
			} else {// 单个
				ret.add(convert(reg, key));
			}
		} else {
			ret = null;// null 不限制
		}
		// System.out.println(ret);
		return ret;
	}

	public void setMinute(String minute) throws Exception {
		try {
			minuteData = value(regMinute, minute);
			this.minute = minute;
		} catch (Exception e) {
			Exception e2 = new Exception("minute " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getHour() {
		return hour;
	}

	public void setHour(String hour) throws Exception {
		try {
			hourData = value(regHour, hour);
			this.hour = hour;
		} catch (Exception e) {
			Exception e2 = new Exception("hour " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) throws Exception {
		try {
			dayData = value(regDay, day);
			this.day = day;
		} catch (Exception e) {
			Exception e2 = new Exception("day " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getMonth() {
		return month;
	}

	public void setMonth(String month) throws Exception {
		try {
			monthData = value(regMonth, month);
			this.month = month;
		} catch (Exception e) {
			Exception e2 = new Exception("month " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) throws Exception {
		try {
			weekData = value(regWeek, week);
			this.week = week;
		} catch (Exception e) {
			Exception e2 = new Exception("week " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) throws Exception {
		try {
			yearData = value(regYear, year);
			this.year = year;
		} catch (Exception e) {
			Exception e2 = new Exception("year " + e.getMessage());
			e2.setStackTrace(e.getStackTrace());
			throw e2;
		}
	}

	public String getFormat() {
		return this.getMinute() + " " + this.getHour() + " " + this.getDay()
				+ " " + this.getMonth() + " " + this.getWeek() + " "
				+ this.getYear();
	}

	/**
	 * 测试是否需要执行
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean testing() throws Exception {
		Date date = new Date();

		int year = date.getYear() + 1900;
		int month = date.getMonth() + 1;
		int week = date.getDay();
		int day = date.getDate();
		int hour = date.getHours();
		int minute = date.getMinutes();
		if ((minuteData == null || minuteData.contains(minute))
				&& (hourData == null || hourData.contains(hour))
				&& (dayData == null || dayData.contains(day))
				&& (weekData == null || weekData.contains(week))
				&& (monthData == null || monthData.contains(month))
				&& (yearData == null || yearData.contains(year))) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		// "分 时 天 月 周 年"
		// Timing timer = new Timing("0/1 * * * * *");
		Timing timer = new Timing("24/60 * 5 11 * 2013");
		System.out.println(timer.getFormat());
		System.out.println(timer.testing());
	}
}
