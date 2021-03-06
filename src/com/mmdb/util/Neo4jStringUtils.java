package com.mmdb.util;

import java.util.List;

/**
 * 
 * @author TY
 * @version 2015年5月14日
 */
public class Neo4jStringUtils {

	public static String cypherESC(String str) {
		// str = str.replaceAll("'", "\\\'");
		String s = "";
		if (str != null) {
			s = str.replace("\\", "\\\\\\\\").replace("\"", "\\\"")
					.replace("'", "\\\\'").replace("\n", "").replace("\r", "")
					.replaceAll("[\\x00-\\x09\\x11\\x12\\x14-\\x1F\\x7F]", "");
		}
		return s;
	}

	/**
	 * 
	 * @param key
	 *            str
	 * @return `str`
	 */
	public static String cypherKey(String key) {
		return "`" + key + "`";
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	public static String toString(List<String> data) {
		if (data == null || data.size() == 0) {
			return "[]";
		}
		StringBuffer a = new StringBuffer();
		a.append("['");
		for (String value : data) {
			// a.append(cypherESC(value));
			a.append(value);
			a.append("','");
		}
		a.delete(a.length() - 2, a.length());
		a.append("]");
		return a.toString();
	}

	public static String replace(Object value) {
		if (value != null) {
			return value.toString().replaceAll("'", "''");
		}
		return null;
	}

	public static void main(String[] args) {
		String a = "aaa'ewr'qwe'ewrwe'wer'ew'";
		System.out.println();

	}

}
