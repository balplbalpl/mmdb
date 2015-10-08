package com.mmdb.service.event.impl;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mmdb.model.event.Event;
import com.mmdb.model.event.EventView;
import com.mmdb.model.event.storage.EventStorage;
import com.mmdb.service.event.IEventService;

@Component("eventService")
public class EventServiceImpl implements IEventService {
	@Autowired
	private EventStorage eventStorage;

	@Override
	public List<Event> getEvents(String input, List<String> ciConf,
			List<Map<String, Object>> kpiConf, List<Integer> severities,
			Integer status, Long startTime, Long endTime, Boolean isHistory,
			Map<String, String> titles, Map<String, String> varcharTitles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap, JSONObject order, String userName)
			throws Exception {
		return eventStorage.getEvents(input, ciConf, kpiConf, severities,
				status, startTime, endTime, isHistory, titles, varcharTitles,
				severityMap, statusMap, order, userName);
	}

	@Override
	public Map<String, Long> getEventSeverity(EventView view,
			Boolean isHistory, String userName) throws Exception {
		return eventStorage.getEventSeverity(view, isHistory, userName);
	}

	@Override
	public Event getEventBySerial(String serial, Map<String, String> titles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap) throws Exception {
		return eventStorage.getEventBySerial(serial, titles, severityMap,
				statusMap);
	}

	@Override
	public List<Event> getEventByDuplicateSerial(String serial)
			throws Exception {
		return eventStorage.getEventByDuplicateSerial(serial);
	}

	@Override
	public List<Event> getEventForView(List<String> ciHexes, String viewId,
			String userName, Long startTime, Long endTime) throws Exception {
		return eventStorage.getEventForView(ciHexes, viewId, userName,
				startTime, endTime);
	}
	@Override
	public int getEventCountForView(String ciHex, String kpiHex,
			String viewId, String userName, Long startTime, Long endTime)
			throws Exception {
		return eventStorage.getCountEventForView(ciHex, kpiHex, viewId, userName, startTime, endTime);
	}
	
	@Override
	public List<Event> getEventForView(String ciHex, String kpiHex,
			String viewId, String userName, Long startTime, Long endTime,
			int start, int limit) throws Exception {
		return eventStorage.getEventForView(ciHex, kpiHex, viewId, userName,
				startTime, endTime, start, limit);
	}

	@Override
	public Map<String,Map<String,String>> getMaxSeverityForView(String viewId,String username,List<Map<String,Object>> ciKpis,long startTime,long endTime) throws Exception{
		return eventStorage.getMaxSeverityByCiKPi(viewId, ciKpis, username, startTime, endTime);	
	}
	
	@Override
	public void ackEvent(String serial, String ackInfo, String ackUid,
			Long ackTime) throws Exception {
		eventStorage.ackEvent(serial, ackInfo, ackUid, ackTime);
	}

	@Override
	public void closeEvent(String serial, String closeInfo, String closeUid,
			Long closeTime, Integer status) throws Exception {
		eventStorage.closeEvent(serial, closeInfo, closeUid, closeTime, status);
	}

	@Override
	public void removeAllEvent() throws Exception {
		eventStorage.removeAllEvent();
	}

}
