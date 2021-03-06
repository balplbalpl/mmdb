package com.mmdb.websocket;

import java.util.Map;
import java.util.Set;

import javax.websocket.Session;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

import net.sf.json.JSONObject;

public class WebSocketMessage extends WebSocketBase{
	private Log log = LogFactory.getLogger("WebSocketMessage");

	/**
	 * 用于上传文件,后返回给前台的数据
	 * 
	 * @param msg
	 * @param filename
	 */
	public void broadcast(String msg, double progress) {
		Set<String> sessionIds = conns.keySet();
		for (String sessionId:sessionIds) {
			Map<String, Object> m = conns.get(sessionId);
			if(m!=null){
				Session session = (Session)m.get("session");
				JSONObject ret = new JSONObject();
				ret.put("message", msg);
				ret.put("filename", (String)m.get("filename"));
				ret.put("progress", progress);
				sendOne(session, ret.toString());
			}
		}
	}
}
