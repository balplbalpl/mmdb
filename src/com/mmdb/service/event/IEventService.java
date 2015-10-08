package com.mmdb.service.event;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.mmdb.model.event.Event;
import com.mmdb.model.event.EventView;

public interface IEventService {

	public List<Event> getEvents(String input, List<String> ciConf,
			List<Map<String, Object>> kpiConf, List<Integer> severities,
			Integer status, Long startTime, Long endTime, Boolean isHistory,
			Map<String, String> titles, Map<String, String> varcharTitles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap, JSONObject order, String userName)
			throws Exception;

	public Map<String, Long> getEventSeverity(EventView view,
			Boolean isHistory, String userName) throws Exception;

	public Event getEventBySerial(String serial, Map<String, String> titles,
			Map<String, Map<String, String>> severityMap,
			Map<String, String> statusMap) throws Exception;

	public List<Event> getEventByDuplicateSerial(String serial)
			throws Exception;

	public List<Event> getEventForView(List<String> ciHexes, String viewId,
			String userName, Long startTime, Long endTime) throws Exception;

	public void ackEvent(String serial, String ackInfo, String ackUid,
			Long ackTime) throws Exception;

	public void closeEvent(String serial, String closeInfo, String closeUid,
			Long closeTime, Integer status) throws Exception;

	public int getEventCountForView(String ciHex, String kpiHex, String viewId,
			String userName, Long startTime, Long endTime) throws Exception;

	public List<Event> getEventForView(String ciHex, String kpiHex,
			String viewId, String userName, Long startTime, Long endTime,
			int start, int limit) throws Exception;

	public void removeAllEvent() throws Exception;

	/**
	 * 获取指定视图.用户.ci.kpi上一段时间内级别最高的告警级别
	 * 
	 * @param viewId
	 *            视图id
	 * @param username
	 *            用户loginName
	 * @param ciKpis
	 *            [{"ciId":"xxx","kpiHex":"xxx"}...]
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws Exception
	 */
	public Map<String, Map<String, String>> getMaxSeverityForView(
			String viewId, String username, List<Map<String, Object>> ciKpis,
			long startTime, long endTime) throws Exception;
}
