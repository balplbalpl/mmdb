package com.mmdb.rest.jmx;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

import com.mmdb.core.utils.SpringContextUtil;
import com.mmdb.rest.BaseRest;
import com.mmdb.service.mq.MonitorActiveMqService;

/***
 * activemq队列监控管理
 * <p>
 * activemq.xml中配置 <borker useJmx="true"> <managementContext> <managementContext
 * connectorPort="1211" jmxDomainName="test"
 * connectorPath="/jmxrmi"></managementContext> </managementContext> </borker>
 * <p>
 * 
 * @author xiongjian
 * 
 */
public class MonitorActiveMq extends BaseRest {

	private MonitorActiveMqService mqService;

	@Override
	public void ioc(ApplicationContext context) {
		mqService = context.getBean(MonitorActiveMqService.class);
	}

	@Override
	public Representation getHandler() throws Exception {
		JSONObject ret = new JSONObject();
		try {
			Map<String, List<Map<String, String>>> queuesData = mqService
					.getQueuesData();
			List<Map<String, String>> retData = new ArrayList<Map<String, String>>();
			Set<String> keySet = queuesData.keySet();
			for (String key : keySet) {
				List<Map<String, String>> list = queuesData.get(key);
				for (Map<String, String> map : list) {
					map.put("brokerName", key);
					retData.add(map);
				}
			}
			ret.put("data", retData);
		} catch (Exception e) {

		}
		return new JsonRepresentation(ret.toString());
	}

	@Override
	public Representation delHandler(Representation entity) throws Exception {
		String param1 = getValue("param1");
		String param2 = getValue("param2");
		mqService.delQueue(param1, param2);
		JSONObject ret = new JSONObject();
		ret.put("message", "删除成功!");
		return new JsonRepresentation(ret.toString());
	}

	@Override
	public Representation postHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}

	@Override
	public Representation putHandler(Representation entity) throws Exception {
		return notFindMethod(entity);
	}
}
