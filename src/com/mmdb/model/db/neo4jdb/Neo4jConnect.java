package com.mmdb.model.db.neo4jdb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.neo4j.shell.util.json.JSONArray;
import org.neo4j.shell.util.json.JSONObject;

public class Neo4jConnect {
	private static String neoUsername;
	private static String neoPassword;
	private static String neoIp;
	private static String neoPort;
	private static String url;
	private static String paw;

	private void init() {
		url = "http://" + neoIp + ":" + neoPort + "/db/data/cypher";
		paw = neoUsername + ":" + neoPassword;
	}

	private static CloseableHttpClient httpclient = HttpClients.createDefault();

	private static CloseableHttpResponse excute(HttpPost httppost) {
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				httpclient = HttpClients.createDefault();
				response = httpclient.execute(httppost);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return response;
	}

	public static JSONObject executionCypherBase(String sql) throws Exception {
		// CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost(url);
		String js = "{\"query\":\"" + sql + "\"}";
		StringEntity se = new StringEntity(js, "UTF-8");
		String auth = paw;
		String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
		httppost.addHeader("Authorization", "Basic " + code);
		httppost.addHeader("Accept", "application/json;charset=UTF-8");
		httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
		httppost.setEntity(se);

		CloseableHttpResponse response = excute(httppost);
		// 返回的List
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
						.toString())) {
					String rest = EntityUtils.toString(entity, "UTF-8");
					if (!"".equals(rest)) {
						long a = System.currentTimeMillis();
						JSONObject resObj = new JSONObject(rest);
						System.out.println("查询转JSON耗时====="
								+ (System.currentTimeMillis() - a));
						return resObj;
					}
				} else {
					System.out.println(response.getStatusLine());
					throw new Exception("cypher执行异常\n" + sql);
				}
			}
		} finally {
			response.close();
		}
		return new JSONObject();
	}

	public static JSONArray executionCypher(String sql) {
		// 创建默认的httpClient实例.
		// CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost(url);
		JSONArray dataL = new JSONArray();
		try {
			String js = "{\"query\":\"" + sql + "\"}";
			String encoderJson = URLEncoder.encode(js, "UTF-8");
			StringEntity se = new StringEntity(js, "UTF-8");
			String auth = paw;
			String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
			httppost.addHeader("Authorization", "Basic " + code);
			httppost.addHeader("Accept", "application/json;charset=UTF-8");
			httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
			httppost.setEntity(se);

			// CloseableHttpResponse response = httpclient.execute(httppost);
			CloseableHttpResponse response = excute(httppost);
			// 返回的List
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
							.toString())) {
						String rest = EntityUtils.toString(entity, "UTF-8");
						if (!"".equals(rest)) {
							long a = System.currentTimeMillis();
							JSONObject resObj = new JSONObject(rest);
							dataL = resObj.getJSONArray("data");
							System.out.println("查询转JSON耗时====="
									+ (System.currentTimeMillis() - a));
						}
					} else {
						System.out.println(response.getStatusLine());
//						throw new Exception("cypher执行异常");
						throw new Exception("cypher执行异常\n" + sql);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataL;
	}

	public static JSONArray executionCypher(String sql, String param) {
		// 创建默认的httpClient实例.
		// CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost("http://localhost:7474/db/data/cypher");
		JSONArray dataL = new JSONArray();
		try {
			String js = "";
			if (param == null || "".equals(param)) {
				js = "{\"query\":\"" + sql + "\"}";
			} else {
				js = "{\"query\":\"" + sql + "\",\"params\":{" + param + "}}";
			}
			String encoderJson = URLEncoder.encode(js, "UTF-8");
			StringEntity se = new StringEntity(js, "UTF-8");
			String auth = paw;
			String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
			httppost.addHeader("Authorization", "Basic " + code);
			httppost.addHeader("Accept", "application/json;charset=UTF-8");
			httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
			httppost.setEntity(se);

			CloseableHttpResponse response = excute(httppost);
			// CloseableHttpResponse response = httpclient.execute(httppost);
			// 返回的List

			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
							.toString())) {
						String rest = EntityUtils.toString(entity, "UTF-8");
						if (!"".equals(rest)) {
							long a = System.currentTimeMillis();
							JSONObject resObj = new JSONObject(rest);
							dataL = resObj.getJSONArray("data");
							System.out.println("查询转JSON耗时====="
									+ (System.currentTimeMillis() - a));
						}
					} else {
						System.out.println(response.getStatusLine());
//						throw new Exception("cypher执行异常");
						throw new Exception("cypher执行异常\n" + sql);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataL;
	}

	// public static JSONObject executionCypherBase(String sql) throws Exception
	// {
	// // CloseableHttpClient httpclient = HttpClients.createDefault();
	// // 创建httppost
	// HttpPost httppost = new HttpPost(url);
	// String js = "{\"query\":\"" + sql + "\"}";
	// StringEntity se = new StringEntity(js, "UTF-8");
	// String auth = paw;
	// String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
	// httppost.addHeader("Authorization", "Basic " + code);
	// httppost.addHeader("Accept", "application/json;charset=UTF-8");
	// httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
	// httppost.setEntity(se);
	//
	// CloseableHttpResponse response = excute(httppost);
	// // 返回的List
	// try {
	// HttpEntity entity = response.getEntity();
	// if (entity != null) {
	// if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
	// .toString())) {
	// String rest = EntityUtils.toString(entity, "UTF-8");
	// if (!"".equals(rest)) {
	// long a = System.currentTimeMillis();
	// JSONObject resObj = new JSONObject(rest);
	// System.out.println("haoshi==="+(System.currentTimeMillis()-a));
	// return resObj;
	// }
	// } else {
	// System.out.println(response.getStatusLine());
	// throw new Exception("cypher执行异常");
	// }
	// }
	// } finally {
	// response.close();
	// }
	// return new JSONObject();
	// }

	// public static List executionCypher(String sql) {
	// // 创建默认的httpClient实例.
	// // CloseableHttpClient httpclient = HttpClients.createDefault();
	// // 创建httppost
	// HttpPost httppost = new HttpPost(url);
	// List<Map> restL = new ArrayList<Map>();
	// List dataL = new ArrayList();
	// try {
	// String js = "{\"query\":\"" + sql + "\"}";
	// String encoderJson = URLEncoder.encode(js, "UTF-8");
	// StringEntity se = new StringEntity(js, "UTF-8");
	// String auth = paw;
	// String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
	// httppost.addHeader("Authorization", "Basic " + code);
	// httppost.addHeader("Accept", "application/json;charset=UTF-8");
	// httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
	// httppost.setEntity(se);
	//
	// // CloseableHttpResponse response = httpclient.execute(httppost);
	// CloseableHttpResponse response = excute(httppost);
	// // 返回的List
	// try {
	// HttpEntity entity = response.getEntity();
	// if (entity != null) {
	// if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
	// .toString())) {
	// String rest = EntityUtils.toString(entity, "UTF-8");
	// if (!"".equals(rest)) {
	// long a = System.currentTimeMillis();
	// JSONObject resObj = new JSONObject(rest);
	// System.out.println("haoshi==="+(System.currentTimeMillis()-a));
	// dataL = (List) resObj.getJSONArray("data");
	// }
	// } else {
	// System.out.println(response.getStatusLine());
	// throw new Exception("cypher执行异常");
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// response.close();
	// }
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (UnsupportedEncodingException e1) {
	// e1.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return dataL;
	// }

	// public static List executionCypher(String sql, String param) {
	// // 创建默认的httpClient实例.
	// // CloseableHttpClient httpclient = HttpClients.createDefault();
	// // 创建httppost
	// HttpPost httppost = new HttpPost("http://localhost:7474/db/data/cypher");
	// List<Map> restL = new ArrayList<Map>();
	// List dataL = new ArrayList();
	// try {
	// String js = "";
	// if (param == null || "".equals(param)) {
	// js = "{\"query\":\"" + sql + "\"}";
	// } else {
	// js = "{\"query\":\"" + sql + "\",\"params\":{" + param + "}}";
	// }
	// String encoderJson = URLEncoder.encode(js, "UTF-8");
	// StringEntity se = new StringEntity(js, "UTF-8");
	// String auth = paw;
	// String code = new sun.misc.BASE64Encoder().encode(auth.getBytes());
	// httppost.addHeader("Authorization", "Basic " + code);
	// httppost.addHeader("Accept", "application/json;charset=UTF-8");
	// httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
	// httppost.setEntity(se);
	//
	// CloseableHttpResponse response = excute(httppost);
	// // CloseableHttpResponse response = httpclient.execute(httppost);
	// // 返回的List
	//
	// try {
	// HttpEntity entity = response.getEntity();
	// if (entity != null) {
	// if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
	// .toString())) {
	// String rest = EntityUtils.toString(entity, "UTF-8");
	// if (!"".equals(rest)) {
	// long a = System.currentTimeMillis();
	// JSONObject resObj = new JSONObject(rest);
	// System.out.println("haoshi==="+(System.currentTimeMillis()-a));
	// dataL = (List) resObj.get("data");
	// }
	// } else {
	// System.out.println(response.getStatusLine());
	// throw new Exception("cypher执行异常");
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// response.close();
	// }
	// } catch (ClientProtocolException e) {
	// e.printStackTrace();
	// } catch (UnsupportedEncodingException e1) {
	// e1.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return dataL;
	// }

	public static String getNeoUsername() {
		return neoUsername;
	}

	public static void setNeoUsername(String neoUsername) {
		Neo4jConnect.neoUsername = neoUsername;
	}

	public static String getNeoPassword() {
		return neoPassword;
	}

	public static void setNeoPassword(String neoPassword) {
		Neo4jConnect.neoPassword = neoPassword;
	}

	public static String getNeoIp() {
		return neoIp;
	}

	public static void setNeoIp(String neoIp) {
		Neo4jConnect.neoIp = neoIp;
	}

	public static String getNeoPort() {
		return neoPort;
	}

	public static void setNeoPort(String neoPort) {
		Neo4jConnect.neoPort = neoPort;
	}
}
