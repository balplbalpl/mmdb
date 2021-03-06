package com.mmdb.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.mmdb.common.Global;
import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

import net.sf.json.JSONObject;

public abstract class WebSocketBase {
	private Log log = LogFactory.getLogger("WebSocketBase");
	protected Map<String, Map<String, Object>> conns = new ConcurrentHashMap<String, Map<String, Object>>();
	
	public void receive(Session session, JSONObject params, String token){
		try{
			Map<String, Object> m = conns.get(session.getId());
			if(m==null){
				m = new ConcurrentHashMap<String, Object>();
				m.put("session", session);
				m.put("token", token);
				conns.put(session.getId(), m);
			}
			
			execute(m, params);
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e);
		}
	}
	
	public void close(String sessionId){
		try{
			conns.remove(sessionId);
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e);
		}
	}
	
	protected void sendOne(Session session, String msg){
		try{
			session.getBasicRemote().sendText(msg);
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e);
			conns.remove(session.getId());
			try {
				session.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public JSONObject queryMmdb(String token, String restUrl, JSONObject data){
		 CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost("http://localhost:8080/"+Global.projectName+"/rest"+restUrl);
		StringEntity se = new StringEntity(data.toString(), "UTF-8");
		httppost.addHeader("token", token);
		httppost.addHeader("Accept", "application/json;charset=UTF-8");
		httppost.addHeader("Content-Type", "application/json;charset=UTF-8");
		httppost.setEntity(se);

		CloseableHttpResponse response = null;
		// 返回的List
		try {
			response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				if ("HTTP/1.1 200 OK".equals(response.getStatusLine()
						.toString())) {
					String rest = EntityUtils.toString(entity, "UTF-8");
					if (!"".equals(rest)) {
						JSONObject resObj = JSONObject.fromObject(rest);
						return resObj;
					}
				} else {
					System.out.println(response.getStatusLine());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(response!=null){
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(httpclient!=null){
				try {
					httpclient.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	protected void execute(Map<String, Object> m, JSONObject params) throws Exception{
		Set<String> set = params.keySet();
		for(String key:set){
			m.put(key, params.get(key));
		}
	}
}
