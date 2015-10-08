package com.mmdb.websocket;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.sf.json.JSONObject;

import com.mmdb.core.log.Log;
import com.mmdb.core.log.LogFactory;

@ServerEndpoint(value = "/websocket")
public class WebSocketMapping {
	private Log log = LogFactory.getLogger("WebSocketMapping");
	private static Map<String, WebSocketBase> wss = new HashMap<String, WebSocketBase>();
	private static Map<String, String> sessionMap = new HashMap<String, String>();
	private Session session;
	
	@OnOpen
	public void start(Session session) {
		this.session = session;
	}
	
	@OnClose
	public void end() {
		String sessionId = this.session.getId();
		String type = sessionMap.get(sessionId);
		if(type!=null){
			WebSocketBase wsb = wss.get(type);
			if(wsb!=null){
				wsb.close(sessionId);
			}
			sessionMap.remove(sessionId);
		}
	}
	
	@OnError
	public void onError(Throwable t) throws Throwable {
		log.eLog(t.getMessage());
	}
	
	@OnMessage
	public void incoming(String message) {
		try{
			JSONObject obj = JSONObject.fromObject(message);
			informActor(obj);
		}catch(Exception e){
			e.printStackTrace();
			log.eLog(e);
		}
	}
	
	private void informActor(JSONObject obj){
		String type = obj.getString("type");
		sessionMap.put(this.session.getId(), type);
		WebSocketBase wsb = wss.get(type);
		if(wsb==null){
			if("message".equals(type)){
				wsb = new WebSocketMessage();
			}
			wss.put(type, wsb);
		}
		JSONObject params = obj.containsKey("params") ? obj.getJSONObject("params"):new JSONObject();
		String token = obj.getString("token");
		wsb.receive(session, params, token);
	}
	
	public static WebSocketBase getWebSocketActor(String type){
		return wss.get(type);
	}
}
