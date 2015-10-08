package com.mmdb.util;

import java.io.InputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/** * BASE64加密解密 */
public class BASE64 {
	/** * BASE64解密 * @param key * @return * @throws Exception */
	public static byte[] decryptBASE64(String key) throws Exception {
		return (new BASE64Decoder()).decodeBuffer(key);
	}

	/** * BASE64加密 * @param key * @return * @throws Exception */
	public static String encryptBASE64(byte[] key) throws Exception {
		return (new BASE64Encoder()).encodeBuffer(key);
	}

	public static String base64Icon(String contentType, InputStream in) {
		if (in == null) {
			return null;
		}
		BASE64Encoder encoder = new BASE64Encoder();
		try {
			byte[] tmp = new byte[in.available()];
			in.read(tmp);
			String ret = encoder.encode(tmp);
			ret = "data:" + contentType + ";base64," + ret;
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}