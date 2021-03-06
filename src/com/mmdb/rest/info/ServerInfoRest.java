package com.mmdb.rest.info;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.sf.json.JSONObject;

import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.servlet.internal.ServletCall;
import org.restlet.representation.Representation;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.utils.ProjectInfo;
import com.mmdb.rest.BaseRest;

/**
 * 给前台提供 ip地址,端口号
 * 
 * @author xiongjian
 * 
 */
public class ServerInfoRest extends BaseRest {

	@Override
	public void ioc(ApplicationContext context) {

	}

	@Override
	public Representation getHandler() throws Exception {
		return getWebSocketHost();
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	/**
	 * 返回websocket的地址
	 * <p >
	 * 例如: 192.168.1.187:8080/mmdb2/websocket
	 */
	private Representation getWebSocketHost() {
		JSONObject ret = new JSONObject();
		ServerCall httpCall = ((HttpRequest) getRequest()).getHttpCall();
		ServletCall call = (ServletCall) httpCall;
		//System.out.println(call.getRequest().getLocalName());
		String ip = "127.0.0.1";
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ret.put("data", ip + ":" + call.getRequest().getLocalPort() + "/"
				+ ProjectInfo.getProjectName() + "/websocket");
		return new JsonRepresentation(ret.toString());
	}
}
