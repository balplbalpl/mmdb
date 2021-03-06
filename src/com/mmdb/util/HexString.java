package com.mmdb.util;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONArray;

/**
 * Created by Mir's on 2014/9/12.
 */
public class HexString {

    /**
     * 转化字符串为十六进制编码
     *
     * @param s
     * @return
     */
    public static String toHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /*
     * 16进制数字字符集
     */
    private static String hexString = "0123456789ABCDEF";

    /**
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     *
     * @param str
     * @return
     */
    public static String encode(String str) {
        // 根据默认编码获取字节数组
        try {
            byte[] bytes = str.getBytes("utf-8");
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            // 将字节数组中每个字节拆解成2位16进制整数
            for (int i = 0; i < bytes.length; i++) {
                sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
                sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     *
     * @param bytes
     * @return
     */
    public static String decode(String bytes) {
        try {
            if (bytes.indexOf("[") == -1) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(
                        bytes.length() / 2);
                // 将每2位16进制整数组装成一个字节
                for (int i = 0; i < bytes.length(); i += 2)
                    baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                            .indexOf(bytes.charAt(i + 1))));
                return new String(baos.toByteArray(), "utf-8");
            } else {
                return bytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将16进制数字解码成字符串,适用于所有字符（包括中文）
     *
     * @param bytes
     * @return["sdf","sdf"]
     */
    public static List<String> decode(List<String> bytes) {
        List<String> rets = new ArrayList<String>();
        for (String b : bytes) {
            if (bytes.indexOf("[") == -1) {
                rets.add(decode(b));
            } else {
                rets.add(b);
            }
        }
        return rets;
    }

    /**
     * 把数组转换成JSON字符串
     *
     * @param ids
     * @return
     */
    public static String json2Str(Object... ids) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ids.length; i++) {
            jsonArray.add(ids[i]);
        }
        return jsonArray.toString();
    }
    

    public static void main(String[] args) throws Exception {
    	
        String name = HexString.decode("5B22353561373132666239363661633035343665323362623165222C22676466736167225D");
        String str = HexString.decode("6D656D6F72795F7574696C6D656D6F7279");
       
        System.out.println(name +"   " + JSONArray.fromObject(name).get(0));
    }
}
